package com.codetaylor.mc.pyrotech.modules.tech.machine.init;

import com.codetaylor.mc.athenaeum.registry.Registry;
import com.codetaylor.mc.athenaeum.util.ModelRegistrationHelper;
import com.codetaylor.mc.pyrotech.interaction.spi.TESRInteractable;
import com.codetaylor.mc.pyrotech.modules.tech.machine.ModuleTechMachine;
import com.codetaylor.mc.pyrotech.modules.tech.machine.ModuleTechMachineConfig;
import com.codetaylor.mc.pyrotech.modules.tech.machine.block.*;
import com.codetaylor.mc.pyrotech.modules.tech.machine.tile.*;
import com.google.common.base.Preconditions;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

import static com.codetaylor.mc.athenaeum.util.ModelRegistrationHelper.PROPERTY_STRING_MAPPER;

public final class BlockInitializer {

  public static void onRegister(Registry registry) {

    registry.registerBlockWithItem(new BlockStoneKiln(), BlockStoneKiln.NAME);
    registry.registerBlockWithItem(new BlockStoneOven(), BlockStoneOven.NAME);
    registry.registerBlockWithItem(new BlockStoneSawmill(), BlockStoneSawmill.NAME);
    registry.registerBlockWithItem(new BlockStoneCrucible(), BlockStoneCrucible.NAME);

    registry.registerBlockWithItem(new BlockBrickKiln(), BlockBrickKiln.NAME);
    registry.registerBlockWithItem(new BlockBrickOven(), BlockBrickOven.NAME);
    registry.registerBlockWithItem(new BlockBrickSawmill(), BlockBrickSawmill.NAME);
    registry.registerBlockWithItem(new BlockBrickCrucible(), BlockBrickCrucible.NAME);

    registry.registerBlockWithItem(new BlockStoneHopper(), BlockStoneHopper.NAME);

    BlockMechanicalCompactingBin blockMechanicalCompactingBin = new BlockMechanicalCompactingBin();
    registry.registerBlock(blockMechanicalCompactingBin, new BlockMechanicalCompactingBin.Item(blockMechanicalCompactingBin), BlockMechanicalCompactingBin.NAME);

    registry.registerTileEntities(
        TileStoneKiln.class,
        TileStoneKilnTop.class,
        TileStoneOven.class,
        TileStoneOvenTop.class,
        TileStoneSawmill.class,
        TileStoneSawmillTop.class,
        TileStoneCrucible.class,
        TileStoneCrucibleTop.class,

        TileBrickKiln.class,
        TileBrickKilnTop.class,
        TileBrickOven.class,
        TileBrickOvenTop.class,
        TileBrickSawmill.class,
        TileBrickSawmillTop.class,
        TileBrickCrucible.class,
        TileBrickCrucibleTop.class,

        TileStoneHopper.class,
        TileMechanicalCompactingBinWorker.class
    );
  }

  @SideOnly(Side.CLIENT)
  public static void onClientRegister(Registry registry) {

    registry.registerClientModelRegistrationStrategy(() -> {

      ModelRegistrationHelper.registerBlockItemModels(
          ModuleTechMachine.Blocks.STONE_KILN,
          ModuleTechMachine.Blocks.STONE_SAWMILL,
          ModuleTechMachine.Blocks.STONE_OVEN,
          ModuleTechMachine.Blocks.STONE_CRUCIBLE,

          ModuleTechMachine.Blocks.BRICK_KILN,
          ModuleTechMachine.Blocks.BRICK_SAWMILL,
          ModuleTechMachine.Blocks.BRICK_OVEN,
          ModuleTechMachine.Blocks.BRICK_CRUCIBLE,

          ModuleTechMachine.Blocks.STONE_HOPPER,
          ModuleTechMachine.Blocks.MECHANICAL_COMPACTING_BIN
      );

      if (!ModuleTechMachineConfig.BRICK_KILN.USE_IRON_SKIN) {
        ModelLoader.setCustomStateMapper(ModuleTechMachine.Blocks.BRICK_KILN, new BrickMachineStateMapper());
        BlockInitializer.registerBrickMachineItemModel(ModuleTechMachine.Blocks.BRICK_KILN);
      }

      if (!ModuleTechMachineConfig.BRICK_SAWMILL.USE_IRON_SKIN) {
        ModelLoader.setCustomStateMapper(ModuleTechMachine.Blocks.BRICK_SAWMILL, new BrickMachineStateMapper());
        BlockInitializer.registerBrickMachineItemModel(ModuleTechMachine.Blocks.BRICK_SAWMILL);
      }

      if (!ModuleTechMachineConfig.BRICK_OVEN.USE_IRON_SKIN) {
        ModelLoader.setCustomStateMapper(ModuleTechMachine.Blocks.BRICK_OVEN, new BrickMachineStateMapper());
        BlockInitializer.registerBrickMachineItemModel(ModuleTechMachine.Blocks.BRICK_OVEN);
      }

      if (!ModuleTechMachineConfig.BRICK_CRUCIBLE.USE_IRON_SKIN) {
        ModelLoader.setCustomStateMapper(ModuleTechMachine.Blocks.BRICK_CRUCIBLE, new BrickMachineStateMapper());
        BlockInitializer.registerBrickMachineItemModel(ModuleTechMachine.Blocks.BRICK_CRUCIBLE);
      }

      // TESRs
      ClientRegistry.bindTileEntitySpecialRenderer(TileStoneKiln.class, new TESRInteractable<>());
      ClientRegistry.bindTileEntitySpecialRenderer(TileStoneOven.class, new TESRInteractable<>());
      ClientRegistry.bindTileEntitySpecialRenderer(TileStoneSawmill.class, new TESRInteractable<>());
      ClientRegistry.bindTileEntitySpecialRenderer(TileStoneCrucible.class, new TESRInteractable<>());

      ClientRegistry.bindTileEntitySpecialRenderer(TileBrickKiln.class, new TESRInteractable<>());
      ClientRegistry.bindTileEntitySpecialRenderer(TileBrickOven.class, new TESRInteractable<>());
      ClientRegistry.bindTileEntitySpecialRenderer(TileBrickSawmill.class, new TESRInteractable<>());
      ClientRegistry.bindTileEntitySpecialRenderer(TileBrickCrucible.class, new TESRInteractable<>());

      ClientRegistry.bindTileEntitySpecialRenderer(TileStoneHopper.class, new TESRInteractable<>());
      ClientRegistry.bindTileEntitySpecialRenderer(TileMechanicalCompactingBinWorker.class, new TESRInteractable<>());
    });
  }

  private static void registerBrickMachineItemModel(Block block) {

    ModelRegistrationHelper.registerItemModel(
        Item.getItemFromBlock(block),
        new ModelResourceLocation(
            Preconditions.checkNotNull(Block.REGISTRY.getNameForObject(block), "Block %s has null registry name", block) + "_brick_only",
            PROPERTY_STRING_MAPPER.getPropertyString(block.getDefaultState().getProperties())
        )
    );
  }

  private static class BrickMachineStateMapper
      extends DefaultStateMapper {

    @Nonnull
    @Override
    protected ModelResourceLocation getModelResourceLocation(IBlockState state) {

      return new ModelResourceLocation(Block.REGISTRY.getNameForObject(state.getBlock()) + "_brick_only", this.getPropertyString(state.getProperties()));
    }
  }

  private BlockInitializer() {
    //
  }
}
