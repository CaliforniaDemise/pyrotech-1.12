package com.codetaylor.mc.pyrotech.modules.core.init;

import com.codetaylor.mc.athenaeum.registry.Registry;
import com.codetaylor.mc.athenaeum.util.ModelRegistrationHelper;
import com.codetaylor.mc.pyrotech.library.util.RegistryHelper;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.core.block.*;
import com.codetaylor.mc.pyrotech.modules.core.block.item.ItemDoorStone;
import com.codetaylor.mc.pyrotech.modules.core.item.ItemRock;
import com.codetaylor.mc.pyrotech.modules.core.item.ItemRockGrass;
import com.codetaylor.mc.pyrotech.modules.core.item.ItemRockNetherrack;
import com.codetaylor.mc.pyrotech.modules.core.item.ItemStrawBed;
import com.codetaylor.mc.pyrotech.modules.core.tile.TileFarmlandMulched;
import com.codetaylor.mc.pyrotech.modules.hunting.tile.TileCarcass;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSlab;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class BlockInitializer {

  public static void onRegister(Registry registry) {

    registry.registerBlock(new BlockPyroberryBush(), BlockPyroberryBush.NAME);
    registry.registerBlock(new BlockGloamberryBush(), BlockGloamberryBush.NAME);

    final BlockRock blockRock = new BlockRock();
    final ItemRock itemRock = new ItemRock(blockRock);
    registry.registerBlock(blockRock, itemRock, BlockRock.NAME);

    final BlockRockGrass blockRockGrass = new BlockRockGrass();
    ItemRockGrass itemRockGrass = new ItemRockGrass(blockRockGrass);
    registry.registerBlock(blockRockGrass, itemRockGrass, BlockRockGrass.NAME);

    final BlockRockNetherrack blockRockNetherrack = new BlockRockNetherrack();
    ItemRockNetherrack itemRockNetherrack = new ItemRockNetherrack(blockRockNetherrack);
    registry.registerBlock(blockRockNetherrack, itemRockNetherrack, BlockRockNetherrack.NAME);

    final BlockRefractoryDoor blockRefractoryDoor = new BlockRefractoryDoor();
    registry.registerBlock(blockRefractoryDoor, BlockRefractoryDoor.NAME);
    registry.registerItem(new ItemDoorStone(blockRefractoryDoor), blockRefractoryDoor.getRegistryName());

    final BlockStoneDoor blockStoneDoor = new BlockStoneDoor();
    registry.registerBlock(blockStoneDoor, BlockStoneDoor.NAME);
    registry.registerItem(new ItemDoorStone(blockStoneDoor), blockStoneDoor.getRegistryName());

    final BlockStrawBed blockStrawBed = new BlockStrawBed();
    registry.registerBlock(blockStrawBed, BlockStrawBed.NAME);
    registry.registerItem(new ItemStrawBed(blockStrawBed), blockStrawBed.getRegistryName());

    registry.registerBlockWithItem(new BlockCobWet(), BlockCobWet.NAME);
    registry.registerBlockWithItem(new BlockCobDry(), BlockCobDry.NAME);
    registry.registerBlockWithItem(new BlockMud(), BlockMud.NAME);
    registry.registerBlockWithItem(new BlockMudLayer(), BlockMudLayer.NAME);
    registry.registerBlockWithItem(new BlockLogPile(), BlockLogPile.NAME);
    registry.registerBlockWithItem(new BlockCoalCokeBlock(), BlockCoalCokeBlock.NAME);
    registry.registerBlockWithItem(new BlockThatch(), BlockThatch.NAME);
    registry.registerBlockWithItem(new BlockLimestone(), BlockLimestone.NAME);
    registry.registerBlockWithItem(new BlockRefractoryGlass(), BlockRefractoryGlass.NAME);
    registry.registerBlockWithItem(new BlockSlagGlass(), BlockSlagGlass.NAME);
    registry.registerBlockWithItem(new BlockOreFossil(), BlockOreFossil.NAME);
    registry.registerBlockWithItem(new BlockOreDenseCoal(), BlockOreDenseCoal.NAME);
    registry.registerBlockWithItem(new BlockOreDenseNetherCoal(), BlockOreDenseNetherCoal.NAME);
    registry.registerBlockWithItem(new BlockCobblestone(), BlockCobblestone.NAME);
    registry.registerBlockWithItem(new BlockFarmlandMulched(), BlockFarmlandMulched.NAME);
    registry.registerBlockWithItem(new BlockPlanksTarred(), BlockPlanksTarred.NAME);
    registry.registerBlockWithItem(new BlockPileWoodChips(), BlockPileWoodChips.NAME);
    registry.registerBlockWithItem(new BlockPileAsh(), BlockPileAsh.NAME);
    registry.registerBlockWithItem(new BlockWoolTarred(), BlockWoolTarred.NAME);
    registry.registerBlockWithItem(new BlockCharcoalBlock(), BlockCharcoalBlock.NAME);
    registry.registerBlockWithItem(new BlockWoodTarBlock(), BlockWoodTarBlock.NAME);
    registry.registerBlockWithItem(new BlockLivingTar(), BlockLivingTar.NAME);
    registry.registerBlockWithItem(new BlockOreDenseRedstoneLarge(false), BlockOreDenseRedstoneLarge.NAME);
    registry.registerBlockWithItem(new BlockOreDenseRedstoneLarge(true), BlockOreDenseRedstoneLarge.NAME_ACTIVATED);
    registry.registerBlockWithItem(new BlockOreDenseRedstoneSmall(false), BlockOreDenseRedstoneSmall.NAME);
    registry.registerBlockWithItem(new BlockOreDenseRedstoneSmall(true), BlockOreDenseRedstoneSmall.NAME_ACTIVATED);
    registry.registerBlockWithItem(new BlockOreDenseRedstoneRocks(false), BlockOreDenseRedstoneRocks.NAME);
    registry.registerBlockWithItem(new BlockOreDenseRedstoneRocks(true), BlockOreDenseRedstoneRocks.NAME_ACTIVATED);
    registry.registerBlockWithItem(new BlockOreDenseQuartzLarge(), BlockOreDenseQuartzLarge.NAME);
    registry.registerBlockWithItem(new BlockOreDenseQuartzSmall(), BlockOreDenseQuartzSmall.NAME);
    registry.registerBlockWithItem(new BlockOreDenseQuartzRocks(), BlockOreDenseQuartzRocks.NAME);
    registry.registerBlockWithItem(new BlockCraftingTableTemplate(), BlockCraftingTableTemplate.NAME);

    {
      BlockRefractoryBrick block = new BlockRefractoryBrick();
      registry.registerBlockWithItem(block, BlockRefractoryBrick.NAME);
      IBlockState defaultState = block.getDefaultState();
      registry.registerBlockWithItem(new BlockRefractoryBrickStairs(defaultState), BlockRefractoryBrickStairs.NAME);
      BlockMasonryBrickSlab.Double slabDouble = new BlockMasonryBrickSlab.Double(defaultState);
      BlockMasonryBrickSlab.Half slab = new BlockMasonryBrickSlab.Half(defaultState);
      registry.registerBlock(slabDouble, BlockMasonryBrickSlab.Double.NAME);
      registry.registerBlock(slab, new ItemSlab(slab, slab, slabDouble), BlockMasonryBrickSlab.Half.NAME);
      registry.registerBlockWithItem(new BlockRefractoryBrickWall(), BlockRefractoryBrickWall.NAME);
    }

    {
      BlockMasonryBrick block = new BlockMasonryBrick();
      registry.registerBlockWithItem(block, BlockMasonryBrick.NAME);
      IBlockState defaultState = block.getDefaultState();
      registry.registerBlockWithItem(new BlockMasonryBrickStairs(defaultState), BlockMasonryBrickStairs.NAME);
      BlockRefractoryBrickSlab.Double slabDouble = new BlockRefractoryBrickSlab.Double(defaultState);
      BlockRefractoryBrickSlab.Half slab = new BlockRefractoryBrickSlab.Half(defaultState);
      registry.registerBlock(slabDouble, BlockRefractoryBrickSlab.Double.NAME);
      registry.registerBlock(slab, new ItemSlab(slab, slab, slabDouble), BlockRefractoryBrickSlab.Half.NAME);
      registry.registerBlockWithItem(new BlockMasonryBrickWall(), BlockMasonryBrickWall.NAME);
    }

    RegistryHelper.registerTileEntities(
        registry,
        TileFarmlandMulched.class,
        TileCarcass.class
    );
  }

  @SideOnly(Side.CLIENT)
  public static void onClientRegister(Registry registry) {

    registry.registerClientModelRegistrationStrategy(() -> {

      ModelRegistrationHelper.registerBlockItemModels(
          ModuleCore.Blocks.COB_WET,
          ModuleCore.Blocks.COB_DRY,
          ModuleCore.Blocks.MUD,
          ModuleCore.Blocks.MUD_LAYER,
          ModuleCore.Blocks.LOG_PILE,
          ModuleCore.Blocks.COAL_COKE_BLOCK,
          ModuleCore.Blocks.THATCH,
          ModuleCore.Blocks.REFRACTORY_BRICK,
          ModuleCore.Blocks.REFRACTORY_BRICK_SLAB,
          ModuleCore.Blocks.LIMESTONE,
          ModuleCore.Blocks.REFRACTORY_GLASS,
          ModuleCore.Blocks.SLAG_GLASS,
          ModuleCore.Blocks.MASONRY_BRICK,
          ModuleCore.Blocks.MASONRY_BRICK_SLAB,
          ModuleCore.Blocks.FARMLAND_MULCHED,
          ModuleCore.Blocks.PLANKS_TARRED,
          ModuleCore.Blocks.PILE_WOOD_CHIPS,
          ModuleCore.Blocks.PILE_ASH,
          ModuleCore.Blocks.WOOL_TARRED,
          ModuleCore.Blocks.CHARCOAL_BLOCK,
          ModuleCore.Blocks.WOOD_TAR_BLOCK,
          ModuleCore.Blocks.LIVING_TAR,
          ModuleCore.Blocks.ORE_FOSSIL,
          ModuleCore.Blocks.ORE_DENSE_COAL,
          ModuleCore.Blocks.ORE_DENSE_NETHER_COAL,
          ModuleCore.Blocks.ORE_DENSE_REDSTONE_LARGE,
          ModuleCore.Blocks.ORE_DENSE_REDSTONE_LARGE_ACTIVATED,
          ModuleCore.Blocks.ORE_DENSE_REDSTONE_SMALL,
          ModuleCore.Blocks.ORE_DENSE_REDSTONE_SMALL_ACTIVATED,
          ModuleCore.Blocks.ORE_DENSE_REDSTONE_ROCKS,
          ModuleCore.Blocks.ORE_DENSE_REDSTONE_ROCKS_ACTIVATED,
          ModuleCore.Blocks.ORE_DENSE_QUARTZ_LARGE,
          ModuleCore.Blocks.ORE_DENSE_QUARTZ_SMALL,
          ModuleCore.Blocks.ORE_DENSE_QUARTZ_ROCKS,
          ModuleCore.Blocks.CRAFTING_TABLE_TEMPLATE
      );

      ModelRegistrationHelper.registerItemModels(
          ModuleCore.Items.REFRACTORY_DOOR,
          ModuleCore.Items.STONE_DOOR,
          ModuleCore.Items.ROCK_GRASS,
          ModuleCore.Items.ROCK_NETHERRACK,
          ModuleCore.Items.STRAW_BED
      );

      ModelRegistrationHelper.registerItemModel(
          Item.getItemFromBlock(ModuleCore.Blocks.REFRACTORY_BRICK_WALL),
          ModuleCore.MOD_ID + ":" + BlockRefractoryBrickWall.NAME
      );

      ModelRegistrationHelper.registerItemModel(
          Item.getItemFromBlock(ModuleCore.Blocks.REFRACTORY_BRICK_STAIRS),
          ModuleCore.MOD_ID + ":" + BlockRefractoryBrickStairs.NAME
      );

      ModelRegistrationHelper.registerItemModel(
          Item.getItemFromBlock(ModuleCore.Blocks.MASONRY_BRICK_WALL),
          ModuleCore.MOD_ID + ":" + BlockMasonryBrickWall.NAME
      );

      ModelRegistrationHelper.registerItemModel(
          Item.getItemFromBlock(ModuleCore.Blocks.MASONRY_BRICK_STAIRS),
          ModuleCore.MOD_ID + ":" + BlockMasonryBrickStairs.NAME
      );

      // Refractory Door
      ModelLoader.setCustomStateMapper(
          ModuleCore.Blocks.REFRACTORY_DOOR,
          (new StateMap.Builder()).ignore(BlockDoor.POWERED).build()
      );

      // Stone Door
      ModelLoader.setCustomStateMapper(
          ModuleCore.Blocks.STONE_DOOR,
          (new StateMap.Builder()).ignore(BlockDoor.POWERED).build()
      );

      // Cobblestone
      ModelRegistrationHelper.registerVariantBlockItemModels(
          ModuleCore.Blocks.COBBLESTONE.getDefaultState(),
          BlockCobblestone.VARIANT
      );

      // Rock Pieces
      ModelLoader.setCustomStateMapper(
          ModuleCore.Blocks.ROCK,
          (new StateMap.Builder()).withName(BlockRock.VARIANT).build()
      );

      // Rock
      ModelRegistrationHelper.registerVariantBlockItemModelsSeparately(
          ModuleCore.MOD_ID,
          ModuleCore.Blocks.ROCK,
          BlockRock.VARIANT
      );

    });
  }

  @SideOnly(Side.CLIENT)
  public static void onClientInitialization() {

    // -------------------------------------------------------------------------
    // - Block Colors
    // -------------------------------------------------------------------------

    Minecraft minecraft = Minecraft.getMinecraft();
    BlockColors blockColors = minecraft.getBlockColors();

    // Grass Rock
    blockColors.registerBlockColorHandler((state, world, pos, tintIndex) -> {

      if (world != null && pos != null) {
        BiomeColorHelper.getGrassColorAtPos(world, pos);
      }

      return ColorizerGrass.getGrassColor(0.5D, 1.0D);

    }, ModuleCore.Blocks.ROCK_GRASS);
  }

  private BlockInitializer() {
    //
  }
}
