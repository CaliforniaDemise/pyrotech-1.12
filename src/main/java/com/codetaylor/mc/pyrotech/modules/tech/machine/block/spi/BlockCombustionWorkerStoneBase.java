package com.codetaylor.mc.pyrotech.modules.tech.machine.block.spi;

import com.codetaylor.mc.athenaeum.interaction.spi.IBlockInteractable;
import com.codetaylor.mc.athenaeum.interaction.spi.IInteraction;
import com.codetaylor.mc.athenaeum.spi.IVariant;
import com.codetaylor.mc.athenaeum.util.Properties;
import com.codetaylor.mc.athenaeum.util.StackHelper;
import com.codetaylor.mc.pyrotech.library.spi.block.IBlockIgnitableAdjacentIgniterBlock;
import com.codetaylor.mc.pyrotech.library.spi.block.IBlockIgnitableWithIgniterItem;
import com.codetaylor.mc.pyrotech.library.spi.tile.ITileContainer;
import com.codetaylor.mc.pyrotech.library.spi.tile.TileCombustionWorkerBase;
import com.codetaylor.mc.pyrotech.modules.ignition.item.ItemIgniterBase;
import com.codetaylor.mc.pyrotech.modules.tech.machine.ModuleTechMachineConfig;
import com.codetaylor.mc.pyrotech.modules.tech.machine.tile.spi.TileCombustionWorkerStoneBase;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Stream;

