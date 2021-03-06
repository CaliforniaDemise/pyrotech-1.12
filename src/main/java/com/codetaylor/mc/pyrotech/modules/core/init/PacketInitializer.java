package com.codetaylor.mc.pyrotech.modules.core.init;

import com.codetaylor.mc.athenaeum.interaction.network.CSPacketInteractionMouseWheel;
import com.codetaylor.mc.athenaeum.network.IPacketRegistry;
import com.codetaylor.mc.athenaeum.network.tile.SCPacketTileData;
import com.codetaylor.mc.pyrotech.modules.core.network.*;
import net.minecraftforge.fml.relauncher.Side;

public final class PacketInitializer {

  public static void register(IPacketRegistry registry) {

    registry.register(
        SCPacketParticleLava.class,
        SCPacketParticleLava.class,
        Side.CLIENT
    );

    registry.register(
        SCPacketModuleListRequest.class,
        SCPacketModuleListRequest.class,
        Side.CLIENT
    );

    registry.register(
        SCPacketParticleBoneMeal.class,
        SCPacketParticleBoneMeal.class,
        Side.CLIENT
    );

    registry.register(
        SCPacketParticleProgress.class,
        SCPacketParticleProgress.class,
        Side.CLIENT
    );

    registry.register(
        SCPacketParticleCombust.class,
        SCPacketParticleCombust.class,
        Side.CLIENT
    );

    registry.register(
        CSPacketModuleListResponse.class,
        CSPacketModuleListResponse.class,
        Side.SERVER
    );

    registry.register(
        CSPacketInteractionMouseWheel.class,
        CSPacketInteractionMouseWheel.class,
        Side.SERVER
    );

    registry.register(
        SCPacketNoHunger.class,
        SCPacketNoHunger.class,
        Side.CLIENT
    );

    // Tile Data
    registry.register(
        SCPacketTileData.class,
        SCPacketTileData.class,
        Side.CLIENT
    );
  }

  private PacketInitializer() {
    //
  }
}
