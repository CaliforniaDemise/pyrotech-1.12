package com.codetaylor.mc.pyrotech.modules.worldgen.feature;

import com.codetaylor.mc.athenaeum.util.BlockHelper;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.worldgen.ModuleWorldGenConfig;
import com.codetaylor.mc.pyrotech.modules.worldgen.feature.spi.IWorldGenFeature;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;

import javax.annotation.Nonnull;
import java.util.Random;

public class WorldGenDenseQuartzOre implements IWorldGenFeature {

    @Override
    public boolean generateDecoration(@Nonnull World world, @Nonnull Random random, ChunkPos pos, DecorateBiomeEvent.Decorate.EventType type) {
        final int blockXPos = pos.x << 4;
        final int blockZPos = pos.z << 4;

        if (random.nextFloat() > ModuleWorldGenConfig.DENSE_QUARTZ_ORE.CHANCE_TO_SPAWN) {
            return false;
        }

        for (int i = 0; i < 20; i++) {

            int[] posX;
            int[] posY;
            int[] posZ;

            int toPlaceLargeCount = 1 + random.nextInt(2);
            int[] toPlaceLarge = new int[]{toPlaceLargeCount};
            int[] toPlaceSmall = new int[]{3 + random.nextInt(3)};
            int[] toPlaceRocks = new int[]{6 + random.nextInt(3)};

            double oreChance = ModuleWorldGenConfig.DENSE_QUARTZ_ORE.CHANCE_TO_SPAWN_QUARTZ_ORE;

            posX = new int[]{blockXPos + random.nextInt(16) + 8};
            {
                int min = Math.min(255, Math.max(0, ModuleWorldGenConfig.DENSE_QUARTZ_ORE.VERTICAL_BOUNDS[0]));
                int max = ModuleWorldGenConfig.DENSE_QUARTZ_ORE.VERTICAL_BOUNDS[1];
                int range = Math.max(1, max - min + 1);
                posY = new int[]{Math.min(255, Math.max(0, min + random.nextInt(range)))};
            }
            posZ = new int[]{blockZPos + random.nextInt(16) + 8};

            BlockHelper.forBlocksInCube(world, new BlockPos(posX[0], posY[0], posZ[0]), 4, 4, 4, (w, p, bs) -> {

                BlockPos posDown = p.down();

                if (w.isAirBlock(p)
                        && this.canSpawnOnTopOf(w, posDown, w.getBlockState(posDown))) {
                    world.setBlockState(p, ModuleCore.Blocks.ORE_DENSE_QUARTZ_LARGE.getDefaultState(), 2 | 16);
                    toPlaceLarge[0] -= 1;

                    if (world.getBlockState(posDown).getBlock() == Blocks.NETHERRACK
                            && random.nextFloat() < oreChance) {
                        world.setBlockState(posDown, Blocks.QUARTZ_ORE.getDefaultState(), 2 | 16);
                    }

                    if (toPlaceLarge[0] == 0) {
                        posX[0] = p.getX();
                        posY[0] = p.getY();
                        posZ[0] = p.getZ();
                        return false; // stop processing
                    }
                }

                return true; // keep processing
            });

            if (toPlaceLarge[0] == toPlaceLargeCount) {
                continue;
            }

            BlockHelper.forBlocksInCubeShuffled(world, new BlockPos(posX[0], posY[0], posZ[0]), 4, 4, 4, (w, p, bs) -> {

                BlockPos posDown = p.down();

                if (w.isAirBlock(p)
                        && this.canSpawnOnTopOf(w, posDown, w.getBlockState(posDown))) {
                    world.setBlockState(p, ModuleCore.Blocks.ORE_DENSE_QUARTZ_SMALL.getDefaultState(), 2 | 16);
                    toPlaceSmall[0] -= 1;

                    if (world.getBlockState(posDown).getBlock() == Blocks.NETHERRACK
                            && random.nextFloat() < oreChance) {
                        world.setBlockState(posDown, Blocks.QUARTZ_ORE.getDefaultState(), 2 | 16);
                    }
                }

                return toPlaceSmall[0] > 0; // keep processing
            });

            BlockHelper.forBlocksInCubeShuffled(world, new BlockPos(posX[0], posY[0], posZ[0]), 4, 4, 4, (w, p, bs) -> {

                BlockPos posDown = p.down();

                if (w.isAirBlock(p)
                        && this.canSpawnOnTopOf(w, posDown, w.getBlockState(posDown))) {
                    world.setBlockState(p, ModuleCore.Blocks.ORE_DENSE_QUARTZ_ROCKS.getDefaultState(), 2 | 16);
                    toPlaceRocks[0] -= 1;

                    if (world.getBlockState(posDown).getBlock() == Blocks.NETHERRACK
                            && random.nextFloat() < oreChance) {
                        world.setBlockState(posDown, Blocks.QUARTZ_ORE.getDefaultState(), 2 | 16);
                    }
                }

                return toPlaceRocks[0] > 0; // keep processing
            });

            break;
        }

        return true;
    }

    @Override
    public boolean isAllowed(int dimensionId) {

        return ModuleWorldGenConfig.DENSE_QUARTZ_ORE.ENABLED
                && ModuleWorldGenConfig.DENSE_QUARTZ_ORE.CHANCE_TO_SPAWN > 0
                && this.isAllowedDimension(dimensionId, ModuleWorldGenConfig.DENSE_QUARTZ_ORE.DIMENSION_WHITELIST, ModuleWorldGenConfig.DENSE_QUARTZ_ORE.DIMENSION_BLACKLIST);
    }

    private boolean canSpawnOnTopOf(World world, BlockPos pos, IBlockState blockState) {

        if (!blockState.isSideSolid(world, pos, EnumFacing.UP)) {
            return false;
        }

        Material material = blockState.getMaterial();
        return material == Material.ROCK;
    }
}
