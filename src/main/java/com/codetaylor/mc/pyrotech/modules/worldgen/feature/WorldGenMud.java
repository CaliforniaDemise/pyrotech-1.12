package com.codetaylor.mc.pyrotech.modules.worldgen.feature;

import com.codetaylor.mc.athenaeum.util.BlockHelper;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.core.block.BlockRock;
import com.codetaylor.mc.pyrotech.modules.worldgen.ModuleWorldGenConfig;
import com.codetaylor.mc.pyrotech.modules.worldgen.feature.spi.IWorldGenFeature;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;

import javax.annotation.Nonnull;
import java.util.Random;

public class WorldGenMud
        implements IWorldGenFeature {

    @Override
    public boolean generateDecoration(@Nonnull World world, @Nonnull Random random, ChunkPos chunkPos, DecorateBiomeEvent.Decorate.EventType type) {
        if (type != DecorateBiomeEvent.Decorate.EventType.CLAY) return false;

        final int chancesToSpawn = ModuleWorldGenConfig.MUD.CHANCES_TO_SPAWN;
        final int blockXPos = (chunkPos.x << 4);
        final int blockZPos = (chunkPos.z << 4);

        for (int i = 0; i < chancesToSpawn; i++) {
            int x = blockXPos + random.nextInt(16) + 8;
            int z = blockZPos + random.nextInt(16) + 8;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            IBlockState previousState = world.getBlockState(pos.setPos(x, 255, z));
            IBlockState currentState = world.getBlockState(pos.setPos(x, 254, z));

            for (int y = 253; y >= 0; y--) {

                if (currentState.getBlock() == Blocks.DIRT
                        && currentState.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT) {

                    if (previousState.getBlock() == Blocks.WATER) {
                        BlockHelper.forBlocksInRange(world, pos, 2 + random.nextInt(3), (w, p, bs) -> {
                            if (bs.getBlock() == Blocks.DIRT && bs.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT) {
                                if (w.getBlockState(p.up()).getBlock() == Blocks.WATER) {
                                    w.setBlockState(p, ModuleCore.Blocks.MUD.getDefaultState(), 2);
                                } else {
                                    for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                                        if (w.getBlockState(p.offset(facing)).getBlock() == Blocks.WATER) {
                                            w.setBlockState(p, ModuleCore.Blocks.MUD.getDefaultState(), 2);
                                            break;
                                        }
                                    }
                                }
                            } else if (bs.getBlock() == Blocks.AIR) {
                                BlockPos downPos = p.down();
                                IBlockState downState = w.getBlockState(downPos);
                                if (downState.getBlock().isTopSolid(downState)) {
                                    if (random.nextFloat() < 0.75) {
                                        w.setBlockState(p, ModuleCore.Blocks.ROCK.getDefaultState().withProperty(BlockRock.VARIANT, BlockRock.EnumType.MUD), 2);
                                    } else if (!w.canSeeSky(p)) {
                                        w.setBlockState(downPos, ModuleCore.Blocks.MUD.getDefaultState(), 2);
                                    }
                                }
                            }
                            return true; // continue executing
                        });
                        break;
                    }
                }

                previousState = currentState;
                currentState = world.getBlockState(pos.setPos(x, y, z));
            }
        }

        return true;
    }

    @Override
    public boolean isAllowed(int dimensionId) {

        return ModuleWorldGenConfig.MUD.ENABLED
                && this.isAllowedDimension(dimensionId, ModuleWorldGenConfig.MUD.DIMENSION_WHITELIST, ModuleWorldGenConfig.MUD.DIMENSION_BLACKLIST);
    }
}
