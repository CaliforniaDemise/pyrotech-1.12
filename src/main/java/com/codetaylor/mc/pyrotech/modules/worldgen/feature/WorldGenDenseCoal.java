package com.codetaylor.mc.pyrotech.modules.worldgen.feature;

import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.worldgen.ModuleWorldGenConfig;
import com.codetaylor.mc.pyrotech.modules.worldgen.feature.spi.WorldGenOre;
import net.minecraft.init.Blocks;

import static com.codetaylor.mc.pyrotech.modules.worldgen.ModuleWorldGenConfig.DENSE_COAL_ORE;

// TODO maybe handle it with decoration. It ends up being really rare.
public class WorldGenDenseCoal extends WorldGenOre {
    public WorldGenDenseCoal() {
        super(ModuleCore.Blocks.ORE_DENSE_COAL.getDefaultState(), DENSE_COAL_ORE.MIN_HEIGHT,
                DENSE_COAL_ORE.MAX_HEIGHT, DENSE_COAL_ORE.ENABLED, DENSE_COAL_ORE.CHANCES_TO_SPAWN,
                DENSE_COAL_ORE.DIMENSION_WHITELIST, DENSE_COAL_ORE.DIMENSION_BLACKLIST, random -> {
                    int minVeinSize = ModuleWorldGenConfig.DENSE_COAL_ORE.MIN_VEIN_SIZE;
                    int maxVeinSize = ModuleWorldGenConfig.DENSE_COAL_ORE.MAX_VEIN_SIZE;
                    return (minVeinSize + random.nextInt(Math.max(1, maxVeinSize - minVeinSize + 1)));
                }, state -> state.getBlock() == Blocks.COAL_ORE);
    }
}
