package com.codetaylor.mc.pyrotech.modules.tech.refractory.init;

import com.codetaylor.mc.athenaeum.parser.recipe.item.MalformedRecipeItemException;
import com.codetaylor.mc.athenaeum.parser.recipe.item.ParseResult;
import com.codetaylor.mc.athenaeum.parser.recipe.item.RecipeItemParser;
import com.codetaylor.mc.athenaeum.util.Injector;
import com.codetaylor.mc.athenaeum.util.Properties;
import com.codetaylor.mc.pyrotech.ModPyrotech;
import com.codetaylor.mc.pyrotech.library.util.BlockMetaMatcher;
import com.codetaylor.mc.pyrotech.library.util.Util;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.core.block.BlockRefractoryBrick;
import com.codetaylor.mc.pyrotech.modules.core.block.BlockRefractoryDoor;
import com.codetaylor.mc.pyrotech.modules.core.block.BlockRefractoryGlass;
import com.codetaylor.mc.pyrotech.modules.ignition.ModuleIgnition;
import com.codetaylor.mc.pyrotech.modules.ignition.block.BlockIgniter;
import com.codetaylor.mc.pyrotech.modules.tech.refractory.ModuleTechRefractory;
import com.codetaylor.mc.pyrotech.modules.tech.refractory.ModuleTechRefractoryConfig;
import com.codetaylor.mc.pyrotech.modules.tech.refractory.block.BlockTarCollector;
import com.codetaylor.mc.pyrotech.modules.tech.refractory.block.BlockTarDrain;
import com.codetaylor.mc.pyrotech.modules.tech.refractory.event.ItemTooltipEventHandler;
import com.codetaylor.mc.pyrotech.modules.tech.refractory.recipe.PitBurnRecipe;
import com.google.common.collect.Lists;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class RegistryInitializer {

  public static void createRegistries() {

    new RegistryBuilder<PitBurnRecipe>()
        .setName(new ResourceLocation(ModuleTechRefractory.MOD_ID, "pit_recipe"))
        .setType(PitBurnRecipe.class)
        .allowModification()
        .create();

    // --- Injection

    Injector injector = new Injector();

    injector.inject(
        ModuleTechRefractory.Registries.class,
        "BURN_RECIPE",
        GameRegistry.findRegistry(PitBurnRecipe.class)
    );
  }

  public static void initializeRefractoryBlocks() {

    RecipeItemParser parser = new RecipeItemParser();

    // ------------------------------------------------------------------------
    // - Refractory Blocks
    // ------------------------------------------------------------------------

    List<BlockMetaMatcher> blockMetaMatcherList = new ArrayList<>();

    blockMetaMatcherList.add(new BlockMetaMatcher(ModuleTechRefractory.Blocks.ACTIVE_PILE, 0));
    blockMetaMatcherList.add(new BlockMetaMatcher(ModuleTechRefractory.Blocks.PIT_ASH_BLOCK, 0));

    if (ModPyrotech.INSTANCE.isModuleEnabled(ModuleIgnition.class)) {
      blockMetaMatcherList.addAll(Arrays.asList(
          new BlockMetaMatcher(ModuleIgnition.Blocks.IGNITER, RegistryInitializer.getIgniterMeta(EnumFacing.NORTH)),
          new BlockMetaMatcher(ModuleIgnition.Blocks.IGNITER, RegistryInitializer.getIgniterMeta(EnumFacing.EAST)),
          new BlockMetaMatcher(ModuleIgnition.Blocks.IGNITER, RegistryInitializer.getIgniterMeta(EnumFacing.SOUTH)),
          new BlockMetaMatcher(ModuleIgnition.Blocks.IGNITER, RegistryInitializer.getIgniterMeta(EnumFacing.WEST))
      ));

      ItemTooltipEventHandler.VALID_REFRACTORY_ITEM_BLOCKS.addAll(Lists.newArrayList(
          new ParseResult(ModuleTechRefractory.MOD_ID, BlockIgniter.NAME, BlockIgniter.EnumType.BRICK.getMeta())
      ));
    }

    blockMetaMatcherList.addAll(Arrays.asList(
        new BlockMetaMatcher(ModuleCore.Blocks.REFRACTORY_BRICK, 0),
        new BlockMetaMatcher(ModuleCore.Blocks.REFRACTORY_GLASS, OreDictionary.WILDCARD_VALUE),
        new BlockMetaMatcher(ModuleTechRefractory.Blocks.TAR_COLLECTOR, BlockTarCollector.EnumType.BRICK.getMeta()),
        new BlockMetaMatcher(ModuleTechRefractory.Blocks.TAR_DRAIN, RegistryInitializer.getTarDrainMeta(EnumFacing.NORTH)),
        new BlockMetaMatcher(ModuleTechRefractory.Blocks.TAR_DRAIN, RegistryInitializer.getTarDrainMeta(EnumFacing.EAST)),
        new BlockMetaMatcher(ModuleTechRefractory.Blocks.TAR_DRAIN, RegistryInitializer.getTarDrainMeta(EnumFacing.SOUTH)),
        new BlockMetaMatcher(ModuleTechRefractory.Blocks.TAR_DRAIN, RegistryInitializer.getTarDrainMeta(EnumFacing.WEST)),
        new BlockMetaMatcher(ModuleCore.Blocks.REFRACTORY_BRICK_SLAB_DOUBLE, 0)
    ));

    ItemTooltipEventHandler.VALID_REFRACTORY_ITEM_BLOCKS.addAll(Lists.newArrayList(
        new ParseResult(ModuleTechRefractory.MOD_ID, BlockRefractoryBrick.NAME, 0),
        new ParseResult(ModuleTechRefractory.MOD_ID, BlockRefractoryGlass.NAME, 0),
        new ParseResult(ModuleTechRefractory.MOD_ID, BlockTarDrain.NAME, BlockTarDrain.EnumType.BRICK.getMeta()),
        new ParseResult(ModuleTechRefractory.MOD_ID, BlockTarCollector.NAME, BlockTarCollector.EnumType.BRICK.getMeta()),
        new ParseResult(ModuleTechRefractory.MOD_ID, BlockRefractoryDoor.NAME, 0)
    ));

    for (String blockString : ModuleTechRefractoryConfig.REFRACTORY.REFRACTORY_BRICKS) {
      try {
        blockMetaMatcherList.add(Util.parseBlockStringWithWildcard(blockString, parser));

      } catch (MalformedRecipeItemException e) {
        ModuleTechRefractory.LOGGER.error("", e);
      }
    }

    for (String itemString : ModuleTechRefractoryConfig.CLIENT.VALID_REFRACTORY_TOOLTIP) {
      try {
        ItemTooltipEventHandler.VALID_REFRACTORY_ITEM_BLOCKS.add(RecipeItemParser.INSTANCE.parse(itemString));

      } catch (MalformedRecipeItemException e) {
        ModuleTechRefractory.LOGGER.error("", e);
      }
    }

    // --- Injection

    Injector injector = new Injector();

    injector.inject(
        ModuleTechRefractory.Registries.class,
        "REFRACTORY_BLOCK_LIST",
        Collections.unmodifiableList(blockMetaMatcherList)
    );
  }

  private static int getTarDrainMeta(EnumFacing facing) {

    return ModuleTechRefractory.Blocks.TAR_DRAIN.getMetaFromState(
        ModuleTechRefractory.Blocks.TAR_DRAIN.getDefaultState()
            .withProperty(BlockTarDrain.VARIANT, BlockTarDrain.EnumType.BRICK)
            .withProperty(BlockTarDrain.FACING, facing)
    );
  }

  private static int getIgniterMeta(EnumFacing facing) {

    return ModuleIgnition.Blocks.IGNITER.getMetaFromState(
        ModuleIgnition.Blocks.IGNITER.getDefaultState()
            .withProperty(BlockIgniter.VARIANT, BlockIgniter.EnumType.BRICK)
            .withProperty(Properties.FACING_HORIZONTAL, facing)
    );
  }

  private RegistryInitializer() {
    //
  }
}
