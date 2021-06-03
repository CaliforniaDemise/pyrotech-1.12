package com.codetaylor.mc.pyrotech.modules.tech.machine.tile.spi;

import com.codetaylor.mc.athenaeum.interaction.api.Transform;
import com.codetaylor.mc.athenaeum.interaction.spi.IInteraction;
import com.codetaylor.mc.athenaeum.interaction.spi.ITileInteractable;
import com.codetaylor.mc.athenaeum.interaction.spi.InteractionItemStack;
import com.codetaylor.mc.athenaeum.inventory.ObservableStackHandler;
import com.codetaylor.mc.athenaeum.network.tile.ITileDataService;
import com.codetaylor.mc.athenaeum.network.tile.data.TileDataBoolean;
import com.codetaylor.mc.athenaeum.network.tile.data.TileDataItemStackHandler;
import com.codetaylor.mc.athenaeum.network.tile.spi.ITileData;
import com.codetaylor.mc.athenaeum.network.tile.spi.ITileDataItemStackHandler;
import com.codetaylor.mc.athenaeum.network.tile.spi.TileEntityDataBase;
import com.codetaylor.mc.athenaeum.util.*;
import com.codetaylor.mc.pyrotech.modules.tech.machine.ModuleTechMachineConfig;
import com.codetaylor.mc.pyrotech.modules.tech.machine.client.render.InteractionCogRenderer;
import com.codetaylor.mc.pyrotech.modules.tech.machine.tile.TileStoneHopper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public abstract class TileCogWorkerBase
    extends TileEntityDataBase
    implements ITickable,
    ITileInteractable {

  private final CogStackHandler cogStackHandler;
  private final TickCounter updateTickCounter;
  private final TileDataBoolean triggered;
  private final TileDataItemStackHandler<CogStackHandler> tileDataItemStackHandler;

  private IInteraction<?>[] interactions;
  private boolean ready;

  @SideOnly(Side.CLIENT)
  private TileCogWorkerBase.ClientRenderData clientRenderData;

  public TileCogWorkerBase(ITileDataService tileDataService) {

    super(tileDataService);

    this.updateTickCounter = new TickCounter(this.getUpdateIntervalTicks());
    this.cogStackHandler = new CogStackHandler(this);
    this.cogStackHandler.addObserver((handler, slot) -> {
      this.updateTickCounter.reset();
      this.markDirty();
    });

    this.triggered = new TileDataBoolean(false);

    // --- Network ---

    this.tileDataItemStackHandler = new TileDataItemStackHandler<>(this.cogStackHandler);

    this.registerTileDataForNetwork(new ITileData[]{
        tileDataItemStackHandler,
        this.triggered
    });

    // --- Interactions ---

    this.interactions = new IInteraction[]{
        new InteractionCog(this, this.cogStackHandler, this.getCogInteractionBounds(), this.getCogInteractionTransform())
    };
  }

  protected void addInteractions(IInteraction<?>[] interactions) {

    this.interactions = ArrayHelper.combine(this.interactions, interactions);
  }

  protected Transform getCogInteractionTransform() {

    return new Transform(
        Transform.translate(0.5, 7.0 / 16.0, 2.0 / 16.0),
        Transform.rotate(),
        Transform.scale(0.75, 0.75, 2.00)
    );
  }

  protected AxisAlignedBB getCogInteractionBounds() {

    return AABBHelper.create(0, 0, 0, 16, 16, 4);
  }

  // ---------------------------------------------------------------------------
  // - Accessors
  // ---------------------------------------------------------------------------

  @SideOnly(Side.CLIENT)
  public TileStoneHopper.ClientRenderData getClientRenderData() {

    if (this.clientRenderData == null) {
      this.clientRenderData = new TileCogWorkerBase.ClientRenderData();
    }

    return this.clientRenderData;
  }

  public CogStackHandler getCogStackHandler() {

    return this.cogStackHandler;
  }

  protected abstract boolean isValidCog(ItemStack itemStack);

  protected abstract int getUpdateIntervalTicks();

  protected ItemStack getCog() {

    return this.cogStackHandler.getStackInSlot(0);
  }

  // ---------------------------------------------------------------------------
  // - Update
  // ---------------------------------------------------------------------------

  /**
   * Perform work and return cog damage.
   *
   * @param cog the cog
   * @return cog damage
   */
  protected abstract int doWork(ItemStack cog);

  protected abstract boolean isPowered();

  @Override
  public void update() {

    if (this.world.isRemote) {
      return;
    }

    ItemStack cog = this.cogStackHandler.getStackInSlot(0);

    if (cog.isEmpty()) {
      return;
    }

    if (this.updateTickCounter != null
        && this.updateTickCounter.increment()) {
      this.ready = true;
    }

    if (this.isPowered()) {
      return;
    }

    if (this.ready) {
      //noinspection ConstantConditions
      this.updateTickCounter.reset();
      this.ready = false;
      int cogDamage = this.doWork(cog);

      if (cogDamage >= 0) {

        // --- Damage Cog

        ItemStack actualCog = this.cogStackHandler.extractItem(0, 1, false);

        if (!ModuleTechMachineConfig.isCogIndestructible(actualCog.getItem())
            && (actualCog.attemptDamageItem(cogDamage, RandomHelper.random(), null)
            || actualCog.getItemDamage() == actualCog.getMaxDamage())) {

          SoundHelper.playSoundServer(this.world, this.pos, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS);

        } else {
          this.cogStackHandler.insertItem(0, actualCog, false);
        }

        this.triggered.set(true);
        return;
      }
    }

    this.triggered.set(false);
  }

  // ---------------------------------------------------------------------------
  // - Serialization
  // ---------------------------------------------------------------------------

  @Nonnull
  @Override
  public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {

    super.writeToNBT(compound);
    compound.setTag("cogStackHandler", this.cogStackHandler.serializeNBT());
    return compound;
  }

  @Override
  public void readFromNBT(@Nonnull NBTTagCompound compound) {

    super.readFromNBT(compound);
    this.cogStackHandler.deserializeNBT(compound.getCompoundTag("cogStackHandler"));
  }

  // ---------------------------------------------------------------------------
  // - Network
  // ---------------------------------------------------------------------------

  @SideOnly(Side.CLIENT)
  @Override
  public void onTileDataUpdate() {

    ClientRenderData data = this.getClientRenderData();

    if (this.triggered.isDirty()
        && this.triggered.get()) {
      data.totalAnimationTime = Math.min(this.getUpdateIntervalTicks(), 40);
      data.remainingAnimationTime = data.totalAnimationTime;
      data.cogRotationStage = (data.cogRotationStage + 1) % 8;
    }

    if (this.tileDataItemStackHandler.isDirty()
        && this.cogStackHandler.getStackInSlot(0).isEmpty()) {
      data.remainingAnimationTime = -1;
    }
  }

  // ---------------------------------------------------------------------------
  // - Interaction
  // ---------------------------------------------------------------------------

  @Override
  public IInteraction<?>[] getInteractions() {

    return this.interactions;
  }

  @Override
  public boolean shouldRenderInPass(int pass) {

    return (pass == 0) || (pass == 1);
  }

  public static class InteractionCog
      extends InteractionItemStack<TileCogWorkerBase> {

    private final TileCogWorkerBase tile;

    /* package */ InteractionCog(TileCogWorkerBase tile, ItemStackHandler stackHandler, AxisAlignedBB bounds, Transform transform) {

      super(
          new ItemStackHandler[]{stackHandler},
          0,
          EnumFacing.VALUES,
          bounds,
          transform
      );
      this.tile = tile;
    }

    @Override
    protected boolean doItemStackValidation(ItemStack itemStack) {

      return this.tile.isValidCog(itemStack);
    }

    public TileCogWorkerBase getTile() {

      return this.tile;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderSolidPass(World world, RenderItem renderItem, BlockPos pos, IBlockState blockState, float partialTicks) {

      InteractionCogRenderer.INSTANCE.renderSolidPass(this, world, renderItem, pos, blockState, partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderAdditivePass(World world, RenderItem renderItem, EnumFacing hitSide, Vec3d hitVec, BlockPos hitPos, IBlockState blockState, ItemStack heldItemMainHand, float partialTicks) {

      return InteractionCogRenderer.INSTANCE.renderAdditivePass(this, world, renderItem, hitSide, hitVec, hitPos, blockState, heldItemMainHand, partialTicks);
    }
  }

  // ---------------------------------------------------------------------------
  // - Stack Handler
  // ---------------------------------------------------------------------------

  public static class CogStackHandler
      extends ObservableStackHandler
      implements ITileDataItemStackHandler {

    private final TileCogWorkerBase tile;

    /* package */ CogStackHandler(TileCogWorkerBase tile) {

      super(1);
      this.tile = tile;
    }

    @Override
    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {

      return 1;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {

      // Filter out non-cog items.

      if (!this.tile.isValidCog(stack)) {
        return stack;
      }

      return super.insertItem(slot, stack, simulate);
    }
  }

  // ---------------------------------------------------------------------------
  // - Client Render Data
  // ---------------------------------------------------------------------------

  @SideOnly(Side.CLIENT)
  public static class ClientRenderData {

    public double remainingAnimationTime;
    public double totalAnimationTime;
    public int cogRotationStage;
  }
}