public abstract class BlockCombustionWorkerStoneBase
    extends Block
    implements IBlockInteractable,
    IBlockIgnitableAdjacentIgniterBlock,
    IBlockIgnitableWithIgniterItem {

  public static final PropertyEnum<EnumType> TYPE = PropertyEnum.create("type", EnumType.class);

  private static final AxisAlignedBB AABB_TOP = new AxisAlignedBB(1.0 / 16.0, 0.0 / 16.0, 1.0 / 16.0, 15.0 / 16.0, 8.0 / 16.0, 15.0 / 16.0);

  public BlockCombustionWorkerStoneBase() {

    super(Material.ROCK);
    this.setSoundType(SoundType.STONE);
    this.setHardness(2);
    this.setResistance(15);
    this.setHarvestLevel("pickaxe", 0);
    this.setDefaultState(this.blockState.getBaseState()
        .withProperty(Properties.FACING_HORIZONTAL, EnumFacing.NORTH)
        .withProperty(TYPE, BlockCombustionWorkerStoneBase.EnumType.BOTTOM)
    );
  }

  // ---------------------------------------------------------------------------
  // - Ignition
  // ---------------------------------------------------------------------------

  @Override
  public void igniteWithAdjacentIgniterBlock(World world, BlockPos pos, IBlockState blockState, EnumFacing facing) {

    TileEntity tileEntity = world.getTileEntity(pos);

    if (tileEntity instanceof TileCombustionWorkerStoneBase) {
      ((TileCombustionWorkerStoneBase<?>) tileEntity).workerSetActive(true);
    }
  }

  @Override
  public void igniteWithIgniterItem(World world, BlockPos pos, IBlockState blockState, EnumFacing facing) {

    if (!this.isTop(blockState)
        && blockState.getValue(Properties.FACING_HORIZONTAL) == facing) {

      TileEntity tileEntity = world.getTileEntity(pos);

      if (tileEntity instanceof TileCombustionWorkerStoneBase) {
        ((TileCombustionWorkerStoneBase<?>) tileEntity).workerSetActive(true);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // - Light
  // ---------------------------------------------------------------------------

  @Override
  public int getLightValue(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {

    if (state.getValue(TYPE) == BlockCombustionWorkerStoneBase.EnumType.BOTTOM_LIT) {
      return MathHelper.clamp(ModuleTechMachineConfig.GENERAL.STONE_MACHINE_LIGHT_LEVEL, 0, 15);
    }

    return super.getLightValue(state, world, pos);
  }

  // ---------------------------------------------------------------------------
  // - Accessors
  // ---------------------------------------------------------------------------

  public boolean isTop(IBlockState state) {

    return state.getValue(TYPE) == BlockCombustionWorkerStoneBase.EnumType.TOP;
  }

  // ---------------------------------------------------------------------------
  // - Spatial
  // ---------------------------------------------------------------------------

  @SuppressWarnings("deprecation")
  @Nonnull
  @ParametersAreNonnullByDefault
  @Override
  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {

    if (this.isTop(state)) {
      return AABB_TOP;
    }

    return super.getBoundingBox(state, source, pos);
  }

  @SuppressWarnings("deprecation")
  @ParametersAreNonnullByDefault
  @Override
  public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing face) {

    if (this.isTop(state)) {

      return false;

    } else {

      if (face == EnumFacing.UP
          || face == state.getValue(Properties.FACING_HORIZONTAL)) {
        return false;
      }

      return this.isFullBlock(state);
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean isFullBlock(@Nonnull IBlockState state) {

    return false;
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean isFullCube(@Nonnull IBlockState state) {

    return this.isFullBlock(state);
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean isOpaqueCube(@Nonnull IBlockState state) {

    return this.isFullBlock(state);
  }

  @ParametersAreNonnullByDefault
  @Override
  public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {

    return this.isFullBlock(state);
  }

  @SuppressWarnings("deprecation")
  @Nonnull
  @ParametersAreNonnullByDefault
  @Override
  public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {

    return BlockFaceShape.UNDEFINED;
  }

  // ---------------------------------------------------------------------------
  // - Fire
  // ---------------------------------------------------------------------------

  @ParametersAreNonnullByDefault
  @Override
  public boolean isFireSource(World world, BlockPos pos, EnumFacing side) {

    return false;
  }

  @ParametersAreNonnullByDefault
  @Override
  public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {

    return 0;
  }

  // ---------------------------------------------------------------------------
  // - Tile Entity
  // ---------------------------------------------------------------------------

  @Override
  public boolean hasTileEntity(@Nonnull IBlockState state) {

    return true;
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {

    if (this.isTop(state)) {
      return this.createTileEntityTop();

    } else {
      return this.createTileEntityBottom();
    }
  }

  protected abstract TileEntity createTileEntityTop();

  protected abstract TileEntity createTileEntityBottom();

  // ---------------------------------------------------------------------------
  // - Interaction
  // ---------------------------------------------------------------------------

  @ParametersAreNonnullByDefault
  @Override
  public int quantityDropped(IBlockState state, int fortune, Random random) {

    if (this.isTop(state)) {
      return 0;
    }

    return super.quantityDropped(state, fortune, random);
  }

  @SuppressWarnings("deprecation")
  @Nullable
  @ParametersAreNonnullByDefault
  @Override
  public RayTraceResult collisionRayTrace(IBlockState blockState, World world, BlockPos pos, Vec3d start, Vec3d end) {

    RayTraceResult result = super.collisionRayTrace(blockState, world, pos, start, end);

    if (this.isTop(blockState)) {
      return this.interactionRayTrace(result, blockState, world, pos.down(), start, end);

    } else {
      return this.interactionRayTrace(result, blockState, world, pos, start, end);
    }
  }

  @ParametersAreNonnullByDefault
  @Override
  public boolean onBlockActivated(
      World world,
      BlockPos pos,
      IBlockState state,
      EntityPlayer player,
      EnumHand hand,
      EnumFacing facing,
      float hitX,
      float hitY,
      float hitZ
  ) {

    if (!this.isTop(state)) {
      ItemStack heldItem = player.getHeldItemMainhand();

      if (heldItem.getItem() instanceof ItemIgniterBase) {
        return false;
      }
    }

    return this.interact(IInteraction.EnumType.MouseClick, world, pos, state, player, hand, facing, hitX, hitY, hitZ);
  }

  @Override
  public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {

    if (this.isTop(state)) {

      BlockPos down = pos.down();

      if (world.getBlockState(down).getBlock() == this) {
        TileEntity tileEntity = world.getTileEntity(down);

        if (tileEntity instanceof ITileContainer) {
          ITileContainer tile = (ITileContainer) tileEntity;
          tile.dropContents();
        }

        StackHelper.spawnStackOnTop(world, new ItemStack(this), down);
        world.setBlockToAir(down);
      }

    } else {

      BlockPos up = pos.up();

      if (world.getBlockState(up).getBlock() == this) {
        world.setBlockToAir(up);
      }

      TileEntity tileEntity = world.getTileEntity(pos);

      if (tileEntity instanceof ITileContainer) {
        ITileContainer tile = (ITileContainer) tileEntity;
        tile.dropContents();
      }
    }

    super.breakBlock(world, pos, state);
  }

  @ParametersAreNonnullByDefault
  @Override
  public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {

    if (!this.isTop(state)) {

      BlockPos up = pos.up();

      if (super.canPlaceBlockAt(world, up)) {
        EnumFacing facing = state.getValue(Properties.FACING_HORIZONTAL);
        world.setBlockState(up, this.getDefaultState()
            .withProperty(Properties.FACING_HORIZONTAL, facing)
            .withProperty(TYPE, BlockCombustionWorkerStoneBase.EnumType.TOP));
      }
    }
  }

  @ParametersAreNonnullByDefault
  @Override
  public boolean canSilkHarvest(World world, BlockPos pos, @Nonnull IBlockState state, EntityPlayer player) {

    return false;
  }

  @ParametersAreNonnullByDefault
  @Override
  public boolean canPlaceBlockAt(World world, BlockPos pos) {

    return super.canPlaceBlockAt(world, pos)
        && super.canPlaceBlockAt(world, pos.up());
  }

  @Override
  public boolean interact(IInteraction.EnumType type, World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

    if (this.isTop(state)) {
      return IBlockInteractable.super.interact(type, world, pos.down(), state, player, hand, facing, hitX, hitY + 1, hitZ);
    }

    return IBlockInteractable.super.interact(type, world, pos, state, player, hand, facing, hitX, hitY, hitZ);
  }

  // ---------------------------------------------------------------------------
  // - Rendering
  // ---------------------------------------------------------------------------

  @Nonnull
  @SideOnly(Side.CLIENT)
  public BlockRenderLayer getBlockLayer() {

    return BlockRenderLayer.SOLID;
  }

  @SideOnly(Side.CLIENT)
  @ParametersAreNonnullByDefault
  @Override
  public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {

    if (this.isTop(state)) {

      TileEntity tileEntity = world.getTileEntity(pos.down());

      if (tileEntity instanceof TileCombustionWorkerStoneBase) {
        TileCombustionWorkerStoneBase<?> tileCombustionWorkerStoneBase = (TileCombustionWorkerStoneBase<?>) tileEntity;

        if (tileCombustionWorkerStoneBase.workerIsActive()) {

          this.randomDisplayTickActiveTop(state, world, pos, rand);

          if (tileCombustionWorkerStoneBase.hasFuel()
              && tileCombustionWorkerStoneBase.hasInput()) {
            this.randomDisplayTickWorkingTop(state, world, pos, rand);
          }
        }
      }

    } else {

      TileEntity tileEntity = world.getTileEntity(pos);

      if (tileEntity instanceof TileCombustionWorkerBase
          && ((TileCombustionWorkerBase) tileEntity).workerIsActive()) {

        EnumFacing enumfacing = state.getValue(Properties.FACING_HORIZONTAL);
        double d0 = (double) pos.getX() + 0.5;
        double d1 = (double) pos.getY() + rand.nextDouble() * 6.0 / 16.0;
        double d2 = (double) pos.getZ() + 0.5;
        double d4 = rand.nextDouble() * 0.6 - 0.3;

        if (rand.nextDouble() < 0.1) {
          world.playSound((double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        }

        double offset = 0.25;

        switch (enumfacing) {
          case WEST:
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 - offset, d1, d2 + d4, 0, 0, 0);
            world.spawnParticle(EnumParticleTypes.FLAME, d0 - offset, d1, d2 + d4, 0, 0, 0);
            break;
          case EAST:
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + offset, d1, d2 + d4, 0, 0, 0);
            world.spawnParticle(EnumParticleTypes.FLAME, d0 + offset, d1, d2 + d4, 0, 0, 0);
            break;
          case NORTH:
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 - offset, 0, 0, 0);
            world.spawnParticle(EnumParticleTypes.FLAME, d0 + d4, d1, d2 - offset, 0, 0, 0);
            break;
          case SOUTH:
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4, d1, d2 + offset, 0, 0, 0);
            world.spawnParticle(EnumParticleTypes.FLAME, d0 + d4, d1, d2 + offset, 0, 0, 0);
        }
      }
    }
  }

  /**
   * Called when working on a recipe.
   */
  protected void randomDisplayTickWorkingTop(IBlockState state, World world, BlockPos pos, Random rand) {
    //
  }

  /**
   * Called when lit.
   */
  protected void randomDisplayTickActiveTop(IBlockState state, World world, BlockPos pos, Random rand) {
    //
  }

  // ---------------------------------------------------------------------------
  // - Variants
  // ---------------------------------------------------------------------------

  @Nonnull
  @Override
  protected BlockStateContainer createBlockState() {

    return new BlockStateContainer(this, Properties.FACING_HORIZONTAL, TYPE);
  }

  @SuppressWarnings("deprecation")
  @Nonnull
  @Override
  public IBlockState getStateFromMeta(int meta) {

    // N, S, W, E
    // 0, 1, 2, 3 = facing, no top
    // 4, 5, 6, 7 = facing, top

    int type = ((meta >> 2) & 3);
    int facingIndex = (meta & 3) + 2;

    return this.getDefaultState()
        .withProperty(Properties.FACING_HORIZONTAL, EnumFacing.VALUES[facingIndex])
        .withProperty(TYPE, BlockCombustionWorkerStoneBase.EnumType.fromMeta(type));
  }

  @Override
  public int getMetaFromState(IBlockState state) {

    EnumFacing facing = state.getValue(Properties.FACING_HORIZONTAL);
    EnumType type = state.getValue(TYPE);

    int meta = facing.getIndex() - 2;
    meta |= (type.getMeta() << 2);
    return meta;
  }

  @Nonnull
  @ParametersAreNonnullByDefault
  @Override
  public IBlockState getStateForPlacement(
      World world,
      BlockPos pos,
      EnumFacing facing,
      float hitX,
      float hitY,
      float hitZ,
      int meta,
      EntityLivingBase placer,
      EnumHand hand
  ) {

    EnumFacing opposite = placer.getHorizontalFacing().getOpposite();
    return this.getDefaultState().withProperty(Properties.FACING_HORIZONTAL, opposite);
  }

  @SuppressWarnings("deprecation")
  @Nonnull
  @ParametersAreNonnullByDefault
  @Override
  public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {

    if (state.getValue(TYPE) == BlockCombustionWorkerStoneBase.EnumType.BOTTOM) {

      TileEntity tileEntity = world.getTileEntity(pos);

      if (tileEntity instanceof TileCombustionWorkerBase
          && ((TileCombustionWorkerBase) tileEntity).combustionGetBurnTimeRemaining() > 0) {

        return state.withProperty(TYPE, BlockCombustionWorkerStoneBase.EnumType.BOTTOM_DORMANT);
      }
    }

    return state;
  }

  public enum EnumType
      implements IVariant {

    TOP(0, "top"),
    BOTTOM(1, "bottom"),
    BOTTOM_LIT(2, "bottom_lit"),
    BOTTOM_DORMANT(3, "bottom_dormant");

    private static final EnumType[] META_LOOKUP = Stream.of(BlockCombustionWorkerStoneBase.EnumType.values())
        .sorted(Comparator.comparing(BlockCombustionWorkerStoneBase.EnumType::getMeta))
        .toArray(EnumType[]::new);

    private final int meta;
    private final String name;

    EnumType(int meta, String name) {

      this.meta = meta;
      this.name = name;
    }

    @Override
    public int getMeta() {

      return this.meta;
    }

    @Nonnull
    @Override
    public String getName() {

      return this.name;
    }

    public static EnumType fromMeta(int meta) {

      if (meta < 0 || meta >= META_LOOKUP.length) {
        meta = 0;
      }

      return META_LOOKUP[meta];
    }
  }
}
