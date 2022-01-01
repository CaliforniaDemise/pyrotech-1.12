package com.codetaylor.mc.pyrotech.modules.storage.tile;

import com.codetaylor.mc.athenaeum.integration.gamestages.Stages;
import com.codetaylor.mc.athenaeum.interaction.api.Transform;
import com.codetaylor.mc.athenaeum.interaction.spi.IInteraction;
import com.codetaylor.mc.athenaeum.interaction.spi.ITileInteractable;
import com.codetaylor.mc.athenaeum.interaction.spi.InteractionItemStack;
import com.codetaylor.mc.athenaeum.inventory.LargeObservableStackHandler;
import com.codetaylor.mc.athenaeum.network.tile.data.TileDataLargeItemStackHandler;
import com.codetaylor.mc.athenaeum.network.tile.spi.ITileData;
import com.codetaylor.mc.athenaeum.network.tile.spi.ITileDataItemStackHandler;
import com.codetaylor.mc.athenaeum.network.tile.spi.TileEntityDataBase;
import com.codetaylor.mc.athenaeum.util.Properties;
import com.codetaylor.mc.pyrotech.modules.storage.ModuleStorage;
import com.codetaylor.mc.pyrotech.modules.storage.ModuleStorageConfig;
import com.codetaylor.mc.pyrotech.modules.storage.block.BlockStash;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileStash
    extends TileEntityDataBase
    implements ITileInteractable {

  private StackHandler stackHandler;

  private IInteraction[] interactions;
  private AxisAlignedBB renderBounds;

  public TileStash() {

    super(ModuleStorage.TILE_DATA_SERVICE);

    this.stackHandler = new StackHandler(this.getMaxStacks());
    this.stackHandler.addObserver((handler, slot) -> this.markDirty());

    // --- Network ---

    this.registerTileDataForNetwork(new ITileData[]{
        new TileDataLargeItemStackHandler<>(this.stackHandler)
    });

    // --- Interactions ---

    this.interactions = new IInteraction[]{
        new Interaction(new ItemStackHandler[]{this.stackHandler})
    };
  }

  @Override
  public boolean shouldRefresh(
      World world,
      BlockPos pos,
      @Nonnull IBlockState oldState,
      @Nonnull IBlockState newState
  ) {

    if (oldState.getBlock() == newState.getBlock()) {
      return false;
    }

    return super.shouldRefresh(world, pos, oldState, newState);
  }

  // ---------------------------------------------------------------------------
  // - Accessors
  // ---------------------------------------------------------------------------

  public ItemStackHandler getStackHandler() {

    return this.stackHandler;
  }

  protected int getMaxStacks() {

    return ModuleStorageConfig.STASH.MAX_STACKS;
  }

  // ---------------------------------------------------------------------------
  // - Capabilities
  // ---------------------------------------------------------------------------

  @Override
  public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {

    return this.allowAutomation()
        && (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
  }

  @Nullable
  @Override
  public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {

    if (this.allowAutomation()
        && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {

      //noinspection unchecked
      return (T) this.stackHandler;
    }

    return null;
  }

  protected boolean allowAutomation() {

    return ModuleStorageConfig.STASH.ALLOW_AUTOMATION;
  }

  // ---------------------------------------------------------------------------
  // - Serialization
  // ---------------------------------------------------------------------------

  @Nonnull
  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {

    super.writeToNBT(compound);
    compound.setTag("stackHandler", this.stackHandler.serializeNBT());
    return compound;
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {

    super.readFromNBT(compound);
    this.stackHandler.deserializeNBT(compound.getCompoundTag("stackHandler"));
  }

  // ---------------------------------------------------------------------------
  // - Rendering
  // ---------------------------------------------------------------------------

  @Nonnull
  @Override
  public AxisAlignedBB getRenderBoundingBox() {

    if (this.renderBounds == null) {
      this.renderBounds = new AxisAlignedBB(this.getPos()).expand(0, 0.5, 0);
    }

    return this.renderBounds;
  }

  @Override
  public boolean shouldRenderInPass(int pass) {

    return (pass == 0) || (pass == 1);
  }

  // ---------------------------------------------------------------------------
  // - Interactions
  // ---------------------------------------------------------------------------

  @Nullable
  @Override
  public Stages getStages() {

    return ModuleStorageConfig.STAGES_STASH;
  }

  @Override
  public EnumFacing getTileFacing(World world, BlockPos pos, IBlockState blockState) {

    if (blockState.getBlock() == ModuleStorage.Blocks.STASH
        || blockState.getBlock() == ModuleStorage.Blocks.STASH_STONE) {
      return blockState.getValue(Properties.FACING_HORIZONTAL);
    }

    return ITileInteractable.super.getTileFacing(world, pos, blockState);
  }

  @Override
  public IInteraction[] getInteractions() {

    return this.interactions;
  }

  private class Interaction
      extends InteractionItemStack<TileStash> {

    /* package */ Interaction(ItemStackHandler[] stackHandlers) {

      super(stackHandlers, 0, new EnumFacing[]{EnumFacing.UP}, BlockStash.AABB, new Transform(
          Transform.translate(0.5, 7.0 / 16.0, 0.5),
          Transform.rotate(),
          Transform.scale(0.75, 0.75, 0.75)
      ));
    }

    @Override
    protected boolean doItemStackValidation(ItemStack itemStack) {

      // TODO: config blacklist? whitelist?
      return true;
    }

    @Override
    public Vec3d getTextOffset(EnumFacing tileFacing, EnumFacing playerHorizontalFacing, EnumFacing sideHit) {

      return new Vec3d(0, 0.5, 0);
    }
  }

  // ---------------------------------------------------------------------------
  // - Stack Handlers
  // ---------------------------------------------------------------------------

  /**
   * https://github.com/codetaylor/pyrotech-1.12/issues/380
   * <p>
   * This handler has been modified to fool interrogators into thinking that it
   * has two item slots, one of which is always empty.
   * <p>
   * This is necessary to trick the injected Forge logic found in
   * {@link net.minecraftforge.items.VanillaInventoryCodeHooks#isFull(IItemHandler)}
   * into thinking that this container is not full and can accept input. The
   * hopper is then allowed to attempt an insert, which is always inserted into
   * slot 0.
   * <p>
   * This has been tested against the vanilla hopper and Pyrotech's stone hopper.
   * <p>
   * It is considered to be an experimental fix and may introduce unintended
   * side-effects with other mods.
   */
  @SuppressWarnings("JavadocReference")
  private static class StackHandler
      extends LargeObservableStackHandler
      implements ITileDataItemStackHandler {

    private final int maxStacks;

    /* protected */ StackHandler(int maxStacks) {

      super(1);
      this.maxStacks = maxStacks;
    }

    @Override
    public int getSlots() {

      return 2;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot) {

      return (slot == 1) ? ItemStack.EMPTY : super.getStackInSlot(0);
    }

    @Override
    public void setStackInSlot(int slot, @Nonnull ItemStack stack) {

      super.setStackInSlot(0, stack);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {

      return super.insertItem(0, stack, simulate);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {

      return super.extractItem(0, amount, simulate);
    }

    @Override
    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {

      return stack.getMaxStackSize() * this.maxStacks;
    }
  }

}
