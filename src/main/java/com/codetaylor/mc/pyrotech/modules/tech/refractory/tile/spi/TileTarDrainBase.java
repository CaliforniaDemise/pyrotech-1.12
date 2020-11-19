package com.codetaylor.mc.pyrotech.modules.tech.refractory.tile.spi;

import com.codetaylor.mc.pyrotech.library.util.FloodFill;
import com.codetaylor.mc.pyrotech.modules.tech.refractory.ModuleTechRefractory;
import com.codetaylor.mc.pyrotech.modules.tech.refractory.block.BlockTarDrain;
import com.codetaylor.mc.pyrotech.modules.tech.refractory.tile.TileActivePile;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class TileTarDrainBase
    extends TileTarTankBase {

  public TileTarDrainBase() {

    super();
    this.fluidTank.setCanFill(false);
  }

  @Override
  public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {

    if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
      EnumFacing tileFacing = this.world.getBlockState(this.pos).getValue(BlockTarDrain.FACING);

      return facing == tileFacing
          || facing == tileFacing.getOpposite();
    }

    return false;
  }

  @Nullable
  @Override
  public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {

    if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
      EnumFacing tileFacing = this.world.getBlockState(this.pos).getValue(BlockTarDrain.FACING);

      if (facing == tileFacing
          || facing == tileFacing.getOpposite()) {
        //noinspection unchecked
        return (T) this.fluidTank;
      }
    }

    return null;
  }

  @Override
  protected Set<BlockPos> getCollectionSourcePositions(World world, BlockPos origin) {

    IBlockState blockState = world.getBlockState(origin);

    if (blockState.getBlock() != ModuleTechRefractory.Blocks.TAR_DRAIN) {
      return Collections.emptySet();
    }

    EnumFacing facing = blockState.getValue(BlockTarDrain.FACING).getOpposite();
    int drainRange = this.getDrainRange();
    BlockPos offset = origin.offset(facing, 1 + drainRange);
    Set<BlockPos> eligiblePos = new HashSet<>(9);
    Set<BlockPos> result = new HashSet<>(9);

    for (int x = -drainRange; x <= drainRange; x++) {

      for (int z = -drainRange; z <= drainRange; z++) {
        eligiblePos.add(offset.add(x, 0, z));
      }
    }

    BlockPos start = origin.offset(facing);

    FloodFill.ICandidatePredicate candidatePredicate = (w, p) -> eligiblePos.contains(p) && this.getCollectionSourceFluidTank(w.getTileEntity(p)) != null;
    FloodFill.IAction action = (w, p) -> result.add(p);
    FloodFill.apply(world, start, candidatePredicate, action, eligiblePos.size());

    return result;
  }

  protected abstract int getDrainRange();

  @Nullable
  @Override
  protected FluidTank getCollectionSourceFluidTank(@Nullable TileEntity tileEntity) {

    if (tileEntity instanceof TileTarCollectorBase) {
      return ((TileTarCollectorBase) tileEntity).getFluidTank();

    } else if (tileEntity instanceof TileActivePile) {
      return ((TileActivePile) tileEntity).getFluidTank();
    }

    return null;
  }

}
