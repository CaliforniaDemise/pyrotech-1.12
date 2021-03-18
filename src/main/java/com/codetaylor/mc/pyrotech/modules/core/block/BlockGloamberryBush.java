package com.codetaylor.mc.pyrotech.modules.core.block;

import com.codetaylor.mc.athenaeum.util.RandomHelper;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCoreConfig;
import com.codetaylor.mc.pyrotech.modules.core.block.spi.BlockBushBase;
import com.codetaylor.mc.pyrotech.modules.core.network.SCPacketParticleCombust;
import com.codetaylor.mc.pyrotech.modules.core.network.SCPacketParticleGloamberry;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Random;

public class BlockGloamberryBush
    extends BlockBushBase {

  public static final String NAME = "gloamberry_bush";

  // ---------------------------------------------------------------------------
  // - Implementation
  // ---------------------------------------------------------------------------

  @Override
  public boolean isValidBlock(IBlockState blockState) {

    return (blockState.getMaterial() == Material.GROUND);
  }

  @Override
  public Item getSeedItem() {

    return ModuleCore.Items.GLOAMBERRY_SEEDS;
  }

  // ---------------------------------------------------------------------------
  // - Interaction
  // ---------------------------------------------------------------------------

  @ParametersAreNonnullByDefault
  @Override
  public void breakBlock(World world, BlockPos pos, IBlockState blockState) {

    super.breakBlock(world, pos, blockState);

    if (!world.isRemote) {

      switch (this.getAge(blockState)) {
        case 0:
        case 1:
        case 2:
        case 3:
          break;
        case 4:
        case 5:
        case 6:
        case 7:
        default:
          this.spawnParticles(pos, world.provider.getDimension());
          this.playSound(world, pos);
      }

    }

    if (this.isMaxAge(blockState)) {
      world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.5f, true);
    }
  }

  @ParametersAreNonnullByDefault
  @Override
  public boolean onBlockActivated(World world, BlockPos pos, IBlockState blockState, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

    if (hand == EnumHand.MAIN_HAND) {
      ItemStack itemStack = player.getHeldItemMainhand();
      Item item = itemStack.getItem();

      if (item == Items.DYE
          && EnumDyeColor.CYAN == EnumDyeColor.byDyeDamage(itemStack.getMetadata())) {
        int age = this.getAge(blockState);

        if (age < this.getMaxAge()
            && ForgeHooks.onCropsGrowPre(world, pos, blockState, true)) {

          if (!player.isCreative() && !player.isSpectator()) {
            itemStack.setCount(itemStack.getCount() - 1);
          }

          world.setBlockState(pos, this.withAge(age + 1), 2);
          this.playSound(world, pos);
          this.spawnParticles(pos, world.provider.getDimension());
          ForgeHooks.onCropsGrowPost(world, pos, blockState, world.getBlockState(pos));

        } else if (this.isMaxAge(blockState)) {
          this.spawnParticles(pos, world.provider.getDimension());
          world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.5f, true);
        }

        return true;

      } else if (item == Items.AIR && this.isMaxAge(blockState)) {
        this.spawnParticles(pos, world.provider.getDimension());
        world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.5f, true);
        return true;
      }
    }

    return super.onBlockActivated(world, pos, blockState, player, hand, facing, hitX, hitY, hitZ);
  }

  @ParametersAreNonnullByDefault
  @Override
  public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {

    super.onBlockClicked(world, pos, player);

    if (this.getAge(world.getBlockState(pos)) == this.getMaxAge()) {
      this.spawnParticles(pos, world.provider.getDimension());
    }
  }

  @ParametersAreNonnullByDefault
  @Override
  public void onEntityWalk(World world, BlockPos pos, Entity entity) {

    super.onEntityWalk(world, pos, entity);

    if (RandomHelper.random().nextFloat() < 0.05
        && this.getAge(world.getBlockState(pos)) == this.getMaxAge()) {
      this.spawnParticles(pos, world.provider.getDimension());
    }
  }

  private void playSound(World world, BlockPos pos) {

    world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_NOTE_PLING, SoundCategory.BLOCKS, 1, 1);
  }

  private void spawnParticles(BlockPos pos, int dimension) {

    ModuleCore.PACKET_SERVICE.sendToAllAround(new SCPacketParticleGloamberry(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0.5, 0.5, 0.5), dimension, pos);
  }

  // ---------------------------------------------------------------------------
  // - Update
  // ---------------------------------------------------------------------------

  @ParametersAreNonnullByDefault
  @Override
  public void updateTick(World world, BlockPos pos, IBlockState blockState, Random rand) {

    this.checkAndDropBlock(world, pos);

    if (world.canSeeSky(pos)) {

      if (!world.isDaytime()) {
        // Increase age if is nighttime.
        int age = this.getAge(blockState);
        boolean grew = false;

        if (age < this.getMaxAge()) {
          double chance = (age == this.getMaxAge() - 1) ? ModuleCoreConfig.GLOAMBERRY_BUSH.BERRY_GROWTH_CHANCE : ModuleCoreConfig.GLOAMBERRY_BUSH.GROWTH_CHANCE;

          if (ForgeHooks.onCropsGrowPre(world, pos, blockState, rand.nextFloat() < chance)) {
            world.setBlockState(pos, this.withAge(age + 1), 2);
            grew = true;
            ForgeHooks.onCropsGrowPost(world, pos, blockState, world.getBlockState(pos));
          }
        }

        if ((age > 2 && grew) || (this.isMaxAge(blockState) && rand.nextFloat() < 0.5)) {
          this.spawnParticles(pos, world.provider.getDimension());
          this.playSound(world, pos);
        }
      }

    } else {
      // Reduce age if the plant can't see sky.
      int age = this.getAge(blockState);

      if (age > 4) {

        if (ForgeHooks.onCropsGrowPre(world, pos, blockState, rand.nextFloat() < ModuleCoreConfig.GLOAMBERRY_BUSH.OBSTRUCTED_GROWTH_REVERT_CHANCE)) {
          world.setBlockState(pos, this.withAge(age - 1), 2);
          ForgeHooks.onCropsGrowPost(world, pos, blockState, world.getBlockState(pos));
        }
      }
    }
  }
}
