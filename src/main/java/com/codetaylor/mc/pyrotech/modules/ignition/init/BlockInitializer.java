package com.codetaylor.mc.pyrotech.modules.ignition.init;

import com.codetaylor.mc.athenaeum.registry.Registry;
import com.codetaylor.mc.athenaeum.util.ModelRegistrationHelper;
import com.codetaylor.mc.pyrotech.library.util.RegistryHelper;
import com.codetaylor.mc.pyrotech.modules.ignition.ModuleIgnition;
import com.codetaylor.mc.pyrotech.modules.ignition.block.BlockIgniter;
import com.codetaylor.mc.pyrotech.modules.ignition.block.BlockLampOil;
import com.codetaylor.mc.pyrotech.modules.ignition.block.BlockTorchFiber;
import com.codetaylor.mc.pyrotech.modules.ignition.block.BlockTorchStone;
import com.codetaylor.mc.pyrotech.modules.ignition.client.render.TESRLampOil;
import com.codetaylor.mc.pyrotech.modules.ignition.tile.TileIgniter;
import com.codetaylor.mc.pyrotech.modules.ignition.tile.TileLampOil;
import com.codetaylor.mc.pyrotech.modules.ignition.tile.TileTorchFiber;
import com.codetaylor.mc.pyrotech.modules.ignition.tile.TileTorchStone;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class BlockInitializer {

  public static void onRegister(Registry registry) {

    registry.registerBlockWithItem(new BlockIgniter(), BlockIgniter.NAME);
    registry.registerBlockWithItem(new BlockTorchFiber(), BlockTorchFiber.NAME);
    registry.registerBlockWithItem(new BlockTorchStone(), BlockTorchStone.NAME);
    registry.registerBlockWithItem(new BlockLampOil(), BlockLampOil.NAME);

    RegistryHelper.registerTileEntities(
        registry,
        TileTorchFiber.class,
        TileTorchStone.class,
        TileIgniter.class,
        TileLampOil.class
    );
  }

  @SideOnly(Side.CLIENT)
  public static void onClientRegister(Registry registry) {

    registry.registerClientModelRegistrationStrategy(() -> {

      ModelRegistrationHelper.registerBlockItemModels(
          ModuleIgnition.Blocks.TORCH_FIBER,
          ModuleIgnition.Blocks.TORCH_STONE,
          ModuleIgnition.Blocks.LAMP_OIL
      );

      // Igniter
      ModelRegistrationHelper.registerVariantBlockItemModels(
          ModuleIgnition.Blocks.IGNITER.getDefaultState(),
          BlockIgniter.VARIANT
      );

      // TESRs
      ClientRegistry.bindTileEntitySpecialRenderer(TileLampOil.class, new TESRLampOil());
    });
  }

  private BlockInitializer() {
    //
  }
}
