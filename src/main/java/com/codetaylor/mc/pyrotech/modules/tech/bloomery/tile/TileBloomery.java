package com.codetaylor.mc.pyrotech.modules.tech.bloomery.tile;

import com.codetaylor.mc.athenaeum.inventory.DynamicStackHandler;
import com.codetaylor.mc.athenaeum.inventory.ObservableStackHandler;
import com.codetaylor.mc.athenaeum.network.tile.data.TileDataBoolean;
import com.codetaylor.mc.athenaeum.network.tile.data.TileDataFloat;
import com.codetaylor.mc.athenaeum.network.tile.data.TileDataInteger;
import com.codetaylor.mc.athenaeum.network.tile.data.TileDataItemStackHandler;
import com.codetaylor.mc.athenaeum.network.tile.spi.ITileData;
import com.codetaylor.mc.athenaeum.network.tile.spi.ITileDataItemStackHandler;
import com.codetaylor.mc.athenaeum.util.*;
import com.codetaylor.mc.pyrotech.IAirflowConsumerCapability;
import com.codetaylor.mc.athenaeum.interaction.api.Transform;
import com.codetaylor.mc.athenaeum.interaction.spi.*;
import com.codetaylor.mc.athenaeum.integration.gamestages.Stages;
import com.codetaylor.mc.pyrotech.library.spi.tile.ITileContainer;
import com.codetaylor.mc.pyrotech.library.spi.tile.TileCapabilityDelegate;
import com.codetaylor.mc.athenaeum.network.tile.spi.TileEntityDataBase;
import com.codetaylor.mc.pyrotech.library.util.Util;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.core.item.ItemMaterial;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomery;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomeryConfig;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.block.BlockPileSlag;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.client.particles.ParticleBloomeryDrip;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.client.render.BloomeryFuelRenderer;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomeryRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.recipe.BloomeryRecipeBase;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BooleanSupplier;

