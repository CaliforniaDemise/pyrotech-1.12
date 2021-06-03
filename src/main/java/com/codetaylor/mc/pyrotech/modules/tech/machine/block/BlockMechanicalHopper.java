package com.codetaylor.mc.pyrotech.modules.tech.machine.block;

import com.codetaylor.mc.athenaeum.interaction.spi.IBlockInteractable;
import com.codetaylor.mc.athenaeum.interaction.spi.IInteraction;
import com.codetaylor.mc.athenaeum.spi.BlockPartialBase;
import com.codetaylor.mc.athenaeum.spi.IVariant;
import com.codetaylor.mc.athenaeum.util.AABBHelper;
import com.codetaylor.mc.athenaeum.util.Properties;
import com.codetaylor.mc.athenaeum.util.StackHelper;
import com.codetaylor.mc.pyrotech.modules.tech.machine.tile.TileStoneHopper;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlockMechanicalHopper
    extends BlockPartialBase
    implements IBlockInteractable {

  public static final String NAME = "mechanical_hopper";

  public static final PropertyEnum<EnumType> TYPE = PropertyEnum.create("type", EnumType.class, EnumType.Down, EnumType.Side);

  private static final Map<EnumType, Map<EnumFacing, List<AxisAlignedBB>>> RAYTRACE_COLLISION_BOUNDS;

  static {
    List<AxisAlignedBB> shared = ImmutableList.of(
        AABBHelper.create(0, 10, 0, 16, 16, 16),
        AABBHelper.create(4, 4, 4, 12, 10, 12)
    );

    RAYTRACE_COLLISION_BOUNDS = Stream.of(EnumType.values())
        .collect(Collectors.toMap(type -> type, type -> {

          Map<EnumFacing, List<AxisAlignedBB>> result;

          result = Stream.of(EnumFacing.values())
              .filter(t -> t != EnumFacing.UP)
              .collect(Collectors.toMap(aa -> aa, aa -> new ArrayList<>(shared), (u, v) -> {
                throw new IllegalStateException();
              }, () -> new EnumMap<>(EnumFacing.class)));

          if (type == EnumType.Down
              || type == EnumType.DownWithCog) {

            AxisAlignedBB downAABB = AABBHelper.create(6, 0, 6, 10, 4, 10);
            result.get(EnumFacing.NORTH).add(downAABB);
            result.get(EnumFacing.SOUTH).add(downAABB);
            result.get(EnumFacing.EAST).add(downAABB);
            result.get(EnumFacing.WEST).add(downAABB);

            if (type == EnumType.DownWithCog) {
              result.get(EnumFacing.NORTH).add(AABBHelper.create(2, 1, 0, 14, 13, 4));
              result.get(EnumFacing.SOUTH).add(AABBHelper.create(2, 1, 12, 14, 13, 16));
              result.get(EnumFacing.EAST).add(AABBHelper.create(12, 1, 2, 16, 13, 14));
              result.get(EnumFacing.WEST).add(AABBHelper.create(0, 1, 2, 4, 13, 14));
            }

          } else {
            result.get(EnumFacing.SOUTH).add(AABBHelper.create(6, 4, 0, 10, 8, 4));
            result.get(EnumFacing.NORTH).add(AABBHelper.create(6, 4, 12, 10, 8, 16));
            result.get(EnumFacing.WEST).add(AABBHelper.create(12, 4, 6, 16, 8, 10));
            result.get(EnumFacing.EAST).add(AABBHelper.create(0, 4, 6, 4, 8, 10));

            if (type == EnumType.SideWithCog) {
              result.get(EnumFacing.NORTH).add(AABBHelper.create(2, 1, 0, 14, 13, 4));
              result.get(EnumFacing.SOUTH).add(AABBHelper.create(2, 1, 12, 14, 13, 16));
              result.get(EnumFacing.EAST).add(AABBHelper.create(12, 1, 2, 16, 13, 14));
              result.get(EnumFacing.WEST).add(AABBHelper.create(0, 1, 2, 4, 13, 14));
            }
          }

          return result;

        }, (u, v) -> {
          throw new IllegalStateException();
        }, () -> new EnumMap<>(EnumType.class)));
  }

  public BlockMechanicalHopper() {

    super(Material.ROCK);
    this.setHardness(2);
    this.setHarvestLevel("pickaxe", 0);
    this.setDefaultState(this.blockState.getBaseState()
        .withProperty(Properties.FACING_HORIZONTAL, EnumFacing.NORTH)
        .withProperty(TYPE, EnumType.Down));
  }

  // ---------------------------------------------------------------------------
  // - Interaction
  // ---------------------------------------------------------------------------

  @SuppressWarnings("deprecation")
  @ParametersAreNonnullByDefault
  @Nullable
  @Override
  public RayTraceResult collisionRayTrace(IBlockState blockState, World world, BlockPos pos, Vec3d start, Vec3d end) {

    EnumType type = blockState.getValue(TYPE);

    TileEntity tileEntity = world.getTileEntity(pos);

    if (tileEntity instanceof TileStoneHopper) {

      if (!((TileStoneHopper) tileEntity).getCogStackHandler().getStackInSlot(0).isEmpty()) {

        if (type == EnumType.Down) {
          type = EnumType.DownWithCog;

        } else {
          type = EnumType.SideWithCog;
        }
      }
    }

    return RAYTRACE_COLLISION_BOUNDS.get(type)
        .get(blockState.getValue(Properties.FACING_HORIZONTAL))
        .stream()
        .map(bb -> rayTrace(pos, start, end, bb))
        .anyMatch(Objects::nonNull)
        ? this.interactionRayTrace(super.collisionRayTrace(blockState, world, pos, start, end), blockState, world, pos, start, end) : null;
  }

  @ParametersAreNonnullByDefault
  @Override
  public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

    return this.interact(IInteraction.EnumType.MouseClick, world, pos, state, player, hand, facing, hitX, hitY, hitZ);
  }

  @ParametersAreNonnullByDefault
  @Override
  public void breakBlock(World world, BlockPos pos, IBlockState state) {

    TileEntity tileEntity = world.getTileEntity(pos);

    if (tileEntity instanceof TileStoneHopper) {
      TileStoneHopper.CogStackHandler handler = ((TileStoneHopper) tileEntity).getCogStackHandler();
      StackHelper.spawnStackHandlerContentsOnTop(world, handler, pos);
    }

    super.breakBlock(world, pos, state);
  }

  // ---------------------------------------------------------------------------
  // - Tile Entity
  // ---------------------------------------------------------------------------

  @Override
  public boolean hasTileEntity(@Nonnull IBlockState state) {

    return true;
  }

  @Nullable
  @ParametersAreNonnullByDefault
  @Override
  public TileEntity createTileEntity(World world, IBlockState state) {

    return new TileStoneHopper();
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
    // 0, 1, 2, 3 = facing, down
    // 4, 5, 6, 7 = facing, side

    int type = ((meta >> 2) & 3);
    int facingIndex = (meta & 3) + 2;

    return this.getDefaultState()
        .withProperty(Properties.FACING_HORIZONTAL, EnumFacing.VALUES[facingIndex])
        .withProperty(TYPE, EnumType.fromMeta(type));
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

    EnumType type;

    if (facing.getHorizontalIndex() == -1) {
      type = EnumType.Down;
      facing = placer.getHorizontalFacing().getOpposite();

    } else {
      type = EnumType.Side;
    }

    return this.getDefaultState()
        .withProperty(Properties.FACING_HORIZONTAL, facing)
        .withProperty(TYPE, type);
  }

  public enum EnumType
      implements IVariant {

    Down(0, "down"),
    Side(1, "side"),
    DownWithCog(2, "down_with_cog"),
    SideWithCog(3, "side_with_cog");

    private static final EnumType[] META_LOOKUP = Stream.of(EnumType.values())
        .sorted(Comparator.comparing(EnumType::getMeta))
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
