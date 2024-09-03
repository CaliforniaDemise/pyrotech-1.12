package com.codetaylor.mc.pyrotech.modules.worldgen.feature;

import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.worldgen.ModuleWorldGenConfig;
import com.codetaylor.mc.pyrotech.modules.worldgen.feature.spi.WorldGenOre;
import net.minecraft.init.Blocks;

import static com.codetaylor.mc.pyrotech.modules.worldgen.ModuleWorldGenConfig.DENSE_NETHER_COAL_ORE;

public class WorldGenDenseNetherCoal extends WorldGenOre {

    public WorldGenDenseNetherCoal() {
        super(ModuleCore.Blocks.ORE_DENSE_NETHER_COAL.getDefaultState(), DENSE_NETHER_COAL_ORE.MIN_HEIGHT,
                DENSE_NETHER_COAL_ORE.MAX_HEIGHT, DENSE_NETHER_COAL_ORE.ENABLED, DENSE_NETHER_COAL_ORE.CHANCES_TO_SPAWN,
                DENSE_NETHER_COAL_ORE.DIMENSION_WHITELIST, DENSE_NETHER_COAL_ORE.DIMENSION_BLACKLIST, random -> {
                    int minVeinSize = ModuleWorldGenConfig.DENSE_NETHER_COAL_ORE.MIN_VEIN_SIZE;
                    int maxVeinSize = ModuleWorldGenConfig.DENSE_NETHER_COAL_ORE.MAX_VEIN_SIZE;
                    return (minVeinSize + random.nextInt(Math.max(1, maxVeinSize - minVeinSize + 1)));
                }, state -> state.getBlock() == Blocks.NETHERRACK);
    }
}
