package com.codetaylor.mc.pyrotech.modules.tech.machine.block.spi;

import com.codetaylor.mc.athenaeum.util.Properties;
import com.codetaylor.mc.pyrotech.library.util.Util;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Random;

public abstract class BlockOvenBase
    extends BlockCombustionWorkerStoneBase {

  @SuppressWarnings("deprecation")
  @ParametersAreNonnullByDefault
  @Override
  public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState) {

    EnumFacing facing = state.getValue(Properties.FACING_HORIZONTAL);

    switch (facing) {
      case NORTH:
        addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(7.0 / 16.0, 0, 9.0 / 16.0, 14.0 / 16.0, 1, 14.0 / 16.0));
        break;

      case SOUTH:
        addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(2.0 / 16.0, 0, 2.0 / 16.0, 7.0 / 16.0, 1, 9.0 / 16.0));
        break;

      case EAST:
        addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(2.0 / 16.0, 0, 7.0 / 16.0, 7.0 / 16.0, 1, 14.0 / 16.0));
        break;

      case WEST:
        addCollisionBoxToList(pos, entityBox, collidingBoxes, new AxisAlignedBB(9.0 / 16.0, 0, 2.0 / 16.0, 14.0 / 16.0, 1, 9.0 / 16.0));
        break;
    }

    super.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entity, isActualState);
  }

  @Override
  protected void randomDisplayTickActiveTop(IBlockState state, World world, BlockPos pos, Random rand) {

    double centerX = pos.getX();
    double centerY = pos.getY() + 1;
    double centerZ = pos.getZ();

    EnumFacing facing = state.getValue(Properties.FACING_HORIZONTAL);

    switch (facing) {

      case NORTH:
        centerX += 10.5 / 16.0;
        centerZ += 11.5 / 16.0;
        break;

      case SOUTH:
        centerX += 4.5 / 16.0;
        centerZ += 5.5 / 16.0;
        break;

      case EAST:
        centerX += 4.5 / 16.0;
        centerZ += 10.5 / 16.0;
        break;

      case WEST:
        centerX += 11.5 / 16.0;
        centerZ += 5.5 / 16.0;
        break;

    }

    world.spawnParticle(
        EnumParticleTypes.SMOKE_LARGE,
        centerX + (Util.RANDOM.nextDouble() - 0.5) * 0.25,
        centerY,
        centerZ + (Util.RANDOM.nextDouble() - 0.5) * 0.25,
        0,
        0.05 + (Util.RANDOM.nextFloat() * 2 - 1) * 0.05,
        0
    );
  }
}