public class TileBloomery
    extends TileEntityDataBase
    implements ITileInteractable,
    ITickable,
    ITileContainer,
    IAirflowConsumerCapability {

  private static final AxisAlignedBB INTERACTION_BOUNDS_TOP = new AxisAlignedBB(2f / 16f, 1, 2f / 16f, 14f / 16f, 24f / 16f, 14f / 16f);

  private static final Transform UPPER_TRANSFORM = new Transform(
      Transform.translate(0.5, 24.0 / 16.0, 0.5),
      Transform.rotate(),
      Transform.scale(0.5, 0.5, 0.5)
  );

  private static final Transform LOWER_TRANSFORM = new Transform(
      Transform.translate(0.5, 5.0 / 16.0, 0.5),
      Transform.rotate(),
      Transform.scale(0.5, 0.5, 0.5)
  );

  private InputStackHandler inputStackHandler;
  private OutputStackHandler outputStackHandler;
  private FuelStackHandler fuelStackHandler;

  private TileDataItemStackHandler<InputStackHandler> tileDataInputStackHandler;

  private TileDataFloat recipeProgress;
  private TileDataFloat speed;
  private TileDataBoolean active;
  private TileDataInteger fuelCount;
  private TileDataInteger burnTime;
  private TileDataFloat airflow;
  private float airflowBonus;
  private int lastBurnTime;
  private BloomeryRecipeBase currentRecipe;
  private int remainingSlag;
  private TileDataInteger ashCount;

  private IInteraction[] interactions;
  private boolean checkLight;

  public TileBloomery() {

    super(ModuleTechBloomery.TILE_DATA_SERVICE);

    this.inputStackHandler = new InputStackHandler(this.getInputCapacity());
    this.inputStackHandler.addObserver((handler, slot) -> {
      this.updateRecipe();
      this.markDirty();
    });

    this.outputStackHandler = new OutputStackHandler();
    this.outputStackHandler.addObserver((handler, slot) -> this.markDirty());

    this.fuelStackHandler = new FuelStackHandler(this);
    this.fuelStackHandler.addObserver((handler, slot) -> {
      this.updateAirflow();
      this.markDirty();
    });

    this.recipeProgress = new TileDataFloat(0, 20);
    this.speed = new TileDataFloat(0);
    this.active = new TileDataBoolean(false);
    this.fuelCount = new TileDataInteger(0);
    this.burnTime = new TileDataInteger(0);
    this.airflow = new TileDataFloat(-1);
    this.airflowBonus = 0;
    this.ashCount = new TileDataInteger(0);

    // --- Network ---

    this.tileDataInputStackHandler = new TileDataItemStackHandler<>(this.inputStackHandler);

    this.registerTileDataForNetwork(new ITileData[]{
        this.tileDataInputStackHandler,
        new TileDataItemStackHandler<>(this.outputStackHandler),
        new TileDataItemStackHandler<>(this.fuelStackHandler),
        this.recipeProgress,
        this.speed,
        this.active,
        this.fuelCount,
        this.burnTime,
        this.airflow,
        this.ashCount
    });

    // --- Interactions ---

    this.interactions = new IInteraction[]{
        new InteractionItem(
            this.getInputInteractionBoundsTop()
        ),
        new InteractionIgnite(
            this.getInputInteractionBoundsTop()
        ),
        new InteractionShovel(
            this.getInputInteractionBoundsTop()
        ),
        new InteractionInput(
            this,
            new ItemStackHandler[]{
                this.inputStackHandler,
                this.outputStackHandler
            },
            this.getInputInteractionBoundsTop(),
            () -> (this.fuelStackHandler.getTotalItemCount() == 0 && !this.isActive() && this.getAshCount() == 0)
        ),
        new InteractionFuel(
            this,
            this.fuelStackHandler,
            this.getInputInteractionBoundsTop(),
            () -> (!this.inputStackHandler.getStackInSlot(0).isEmpty() && this.getAshCount() == 0)
        )
    };
  }

  // ---------------------------------------------------------------------------
  // - Accessors
  // ---------------------------------------------------------------------------

  public void setActive() {

    if (!this.active.get()
        && !this.inputStackHandler.getStackInSlot(0).isEmpty()
        && this.burnTime.get() > 0
        && this.currentRecipe != null) {
      this.active.set(true);
      this.fuelStackHandler.clearStacks();
      this.remainingSlag = this.currentRecipe.getSlagCount() * this.inputStackHandler.getStackInSlot(0).getCount();
    }
  }

  public boolean isActive() {

    return this.active.get();
  }

  public float getRecipeProgress() {

    return this.recipeProgress.get();
  }

  public InputStackHandler getInputStackHandler() {

    return this.inputStackHandler;
  }

  public OutputStackHandler getOutputStackHandler() {

    return this.outputStackHandler;
  }

  public FuelStackHandler getFuelStackHandler() {

    return this.fuelStackHandler;
  }

  public BloomeryRecipeBase getCurrentRecipe() {

    return this.currentRecipe;
  }

  public boolean isFuelFull() {

    return this.getFuelCount() >= this.getMaxFuelCount()
        || (this.hasSpeedCap() && this.burnTime.get() >= this.getMaxBurnTime());
  }

  public int getFuelCount() {

    return this.fuelCount.get();
  }

  public float getSpeed() {

    return this.speed.get();
  }

  public int getBurnTime() {

    return this.burnTime.get();
  }

  public float getAirflow() {

    return this.airflow.get();
  }

  public int getAshCount() {

    return this.ashCount.get();
  }

  public int getMaxFuelCount() {

    return ModuleTechBloomeryConfig.BLOOMERY.FUEL_CAPACITY_ITEMS;
  }

  public int getMaxBurnTime() {

    return ModuleTechBloomeryConfig.BLOOMERY.FUEL_CAPACITY_BURN_TIME;
  }

  public boolean hasSpeedCap() {

    return ModuleTechBloomeryConfig.BLOOMERY.HAS_SPEED_CAP;
  }

  protected float getAirflowDragModifier() {

    return (float) ModuleTechBloomeryConfig.BLOOMERY.AIRFLOW_DRAG_MODIFIER;
  }

  protected float getAirflowModifier() {

    return (float) ModuleTechBloomeryConfig.BLOOMERY.AIRFLOW_MODIFIER;
  }

  protected double getAshConversionChance() {

    return ModuleTechBloomeryConfig.BLOOMERY.ASH_CONVERSION_CHANCE;
  }

  public int getMaxAshCapacity() {

    return ModuleTechBloomeryConfig.BLOOMERY.MAX_ASH_CAPACITY;
  }

  protected double getSpeedScalar() {

    return ModuleTechBloomeryConfig.BLOOMERY.SPEED_SCALAR;
  }

  protected int getItemBurnTime(ItemStack stack) {

    return (int) (StackHelper.getItemBurnTime(stack) * this.getSpecialFuelBurnTimeModifier(stack));
  }

  protected double getSpecialFuelBurnTimeModifier(ItemStack stack) {

    return ModuleTechBloomeryConfig.BLOOMERY.getSpecialFuelBurnTimeModifier(stack);
  }

  protected int getInputCapacity() {

    return Math.min(64, Math.max(0, ModuleTechBloomeryConfig.BLOOMERY.CAPACITY));
  }

  protected boolean dropSlagWhenBroken() {

    return ModuleTechBloomeryConfig.BLOOMERY.DROP_SLAG_WHEN_BROKEN;
  }

  // ---------------------------------------------------------------------------
  // - Capabilities
  // ---------------------------------------------------------------------------

  @Override
  public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {

    return (capability == ModuleCore.CAPABILITY_AIRFLOW_CONSUMER);
  }

  @Nullable
  @Override
  public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {

    if (capability == ModuleCore.CAPABILITY_AIRFLOW_CONSUMER) {
      //noinspection unchecked
      return (T) this;
    }

    return null;
  }

  // ---------------------------------------------------------------------------
  // - Recipe
  // ---------------------------------------------------------------------------

  private void updateRecipe() {

    this.currentRecipe = this.getRecipe(this.inputStackHandler.getStackInSlot(0));
  }

  protected BloomeryRecipeBase<?> getRecipe(ItemStack stackInSlot) {

    return BloomeryRecipe.getRecipe(stackInSlot);
  }

  // ---------------------------------------------------------------------------
  // - Speed
  // ---------------------------------------------------------------------------

  private void updateSpeed() {

    float linearSpeed = this.burnTime.get() / (float) this.getMaxBurnTime();
    float speed = (float) (this.getSpeedScalar() * (float) Math.pow(linearSpeed, 0.5));
    speed *= this.calculateSpeedAirflowModifier();
    this.speed.set(speed);
  }

  private double calculateSpeedAirflowModifier() {

    float airflow = this.airflow.get();
    return Math.pow(airflow - (airflow * 0.19), 0.5) + 0.1;
  }

  protected List<BlockPos> getAirflowCheckPositions() {

    IBlockState tileBlockState = this.world.getBlockState(this.pos);
    EnumFacing tileFacing = this.getTileFacing(this.world, this.pos, tileBlockState);
    BlockPos offset = this.pos.offset(tileFacing);
    return Collections.singletonList(offset);
  }

  public void updateAirflow() {

    IBlockState tileBlockState = this.world.getBlockState(this.pos);
    EnumFacing tileFacing = this.getTileFacing(this.world, this.pos, tileBlockState);
    List<BlockPos> positions = this.getAirflowCheckPositions();

    this.airflow.set(0);

    for (BlockPos position : positions) {
      IBlockState blockState = this.world.getBlockState(position);

      if (this.world.isAirBlock(position)) {
        this.airflow.add(1);

      } else if (blockState.getBlock() == ModuleTechBloomery.Blocks.GENERATED_PILE_SLAG) {
        int level = ModuleTechBloomery.Blocks.PILE_SLAG.getLevel(blockState);

        if (level == 1) {
          this.airflow.add(1);

        } else if (level == 2) {
          this.airflow.add(0.66f);

        } else if (level == 3) {
          this.airflow.add(0.33f);

        } else if (level == 4) {
          this.airflow.add(0);
        }

      } else if (blockState.getBlock().isSideSolid(blockState, this.world, position, tileFacing.getOpposite())) {
        this.airflow.add(0);

      } else {
        this.airflow.add(0.5f);
      }
    }

    // Adding fuel reduces the airflow, y is modifier, x is fuel percentage.
    // y=1-0.75x^2
    {
      double x = this.fuelCount.get() / (double) this.getMaxFuelCount();
      double airflowFuelModifier = 1 - (0.75 * Math.pow(x, 2));
      this.airflow.set((float) (this.airflow.get() * airflowFuelModifier));
    }

    this.airflow.add(this.airflowBonus);

    this.updateSpeed();
  }

  @Override
  public float consumeAirflow(float airflow, boolean simulate) {

    if (!simulate) {
      this.airflowBonus += airflow * this.getAirflowModifier();
      this.updateAirflow();
    }

    return 0;
  }

  // ---------------------------------------------------------------------------
  // - Update
  // ---------------------------------------------------------------------------

  @Override
  public void update() {

    if (!this.checkLight) {
      this.checkLight = true;
      this.world.checkLightFor(EnumSkyBlock.BLOCK, this.pos);
    }

    if (this.world.isRemote) {
      Random rand = RandomHelper.random();

      if (this.isActive()
          && rand.nextDouble() < 0.5) {

        if (rand.nextDouble() < 0.1) {
          world.playSound((double) pos.getX() + 0.5, (double) pos.getY(), (double) pos.getZ() + 0.5, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        }

        IBlockState state = this.world.getBlockState(this.pos);
        EnumFacing facing = state.getValue(Properties.FACING_HORIZONTAL);
        this.spawnDrip(rand, facing);
      }

      return;
    }

    if (this.airflowBonus > 0) {
      this.airflowBonus -= this.airflowBonus * this.getAirflowDragModifier();

      if (this.airflowBonus < MathConstants.FLT_EPSILON) {
        this.airflowBonus = 0;
      }

      this.updateAirflow();
    }

    if (this.airflow.get() < 0) {
      this.updateAirflow();
    }

    if (this.lastBurnTime != this.burnTime.get()) {
      this.lastBurnTime = this.burnTime.get();
      this.updateSpeed();
    }

    if (this.isActive()) {

      if (this.currentRecipe == null) {
        this.burnTime.set(0);
        this.recipeProgress.set(0);
        this.active.set(false);
        return;
      }

      // Recipe

      float increment = (1.0f / this.currentRecipe.getTimeTicks()) * this.speed.get();
      float recipeProgress = this.recipeProgress.add(increment);

      {
        int slag = this.currentRecipe.getSlagCount() * this.inputStackHandler.getStackInSlot(0).getCount();
        float nextSlagInterval = (slag - this.remainingSlag) * (1f / slag) + (1f / slag) * 0.5f;

        if (recipeProgress >= nextSlagInterval) {
          this.remainingSlag -= 1;
          this.createSlag();
        }
      }

      if (recipeProgress >= 0.9999) {

        // Create ash
        int ashCount = this.ashCount.get() + 1;
        Random random = RandomHelper.random();
        int fuelCount = this.fuelCount.get();

        for (int i = 1; i < fuelCount; i++) {

          if (random.nextFloat() < this.getAshConversionChance()) {
            ashCount += 1;
          }
        }

        ashCount = Math.min(this.getMaxAshCapacity(), ashCount);
        this.ashCount.set(ashCount);

        int count = this.inputStackHandler.getStackInSlot(0).getCount();

        // Create the bloom itemstack with nbt
        ItemStack output = this.currentRecipe.getUniqueBloomFromOutput(count);

        // Swap the items
        this.inputStackHandler.extractItem(0, count, false);
        this.outputStackHandler.insertItem(0, output, false);

        // Reset
        this.burnTime.set(0);
        this.fuelCount.set(0);
        this.recipeProgress.set(0);
        this.active.set(false);

        this.updateAirflow();
      }
    }
  }

  protected void spawnDrip(Random rand, EnumFacing facing) {

    double offsetY = rand.nextDouble() * 6.0 / 16.0;
    double x = (double) pos.getX() + 0.5;
    double y = (double) pos.getY() + offsetY;
    double z = (double) pos.getZ() + 0.5;

    double offset = 0.55;
    double lavaOffset = 0.075;
    double dripChance = 0.25;

    switch (facing) {

      case WEST:
        if (rand.nextFloat() < dripChance) {
          Minecraft.getMinecraft().effectRenderer.addEffect(
              ParticleBloomeryDrip.createParticle(world, x - offset - lavaOffset, y - offsetY, z)
          );
        }
        break;

      case EAST:
        if (rand.nextFloat() < dripChance) {
          Minecraft.getMinecraft().effectRenderer.addEffect(
              ParticleBloomeryDrip.createParticle(world, x + offset + lavaOffset, y - offsetY, z)
          );
        }
        break;

      case NORTH:
        if (rand.nextFloat() < dripChance) {
          Minecraft.getMinecraft().effectRenderer.addEffect(
              ParticleBloomeryDrip.createParticle(world, x, y - offsetY, z - offset - lavaOffset)
          );
        }

        break;

      case SOUTH:
        if (rand.nextFloat() < dripChance) {
          Minecraft.getMinecraft().effectRenderer.addEffect(
              ParticleBloomeryDrip.createParticle(world, x, y - offsetY, z + offset + lavaOffset)
          );
        }
    }
  }

  protected void createSlag() {

    EnumFacing facing = this.getTileFacing(this.world, this.pos, this.world.getBlockState(this.pos));
    this.createSlag(this.pos.offset(facing));
  }

  protected void createSlag(BlockPos pos) {

    {
      // Add to an existing pile directly in front of the device
      // if it isn't too big already.

      IBlockState blockState = this.world.getBlockState(pos);
      Block block = blockState.getBlock();

      if (block == ModuleTechBloomery.Blocks.PILE_SLAG) {

        int level = ModuleTechBloomery.Blocks.PILE_SLAG.getLevel(blockState);

        if (level >= 4) {
          return;
        }

        this.addSlagItemToTileEntity(pos);

        this.world.setBlockState(pos, ModuleTechBloomery.Blocks.PILE_SLAG
            .setLevel(blockState, level + 1)
            .withProperty(BlockPileSlag.MOLTEN, true));
        return;
      }
    }

    int y = pos.getY();
    BlockPos startPos = pos;

    // Find the first non-replaceable block.

    for (int i = 0; i < y; i++) {
      pos = startPos.down(i);

      if (!this.world.getBlockState(pos).getBlock().isReplaceable(this.world, pos)) {
        break;
      }
    }

    if (pos.getY() < y
        && pos.getY() > 0) {

      // Clear a path.

      int toClear = y - pos.getY();

      for (int i = 1; i < toClear; i++) {
        this.world.destroyBlock(pos.up(i + 1), true);
      }

      IBlockState blockState = this.world.getBlockState(pos);

      if (blockState.getBlock() == ModuleTechBloomery.Blocks.PILE_SLAG) {

        // Add to an existing pile if it isn't too big.

        int level = ModuleTechBloomery.Blocks.PILE_SLAG.getLevel(blockState);

        if (level < 8) {

          this.addSlagItemToTileEntity(pos);

          this.world.setBlockState(pos, ModuleTechBloomery.Blocks.PILE_SLAG
              .setLevel(blockState, level + 1)
              .withProperty(BlockPileSlag.MOLTEN, true));

          return;
        }
      }

      pos = pos.up();

      // Create a new pile.
      this.world.setBlockState(pos, ModuleTechBloomery.Blocks.PILE_SLAG
          .setLevel(ModuleTechBloomery.Blocks.PILE_SLAG.getDefaultState(), 1)
          .withProperty(BlockPileSlag.MOLTEN, true));

      this.addSlagItemToTileEntity(pos);
    }
  }

  private void addSlagItemToTileEntity(BlockPos pos) {

    TileEntity tileEntity = this.world.getTileEntity(pos);

    if (tileEntity instanceof TilePileSlag) {
      TilePileSlag.StackHandler stackHandler = ((TilePileSlag) tileEntity).getStackHandler();
      ItemStack slagItemStack = this.currentRecipe.getSlagItemStack();

      if (!slagItemStack.isEmpty()) {
        Item slagItem = slagItemStack.getItem();
        ItemStack itemStack = new ItemStack(slagItem);
        stackHandler.insertItem(0, itemStack, false);
      }

      ((TilePileSlag) tileEntity).setLastMolten(this.world.getTotalWorldTime());
    }
  }

  // ---------------------------------------------------------------------------
  // - Network
  // ---------------------------------------------------------------------------

  @Override
  public void onTileDataUpdate() {

    if (this.tileDataInputStackHandler.isDirty()) {
      this.updateRecipe();
    }

    if (this.active.isDirty()) {
      BlockHelper.notifyBlockUpdate(this.world, this.pos);
      this.world.checkLightFor(EnumSkyBlock.BLOCK, this.pos);
    }
  }

  // ---------------------------------------------------------------------------
  // - Serialization
  // ---------------------------------------------------------------------------

  @Nonnull
  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {

    super.writeToNBT(compound);
    compound.setTag("inputStackHandler", this.inputStackHandler.serializeNBT());
    compound.setTag("outputStackHandler", this.outputStackHandler.serializeNBT());
    compound.setTag("fuelStackHandler", this.fuelStackHandler.serializeNBT());
    compound.setFloat("recipeProgress", this.recipeProgress.get());
    compound.setBoolean("active", this.active.get());
    compound.setFloat("speed", this.speed.get());
    compound.setInteger("burnTime", this.burnTime.get());
    compound.setInteger("fuelCount", this.fuelCount.get());
    compound.setFloat("airflow", this.airflow.get());
    compound.setInteger("ashCount", this.ashCount.get());
    compound.setInteger("remainingSlag", this.remainingSlag);
    return compound;
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {

    super.readFromNBT(compound);
    this.inputStackHandler.deserializeNBT(compound.getCompoundTag("inputStackHandler"));
    this.outputStackHandler.deserializeNBT(compound.getCompoundTag("outputStackHandler"));
    this.fuelStackHandler.deserializeNBT(compound.getCompoundTag("fuelStackHandler"));
    this.recipeProgress.set(compound.getFloat("recipeProgress"));
    this.active.set(compound.getBoolean("active"));
    this.speed.set(compound.getFloat("speed"));
    this.burnTime.set(compound.getInteger("burnTime"));
    this.fuelCount.set(compound.getInteger("fuelCount"));
    this.airflow.set(compound.getFloat("airflow"));
    this.ashCount.set(compound.getInteger("ashCount"));
    this.remainingSlag = compound.getInteger("remainingSlag");
    this.updateRecipe();
  }

  // ---------------------------------------------------------------------------
  // - Rendering
  // ---------------------------------------------------------------------------

  @Override
  public boolean shouldRenderInPass(int pass) {

    return (pass == 0) || (pass == 1);
  }

  // ---------------------------------------------------------------------------
  // - Container
  // ---------------------------------------------------------------------------

  @Override
  public void dropContents() {

    if (!this.isActive()) {
      StackHelper.spawnStackHandlerSlotContentsOnTop(this.world, this.inputStackHandler, 0, this.pos);

    } else {
      // drop slag
      if (this.currentRecipe != null
          && this.dropSlagWhenBroken()) {
        ItemStack stackInSlot = this.inputStackHandler.getStackInSlot(0);

        if (!stackInSlot.isEmpty()) {
          ItemStack slagItemStack = this.currentRecipe.getSlagItemStack().copy();
          slagItemStack.setCount(this.remainingSlag);
          StackHelper.spawnStackOnTop(this.world, slagItemStack, this.pos);
        }
      }
    }
    StackHelper.spawnStackHandlerSlotContentsOnTop(this.world, this.outputStackHandler, 0, this.pos);
    StackHelper.spawnStackHandlerContentsOnTop(this.world, this.fuelStackHandler, this.pos);
  }

  // ---------------------------------------------------------------------------
  // - Interactions
  // ---------------------------------------------------------------------------

  @Nullable
  @Override
  public Stages getStages() {

    return ModuleTechBloomeryConfig.STAGES_BLOOMERY;
  }

  @Override
  public IInteraction[] getInteractions() {

    return this.interactions;
  }

  @Override
  public boolean isExtendedInteraction(World world, BlockPos pos, IBlockState blockState) {

    BlockPos blockPos = this.getPos();

    return blockPos.getX() == pos.getX()
        && blockPos.getY() + 1 == pos.getY()
        && blockPos.getZ() == pos.getZ();
  }

  @Override
  public EnumFacing getTileFacing(World world, BlockPos pos, IBlockState blockState) {

    if (blockState.getBlock() == ModuleTechBloomery.Blocks.BLOOMERY) {
      return blockState.getValue(Properties.FACING_HORIZONTAL);
    }

    return ITileInteractable.super.getTileFacing(world, pos, blockState);
  }

  // --- ITEM ---

  /**
   * Used for interaction items like the tongs.
   */
  private class InteractionItem
      extends InteractionUseItemBase<TileBloomery> {

    /* package */ InteractionItem(AxisAlignedBB bounds) {

      super(new EnumFacing[]{EnumFacing.UP}, bounds);
    }
  }

  // --- FLINT AND STEEL ---

  private class InteractionIgnite
      extends InteractionUseItemBase<TileBloomery> {

    /* package */ InteractionIgnite(AxisAlignedBB interactionBounds) {

      super(new EnumFacing[]{EnumFacing.UP}, interactionBounds);
    }

    @Override
    protected boolean allowInteraction(TileBloomery tile, World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing hitSide, float hitX, float hitY, float hitZ) {

      return (player.getHeldItem(hand).getItem() == Items.FLINT_AND_STEEL
          || player.getHeldItem(hand).getItem() == Items.FIRE_CHARGE)
          && (!tile.isActive());
    }

    @Override
    protected boolean doInteraction(TileBloomery tile, World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing hitSide, float hitX, float hitY, float hitZ) {

      if (!world.isRemote) {

        tile.setActive();

        if (player.getHeldItem(hand).getItem() == Items.FIRE_CHARGE) {
          player.getHeldItem(hand).shrink(1);
        }

        world.playSound(
            null,
            hitPos,
            SoundEvents.ITEM_FLINTANDSTEEL_USE,
            SoundCategory.BLOCKS,
            1.0F,
            Util.RANDOM.nextFloat() * 0.4F + 0.8F
        );
      }

      return true;
    }
  }

  // --- INPUT / OUTPUT ---

  private class InteractionInput
      extends InteractionItemStack<TileBloomery> {

    private final TileBloomery tile;
    private final BooleanSupplier isEnabled;

    /* package */ InteractionInput(TileBloomery tile, ItemStackHandler[] stackHandlers, AxisAlignedBB interactionBounds, BooleanSupplier isEnabled) {

      super(
          stackHandlers,
          0,
          new EnumFacing[]{EnumFacing.UP},
          interactionBounds,
          LOWER_TRANSFORM
      );
      this.tile = tile;
      this.isEnabled = isEnabled;
    }

    @Override
    public Transform getTransform(World world, BlockPos pos, IBlockState blockState, ItemStack itemStack, float partialTicks) {

      ItemStack input = this.stackHandlers[0].getStackInSlot(0);
      ItemStack output = this.stackHandlers[1].getStackInSlot(0);

      if (input.isEmpty()
          && output.isEmpty()) {
        return UPPER_TRANSFORM;
      }

      return super.getTransform(world, pos, blockState, itemStack, partialTicks);
    }

    @Override
    public boolean isEnabled() {

      return this.isEnabled.getAsBoolean();
    }

    @Override
    protected boolean doItemStackValidation(ItemStack itemStack) {

      return (this.tile.getRecipe(itemStack) != null);
    }

    @Override
    public boolean interact(EnumType type, TileBloomery tile, World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing hitSide, float hitX, float hitY, float hitZ) {

      return super.interact(type, tile, world, hitPos, state, player, hand, hitSide, hitX, hitY, hitZ);
    }
  }

  // --- FUEL ---

  public class InteractionFuel
      extends InteractionItemStack<TileBloomery> {

    private final TileBloomery tile;
    private final FuelStackHandler fuelStackHandler;
    private final BooleanSupplier isEnabled;

    /* package */ InteractionFuel(TileBloomery tile, FuelStackHandler fuelStackHandler, AxisAlignedBB interactionBounds, BooleanSupplier isEnabled) {

      super(
          new ItemStackHandler[]{fuelStackHandler},
          0,
          new EnumFacing[]{EnumFacing.UP},
          interactionBounds,
          UPPER_TRANSFORM
      );
      this.tile = tile;
      this.fuelStackHandler = fuelStackHandler;
      this.isEnabled = isEnabled;
    }

    public FuelStackHandler getFuelStackHandler() {

      return this.fuelStackHandler;
    }

    public TileBloomery getTile() {

      return this.tile;
    }

    @Override
    public boolean isEnabled() {

      return this.isEnabled.getAsBoolean();
    }

    @Override
    public ItemStack getStackInSlot() {

      return super.getStackInSlot();
    }

    @Override
    protected boolean doItemStackValidation(ItemStack itemStack) {

      return StackHelper.isFuel(itemStack)
          && !itemStack.getItem().hasContainerItem(itemStack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderSolidPass(World world, RenderItem renderItem, BlockPos pos, IBlockState blockState, float partialTicks) {

      BloomeryFuelRenderer.INSTANCE.renderSolidPass(this, world, renderItem, pos, blockState, partialTicks);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderSolidPassText(World world, FontRenderer fontRenderer, int yaw, Vec3d offset, BlockPos pos, IBlockState blockState, float partialTicks) {

      //
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderAdditivePass(World world, RenderItem renderItem, EnumFacing hitSide, Vec3d hitVec, BlockPos hitPos, IBlockState blockState, ItemStack heldItemMainHand, float partialTicks) {

      return BloomeryFuelRenderer.INSTANCE.renderAdditivePass(this, world, renderItem, hitSide, hitVec, hitPos, blockState, heldItemMainHand, partialTicks);
    }
  }

  // --- ASH ---

  private class InteractionShovel
      extends InteractionBase<TileBloomery> {

    /* package */ InteractionShovel(AxisAlignedBB interactionBounds) {

      super(new EnumFacing[]{EnumFacing.UP}, interactionBounds);
    }

    @Override
    public boolean interact(EnumType type, TileBloomery tile, World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing hitSide, float hitX, float hitY, float hitZ) {

      if (type != EnumType.MouseClick) {
        return false;
      }

      ItemStack heldItem = player.getHeldItemMainhand();

      if (tile.ashCount.get() > 0
          && heldItem.getItem().getToolClasses(heldItem).contains("shovel")) {

        if (!world.isRemote) {
          tile.ashCount.set(tile.ashCount.get() - 1);
          StackHelper.spawnStackOnTop(world, ItemMaterial.EnumType.PIT_ASH.asStack(), hitPos, 1.5);
          heldItem.damageItem(1, player);
          world.playSound(null, hitPos, SoundEvents.BLOCK_SAND_BREAK, SoundCategory.BLOCKS, 1, 1);
        }

        return true;
      }

      return false;
    }
  }

  private AxisAlignedBB getInputInteractionBoundsTop() {

    return INTERACTION_BOUNDS_TOP;
  }

  // ---------------------------------------------------------------------------
  // - Stack Handlers
  // ---------------------------------------------------------------------------

  public class InputStackHandler
      extends ObservableStackHandler
      implements ITileDataItemStackHandler {

    private final int capacity;

    public InputStackHandler(int capacity) {

      super(1);
      this.capacity = capacity;
    }

    @Override
    public int getSlotLimit(int slot) {

      return this.capacity;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {

      if (TileBloomery.this.getRecipe(stack) == null
          || !TileBloomery.this.outputStackHandler.getStackInSlot(0).isEmpty()) {
        return stack;
      }

      return super.insertItem(slot, stack, simulate);
    }
  }

  public class OutputStackHandler
      extends ObservableStackHandler
      implements ITileDataItemStackHandler {

    /* package */ OutputStackHandler() {

      super(1);
    }

    @Override
    public int getSlotLimit(int slot) {

      return 1;
    }
  }

  public class FuelStackHandler
      extends DynamicStackHandler {

    private final TileBloomery tile;

    /* package */ FuelStackHandler(TileBloomery tile) {

      super(1);
      this.tile = tile;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {

      // Only allow if the input stack handler is not empty.
      if (this.tile.inputStackHandler.getStackInSlot(0).isEmpty()) {
        return stack;
      }

      // Only solid fuel
      if (stack.getItem().hasContainerItem(stack)) {
        return stack;
      }

      int fuelCount = this.tile.fuelCount.get();

      // Only if not full of items
      if (fuelCount >= this.tile.getMaxFuelCount()) {
        return stack;
      }

      int fuelBurnTimeSingle = this.tile.getItemBurnTime(stack);

      if (fuelBurnTimeSingle <= 0) {
        return stack;
      }

      int max = this.tile.getMaxBurnTime();
      int fuelBurnTimeTotal = fuelBurnTimeSingle * stack.getCount();

      if (this.tile.hasSpeedCap()
          && this.tile.burnTime.get() >= max) {
        return stack; // There's no room for insert, fail
      }

      if ((!this.tile.hasSpeedCap() || this.tile.burnTime.get() + fuelBurnTimeTotal <= max)
          && fuelCount + stack.getCount() <= this.tile.getMaxFuelCount()) {

        // There's enough room for all items in the stack. If not a simulation
        // Increase the burn time. Only do the insertion if the machine is
        // not active.

        if (!simulate) {
          this.tile.burnTime.add(fuelBurnTimeTotal);
          this.tile.fuelCount.add(stack.getCount());

          if (!this.tile.isActive()) {
            this.insertItem(stack, false);
          }
        }

        return ItemStack.EMPTY;

      } else {

        // Trim the input stack down to size and, if this isn't a simulation,
        // increase the burn time. Only do the insertion if the machine is
        // not active.

        ItemStack toInsert = stack.copy();
        int itemCountInsertCount = Math.min(toInsert.getCount(), this.tile.getMaxFuelCount() - fuelCount);
        int insertCount;

        if (this.tile.hasSpeedCap()) {
          int burnTimeInsertCount = Math.min(toInsert.getCount(), (int) ((max - this.tile.burnTime.get()) / (float) fuelBurnTimeSingle));
          insertCount = Math.min(burnTimeInsertCount, itemCountInsertCount);
          toInsert.setCount(insertCount);

        } else {
          insertCount = itemCountInsertCount;
          toInsert.setCount(insertCount);
        }

        if (!simulate) {
          this.tile.burnTime.add(insertCount * fuelBurnTimeSingle);
          this.tile.fuelCount.add(insertCount);

          if (!this.tile.isActive()) {
            this.insertItem(toInsert, false);
          }
        }

        ItemStack toReturn = stack.copy();
        toReturn.setCount(toReturn.getCount() - insertCount);
        return toReturn;
      }

    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {

      // Find the first non-empty slot with the highest index and extract from
      // that slot. If this isn't a simulation and the machine isn't active,
      // reduce the burn time.

      // If the machine is active, we don't want to reduce the burn time because
      // this method is called to remove any fuel that is allowed to be inserted
      // when the machine is active.

      for (int i = this.getSlots() - 1; i >= 0; i--) {

        if (!this.getStackInSlot(i).isEmpty()) {
          ItemStack extractItem = super.extractItem(i, amount, true);

          if (!simulate) {

            if (!this.tile.isActive()) {
              this.tile.burnTime.add(-this.tile.getItemBurnTime(extractItem) * extractItem.getCount());
              this.tile.fuelCount.add(-extractItem.getCount());
            }
            super.extractItem(i, amount, false);
          }

          return extractItem;
        }
      }

      return ItemStack.EMPTY;
    }
  }

  // ---------------------------------------------------------------------------
  // - Top
  // ---------------------------------------------------------------------------

  public static class Top
      extends TileCapabilityDelegate
      implements ITileInteractable {

    public Top() {

      super(EnumFacing.DOWN, EnumFacing.UP);
    }
  }

}
