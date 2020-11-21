package com.codetaylor.mc.pyrotech.modules.core.block.spi;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public abstract class BlockWallBase
    extends Block {

  public static final PropertyBool UP = PropertyBool.create("up");
  public static final PropertyBool NORTH = PropertyBool.create("north");
  public static final PropertyBool EAST = PropertyBool.create("east");
  public static final PropertyBool SOUTH = PropertyBool.create("south");
  public static final PropertyBool WEST = PropertyBool.create("west");
  protected static final AxisAlignedBB[] AABB_BY_INDEX = new AxisAlignedBB[]{
      new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D),
      new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 1.0D),
      new AxisAlignedBB(0.0D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D),
      new AxisAlignedBB(0.0D, 0.0D, 0.25D, 0.75D, 1.0D, 1.0D),
      new AxisAlignedBB(0.25D, 0.0D, 0.0D, 0.75D, 1.0D, 0.75D),
      new AxisAlignedBB(0.3125D, 0.0D, 0.0D, 0.6875D, 0.875D, 1.0D),
      new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.75D, 1.0D, 0.75D),
      new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.75D, 1.0D, 1.0D),
      new AxisAlignedBB(0.25D, 0.0D, 0.25D, 1.0D, 1.0D, 0.75D),
      new AxisAlignedBB(0.25D, 0.0D, 0.25D, 1.0D, 1.0D, 1.0D),
      new AxisAlignedBB(0.0D, 0.0D, 0.3125D, 1.0D, 0.875D, 0.6875D),
      new AxisAlignedBB(0.0D, 0.0D, 0.25D, 1.0D, 1.0D, 1.0D),
      new AxisAlignedBB(0.25D, 0.0D, 0.0D, 1.0D, 1.0D, 0.75D),
      new AxisAlignedBB(0.25D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D),
      new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.75D),
      new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)
  };

  protected static final AxisAlignedBB[] CLIP_AABB_BY_INDEX = new AxisAlignedBB[]{
      AABB_BY_INDEX[0].setMaxY(1.5D),
      AABB_BY_INDEX[1].setMaxY(1.5D),
      AABB_BY_INDEX[2].setMaxY(1.5D),
      AABB_BY_INDEX[3].setMaxY(1.5D),
      AABB_BY_INDEX[4].setMaxY(1.5D),
      AABB_BY_INDEX[5].setMaxY(1.5D),
      AABB_BY_INDEX[6].setMaxY(1.5D),
      AABB_BY_INDEX[7].setMaxY(1.5D),
      AABB_BY_INDEX[8].setMaxY(1.5D),
      AABB_BY_INDEX[9].setMaxY(1.5D),
      AABB_BY_INDEX[10].setMaxY(1.5D),
      AABB_BY_INDEX[11].setMaxY(1.5D),
      AABB_BY_INDEX[12].setMaxY(1.5D),
      AABB_BY_INDEX[13].setMaxY(1.5D),
      AABB_BY_INDEX[14].setMaxY(1.5D),
      AABB_BY_INDEX[15].setMaxY(1.5D)
  };

  public BlockWallBase(Material material) {

    super(material);
    this.setDefaultState(this.blockState.getBaseState().withProperty(UP, false).withProperty(NORTH, false).withProperty(EAST, false).withProperty(SOUTH, false).withProperty(WEST, false));
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  @Override
  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {

    state = this.getActualState(state, source, pos);
    return AABB_BY_INDEX[getAABBIndex(state)];
  }

  @ParametersAreNonnullByDefault
  @Override
  public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState) {

    if (!isActualState) {
      state = this.getActualState(state, world, pos);
    }

    addCollisionBoxToList(pos, entityBox, collidingBoxes, CLIP_AABB_BY_INDEX[getAABBIndex(state)]);
  }

  @Nullable
  @ParametersAreNonnullByDefault
  @Override
  public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess world, BlockPos pos) {

    blockState = this.getActualState(blockState, world, pos);
    return CLIP_AABB_BY_INDEX[getAABBIndex(blockState)];
  }

  private static int getAABBIndex(IBlockState state) {

    int i = 0;

    if (state.getValue(NORTH)) {
      i |= 1 << EnumFacing.NORTH.getHorizontalIndex();
    }

    if (state.getValue(EAST)) {
      i |= 1 << EnumFacing.EAST.getHorizontalIndex();
    }

    if (state.getValue(SOUTH)) {
      i |= 1 << EnumFacing.SOUTH.getHorizontalIndex();
    }

    if (state.getValue(WEST)) {
      i |= 1 << EnumFacing.WEST.getHorizontalIndex();
    }

    return i;
  }

  @Override
  public boolean isFullCube(@Nonnull IBlockState state) {

    return false;
  }

  @ParametersAreNonnullByDefault
  @Override
  public boolean isPassable(IBlockAccess world, BlockPos pos) {

    return false;
  }

  @Override
  public boolean isOpaqueCube(@Nonnull IBlockState state) {

    return false;
  }

  private boolean canConnectTo(IBlockAccess worldIn, BlockPos pos, EnumFacing p_176253_3_) {

    IBlockState iblockstate = worldIn.getBlockState(pos);
    Block block = iblockstate.getBlock();
    BlockFaceShape blockfaceshape = iblockstate.getBlockFaceShape(worldIn, pos, p_176253_3_);
    boolean flag = blockfaceshape == BlockFaceShape.MIDDLE_POLE_THICK || blockfaceshape == BlockFaceShape.MIDDLE_POLE && block instanceof BlockFenceGate;
    return !isExcepBlockForAttachWithPiston(block) && blockfaceshape == BlockFaceShape.SOLID || flag;
  }

  protected static boolean isExcepBlockForAttachWithPiston(Block block) {

    return Block.isExceptBlockForAttachWithPiston(block) || block == Blocks.BARRIER || block == Blocks.MELON_BLOCK || block == Blocks.PUMPKIN || block == Blocks.LIT_PUMPKIN;
  }

  @ParametersAreNonnullByDefault
  @Override
  @SideOnly(Side.CLIENT)
  public boolean shouldSideBeRendered(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {

    return (side != EnumFacing.DOWN) || super.shouldSideBeRendered(blockState, blockAccess, pos, side);
  }

  @ParametersAreNonnullByDefault
  @Nonnull
  @Override
  public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {

    boolean flag = canWallConnectTo(world, pos, EnumFacing.NORTH);
    boolean flag1 = canWallConnectTo(world, pos, EnumFacing.EAST);
    boolean flag2 = canWallConnectTo(world, pos, EnumFacing.SOUTH);
    boolean flag3 = canWallConnectTo(world, pos, EnumFacing.WEST);
    boolean flag4 = flag && !flag1 && flag2 && !flag3 || !flag && flag1 && !flag2 && flag3;
    return state.withProperty(UP, !flag4 || !world.isAirBlock(pos.up())).withProperty(NORTH, flag).withProperty(EAST, flag1).withProperty(SOUTH, flag2).withProperty(WEST, flag3);
  }

  @Nonnull
  @Override
  protected BlockStateContainer createBlockState() {

    return new BlockStateContainer(this, UP, NORTH, EAST, WEST, SOUTH);
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  @Override
  public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {

    return face != EnumFacing.UP && face != EnumFacing.DOWN ? BlockFaceShape.MIDDLE_POLE_THICK : BlockFaceShape.CENTER_BIG;
  }

  @ParametersAreNonnullByDefault
  @Override
  public boolean canBeConnectedTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {

    return canConnectTo(world, pos.offset(facing), facing.getOpposite());
  }

  private boolean canWallConnectTo(IBlockAccess world, BlockPos pos, EnumFacing facing) {

    BlockPos other = pos.offset(facing);
    Block block = world.getBlockState(other).getBlock();
    return block.canBeConnectedTo(world, other, facing.getOpposite()) || canConnectTo(world, other, facing.getOpposite());
  }

  @Nonnull
  @Override
  public IBlockState getStateFromMeta(int meta) {

    return this.getDefaultState();
  }

  @Override
  public int getMetaFromState(@Nonnull IBlockState state) {

    return 0;
  }
}