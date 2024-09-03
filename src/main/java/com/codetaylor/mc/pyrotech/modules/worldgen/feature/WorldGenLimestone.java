package com.codetaylor.mc.pyrotech.modules.worldgen.feature;

import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.worldgen.ModuleWorldGenConfig;
import com.codetaylor.mc.pyrotech.modules.worldgen.feature.spi.WorldGenOre;

import static com.codetaylor.mc.pyrotech.modules.worldgen.ModuleWorldGenConfig.LIMESTONE;

public class WorldGenLimestone extends WorldGenOre {

    public WorldGenLimestone() {
        super(ModuleCore.Blocks.LIMESTONE.getDefaultState(), LIMESTONE.MIN_HEIGHT,
                LIMESTONE.MAX_HEIGHT, LIMESTONE.ENABLED, LIMESTONE.CHANCES_TO_SPAWN,
                LIMESTONE.DIMENSION_WHITELIST, LIMESTONE.DIMENSION_BLACKLIST, random -> {
                    int minVeinSize = ModuleWorldGenConfig.FOSSIL.MIN_VEIN_SIZE;
                    int maxVeinSize = ModuleWorldGenConfig.FOSSIL.MAX_VEIN_SIZE;
                    return (minVeinSize + random.nextInt(Math.max(1, maxVeinSize - minVeinSize + 1)));
                });
    }
}
