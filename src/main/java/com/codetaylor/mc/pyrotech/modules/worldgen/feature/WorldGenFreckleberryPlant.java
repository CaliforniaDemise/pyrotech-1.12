package com.codetaylor.mc.pyrotech.modules.worldgen.feature;

import com.codetaylor.mc.athenaeum.util.BlockHelper;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.worldgen.ModuleWorldGenConfig;
import com.codetaylor.mc.pyrotech.modules.worldgen.feature.spi.IWorldGenFeature;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class WorldGenFreckleberryPlant
        implements IWorldGenFeature {

    private final BlockPos.MutableBlockPos pos;

    private Set<Biome> allowedBiomeSet;

    public WorldGenFreckleberryPlant() {

        this.pos = new BlockPos.MutableBlockPos();
    }

    @Override
    public boolean generateDecoration(@Nonnull World world, @Nonnull Random random, ChunkPos pos, DecorateBiomeEvent.Decorate.EventType type) {
        if (type != DecorateBiomeEvent.Decorate.EventType.FLOWERS && type != DecorateBiomeEvent.Decorate.EventType.DEAD_BUSH)
            return false;

        final int blockXPos = pos.x << 4;
        final int blockZPos = pos.z << 4;

        Biome biome = world.getBiome(this.pos.setPos(blockXPos, 0, blockZPos));

        if (!this.isAllowed(biome)) {
            return false;
        }

        if (random.nextFloat() > ModuleWorldGenConfig.FRECKLEBERRY_PLANT.CLUSTER_FREQUENCY) {
            return false;
        }

        final double density = ModuleWorldGenConfig.FRECKLEBERRY_PLANT.DENSITY;

        for (int i = 0; i < ModuleWorldGenConfig.FRECKLEBERRY_PLANT.CHANCES_TO_SPAWN; i++) {

            int posX = blockXPos + random.nextInt(16) + 8;
            int posY = world.getHeight(blockXPos, blockZPos);
            int posZ = blockZPos + random.nextInt(16) + 8;

            BlockHelper.forBlocksInCube(world, new BlockPos(posX, posY, posZ), 4, 4, 4, (w, p, bs) -> {

                if (w.isAirBlock(p)
                        && random.nextFloat() < density
                        && this.canSpawnOnTopOf(w, p.down(), w.getBlockState(p.down()))) {

                    if (random.nextFloat() < ModuleWorldGenConfig.FRECKLEBERRY_PLANT.CHANCE_TO_SPAWN_RIPE) {
                        world.setBlockState(p, ModuleCore.Blocks.FRECKLEBERRY_PLANT.withAge(7), 2 | 16);

                    } else {
                        world.setBlockState(p, ModuleCore.Blocks.FRECKLEBERRY_PLANT.withAge(random.nextInt(5) + 2), 2 | 16);
                    }
                }

                return true; // keep processing
            });
        }

        return true;
    }

    @Override
    public boolean isAllowed(int dimensionId) {

        return ModuleWorldGenConfig.FRECKLEBERRY_PLANT.ENABLED
                && ModuleWorldGenConfig.FRECKLEBERRY_PLANT.CHANCES_TO_SPAWN > 0
                && ModuleWorldGenConfig.FRECKLEBERRY_PLANT.CLUSTER_FREQUENCY > 0
                && this.isAllowedDimension(dimensionId, ModuleWorldGenConfig.FRECKLEBERRY_PLANT.DIMENSION_WHITELIST, ModuleWorldGenConfig.FRECKLEBERRY_PLANT.DIMENSION_BLACKLIST);
    }

    private boolean isAllowed(Biome biome) {

        if (this.allowedBiomeSet == null) {
            String[] allowedBiomes = ModuleWorldGenConfig.FRECKLEBERRY_PLANT.ALLOWED_BIOMES;
            this.allowedBiomeSet = new HashSet<>(allowedBiomes.length);

            for (String allowedBiome : allowedBiomes) {
                Biome biomeLookup = ForgeRegistries.BIOMES.getValue(new ResourceLocation(allowedBiome));

                if (biomeLookup != null) {
                    this.allowedBiomeSet.add(biomeLookup);

                } else {
                    ModuleCore.LOGGER.error("Missing biome registration for " + allowedBiome + " in Pyroberry Bush config");
                }
            }
        }

        return this.allowedBiomeSet.contains(biome);
    }

    private boolean canSpawnOnTopOf(World world, BlockPos pos, IBlockState blockState) {

        return ModuleCore.Blocks.FRECKLEBERRY_PLANT.isValidBlock(blockState);
    }
}
