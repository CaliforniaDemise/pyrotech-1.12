package com.codetaylor.mc.pyrotech.modules.worldgen;

import com.codetaylor.mc.athenaeum.module.ModuleBase;
import com.codetaylor.mc.athenaeum.registry.Registry;
import com.codetaylor.mc.pyrotech.ModPyrotech;
import com.codetaylor.mc.pyrotech.modules.worldgen.feature.spi.WorldGenerator;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ModuleWorldGen
        extends ModuleBase {

    public static final String MODULE_ID = "module.world_gen";
    public static final String MOD_ID = ModPyrotech.MOD_ID;
    public static final CreativeTabs CREATIVE_TAB = ModPyrotech.CREATIVE_TAB;

    private static WorldGenerator generator;

    public ModuleWorldGen() {

        super(0, MOD_ID);

        this.setRegistry(new Registry(MOD_ID, CREATIVE_TAB));
        this.enableAutoRegistry();

        MinecraftForge.TERRAIN_GEN_BUS.register(this);
        MinecraftForge.ORE_GEN_BUS.register(this);
    }

    @Override
    public void onInitializationEvent(FMLInitializationEvent event) {

        super.onInitializationEvent(event);
        generator = new WorldGenerator();
    }

    @SubscribeEvent
    public void decorate(DecorateBiomeEvent.Decorate event) {
        if (generator.check(event.getWorld(), event.getRand())) {
            generator.generateDecoration(event.getWorld(), event.getRand(), event.getChunkPos(), event.getType());
        }
    }

    @SubscribeEvent
    public void generateOres(OreGenEvent.Post event) {
        if (generator.check(event.getWorld(), event.getRand())) {
            generator.generateOre(event.getWorld(), event.getRand(), event.getPos());
        }
    }
}
