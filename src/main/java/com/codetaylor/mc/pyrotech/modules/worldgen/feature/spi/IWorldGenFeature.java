package com.codetaylor.mc.pyrotech.modules.worldgen.feature.spi;

import com.codetaylor.mc.athenaeum.util.ArrayHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;

import javax.annotation.Nonnull;
import java.util.Random;

public interface IWorldGenFeature {

    boolean isAllowed(int dimensionId);

    default boolean isAllowedDimension(int dimensionId, int[] whitelist, int[] blacklist) {

        if (whitelist.length > 0) {
            return ArrayHelper.containsInt(whitelist, dimensionId);

        } else if (blacklist.length > 0) {
            return !ArrayHelper.containsInt(blacklist, dimensionId);
        }

        return true;
    }

    default boolean generateDecoration(@Nonnull World world, @Nonnull Random random, ChunkPos pos, DecorateBiomeEvent.Decorate.EventType type) {
        return false;
    }

    default boolean generateOre(@Nonnull World world, Random random, BlockPos pos) {
        return false;
    }
}