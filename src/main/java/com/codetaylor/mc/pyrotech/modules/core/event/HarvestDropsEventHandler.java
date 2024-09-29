package com.codetaylor.mc.pyrotech.modules.core.event;

import com.codetaylor.mc.athenaeum.util.RandomHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class HarvestDropsEventHandler {

  public static class Sticks {

    @SubscribeEvent
    public void on(BlockEvent.HarvestDropsEvent event) {

      List<ItemStack> drops = event.getDrops();
      IBlockState state = event.getState();
      Block block = state.getBlock();
      BlockPos pos = event.getPos();
      World world = event.getWorld();
      EntityPlayer harvester = event.getHarvester();

      if (block instanceof BlockLeaves) {
        if (harvester == null) {

          // lower chance for sticks if not broken by player
          if (RandomHelper.random().nextFloat() < 0.1) {
            drops.add(new ItemStack(Items.STICK));
          }
        } else {

          if (RandomHelper.random().nextFloat() < 0.5) {
            drops.add(new ItemStack(Items.STICK));
          }
        }
      }
    }
  }

  private HarvestDropsEventHandler() {
    //
  }
}
