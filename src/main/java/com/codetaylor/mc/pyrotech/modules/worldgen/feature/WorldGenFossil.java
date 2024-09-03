package com.codetaylor.mc.pyrotech.modules.worldgen.feature;

import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.worldgen.ModuleWorldGenConfig;
import com.codetaylor.mc.pyrotech.modules.worldgen.feature.spi.WorldGenOre;

import static com.codetaylor.mc.pyrotech.modules.worldgen.ModuleWorldGenConfig.FOSSIL;

public class WorldGenFossil extends WorldGenOre {

    public WorldGenFossil() {
        super(ModuleCore.Blocks.ORE_FOSSIL.getDefaultState(), FOSSIL.MIN_HEIGHT,
                FOSSIL.MAX_HEIGHT, FOSSIL.ENABLED, FOSSIL.CHANCES_TO_SPAWN,
                FOSSIL.DIMENSION_WHITELIST, FOSSIL.DIMENSION_BLACKLIST, random -> {
                    int minVeinSize = ModuleWorldGenConfig.FOSSIL.MIN_VEIN_SIZE;
                    int maxVeinSize = ModuleWorldGenConfig.FOSSIL.MAX_VEIN_SIZE;
                    return (minVeinSize + random.nextInt(Math.max(1, maxVeinSize - minVeinSize + 1)));
                });
    }
}
