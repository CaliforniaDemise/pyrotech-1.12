package com.codetaylor.mc.pyrotech.modules.tech.basic.tile;

import com.codetaylor.mc.athenaeum.integration.gamestages.Stages;
import com.codetaylor.mc.athenaeum.interaction.api.Transform;
import com.codetaylor.mc.athenaeum.interaction.spi.IInteraction;
import com.codetaylor.mc.athenaeum.interaction.spi.ITileInteractable;
import com.codetaylor.mc.athenaeum.interaction.spi.InteractionBase;
import com.codetaylor.mc.athenaeum.interaction.spi.InteractionItemStack;
import com.codetaylor.mc.athenaeum.inventory.LIFOStackHandler;
import com.codetaylor.mc.athenaeum.inventory.ObservableStackHandler;
import com.codetaylor.mc.athenaeum.network.tile.data.TileDataBoolean;
import com.codetaylor.mc.athenaeum.network.tile.data.TileDataInteger;
import com.codetaylor.mc.athenaeum.network.tile.data.TileDataItemStackHandler;
import com.codetaylor.mc.athenaeum.network.tile.spi.ITileData;
import com.codetaylor.mc.athenaeum.network.tile.spi.ITileDataItemStackHandler;
import com.codetaylor.mc.athenaeum.network.tile.spi.TileDataBase;
import com.codetaylor.mc.athenaeum.util.*;
import com.codetaylor.mc.pyrotech.library.InteractionUseItemToActivateWorker;
import com.codetaylor.mc.pyrotech.library.spi.interaction.InteractionExtinguishable;
import com.codetaylor.mc.pyrotech.library.spi.tile.TileCombustionWorkerBase;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCoreConfig;
import com.codetaylor.mc.pyrotech.modules.core.item.ItemMaterial;
import com.codetaylor.mc.pyrotech.modules.ignition.item.ItemIgniterBase;
import com.codetaylor.mc.pyrotech.modules.tech.basic.ModuleTechBasic;
import com.codetaylor.mc.pyrotech.modules.tech.basic.ModuleTechBasicConfig;
import com.codetaylor.mc.pyrotech.modules.tech.basic.block.BlockCampfire;
import com.codetaylor.mc.pyrotech.modules.tech.basic.client.render.CampfireInteractionLogRenderer;
import com.codetaylor.mc.pyrotech.modules.tech.basic.event.CampfireEffectTracker;
import com.codetaylor.mc.pyrotech.modules.tech.basic.potion.PotionResting;
import com.codetaylor.mc.pyrotech.modules.tech.basic.recipe.CampfireRecipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;

public class TileCampfire
    extends TileCombustionWorkerBase
    implements ITickable,
    ITileInteractable {

  // --- Networked ---

  private final InputStackHandler inputStackHandler;
  private final OutputStackHandler outputStackHandler;
  private final FuelStackHandler fuelStackHandler;

  private final TileDataInteger ashLevel;
  private final TileDataBoolean dead;
  private final TileDataItemStackHandler<FuelStackHandler> fuelStackHandlerData;

  // --- Server ---

  private int cookTime;
  private int cookTimeTotal;
  private final TickCounter burnedFoodTickCounter;
  private int[] effectRadii;

  /**
   * Indicates if this has been lit, affects drops.
   */
  private boolean extinguishedByRain;

  private final IInteraction<?>[] interactions;
  private int interactionCooldown;
  private AxisAlignedBB effectBounds;

  public TileCampfire() {

    super(ModuleTechBasic.TILE_DATA_SERVICE, 1);

    // --- Init ---

    {
      int modifier = MathHelper.clamp(ModuleTechBasicConfig.CAMPFIRE.FUEL_LEVEL_FOR_FULL_COOK_SPEED, 1, 8);
      this.burnedFoodTickCounter = new TickCounter(ModuleTechBasicConfig.CAMPFIRE.BURNED_FOOD_TICKS * modifier);
    }

    this.inputStackHandler = new InputStackHandler();
    this.inputStackHandler.addObserver((handler, slot) -> {
      int modifier = MathHelper.clamp(ModuleTechBasicConfig.CAMPFIRE.FUEL_LEVEL_FOR_FULL_COOK_SPEED, 1, 8);
      this.setCookTime(this.getCookTime(handler.getStackInSlot(slot)) * modifier);
      this.markDirty();
    });

    this.outputStackHandler = new OutputStackHandler();
    this.outputStackHandler.addObserver((handler, slot) -> {

      if (handler.getStackInSlot(slot).isEmpty()) {
        this.burnedFoodTickCounter.reset();
      }

      this.markDirty();
    });

    this.fuelStackHandler = new FuelStackHandler();
    this.fuelStackHandler.addObserver((handler, slot) -> {
      this.burnTimeRemaining.forceUpdate();
      this.markDirty();
    });

    this.ashLevel = new TileDataInteger(0);
    this.ashLevel.addChangeObserver(new TileDataBase.IChangeObserver.OnDirtyMarkTileDirty<>(this));

    this.dead = new TileDataBoolean(false);
    this.dead.addChangeObserver(new TileDataBase.IChangeObserver.OnDirtyMarkTileDirty<>(this));

    this.cookTime = -1;
    this.cookTimeTotal = -1;

    // --- Network ---

    this.fuelStackHandlerData = new TileDataItemStackHandler<>(this.fuelStackHandler);

    this.registerTileDataForNetwork(new ITileData[]{
        new TileDataItemStackHandler<>(this.inputStackHandler),
        new TileDataItemStackHandler<>(this.outputStackHandler),
        this.fuelStackHandlerData,
        this.ashLevel,
        this.dead
    });

    // --- Interactions ---

    this.interactions = new IInteraction[]{
        new InteractionExtinguish(),
        new TileCampfire.InteractionFood(new ItemStackHandler[]{
            this.inputStackHandler,
            this.outputStackHandler
        }),
        new TileCampfire.InteractionShovel(),
        new InteractionUseItemToActivateWorker<>(Items.FLINT_AND_STEEL, EnumFacing.VALUES, BlockCampfire.AABB_FULL),
        new InteractionUseItemToActivateWorker<>(Items.FIRE_CHARGE, EnumFacing.VALUES, BlockCampfire.AABB_FULL, true),
        new TileCampfire.InteractionLog(this)
    };
  }

  // ---------------------------------------------------------------------------
  // - Accessors
  // ---------------------------------------------------------------------------

  public ItemStackHandler getInputStackHandler() {

    return this.inputStackHandler;
  }

  public ItemStackHandler getOutputStackHandler() {

    return this.outputStackHandler;
  }

  public int getAshLevel() {

    return this.ashLevel.get();
  }

  private void setAshLevel(int ashLevel) {

    this.ashLevel.set(ashLevel);
  }

  private void setCookTime(int cookTime) {

    this.cookTime = cookTime;
    this.cookTimeTotal = cookTime;
  }

  private int getCookTime(ItemStack stack) {

    CampfireRecipe recipe = CampfireRecipe.getRecipe(stack);
    return (recipe == null) ? -1 : recipe.getTicks();
  }

  public int getFuelRemaining() {

    // 0 is no wood
    // [0, 8]

    int index = this.fuelStackHandler.getLastNonEmptyIndex();
    return (index + 1);
  }

  public boolean isDead() {

    return this.dead.get();
  }

  private void setDead() {

    this.dead.set(true);
    this.world.setBlockState(this.pos, this.world.getBlockState(this.pos)
        .withProperty(BlockCampfire.VARIANT, BlockCampfire.EnumType.ASH));
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

      if (facing == EnumFacing.UP) {
        //noinspection unchecked
        return (T) this.inputStackHandler;

      } else if (facing == EnumFacing.DOWN) {

        //noinspection unchecked
        return (T) this.outputStackHandler;

      } else {
        //noinspection unchecked
        return (T) this.fuelStackHandler;
      }
    }

    return null;
  }

  protected boolean allowAutomation() {

    return ModuleTechBasicConfig.CAMPFIRE.ALLOW_AUTOMATION;
  }

  // ---------------------------------------------------------------------------
  // - Combustion Worker
  // ---------------------------------------------------------------------------

  @Override
  protected int combustionGetInitialBurnTimeRemaining() {

    return ModuleCoreConfig.FUEL.TINDER_BURN_TIME_TICKS;
  }

  @Override
  protected int combustionGetBurnTimeForFuel(ItemStack fuel) {

    return ModuleTechBasicConfig.CAMPFIRE.BURN_TIME_TICKS_PER_LOG;
  }

  @Override
  protected ItemStack combustionGetFuelItem() {

    return this.fuelStackHandler.extractItem(0, 1, false);
  }

  @Override
  protected int combustionGetRainDeactivateTime() {

    if (ModuleTechBasicConfig.CAMPFIRE.EXTINGUISHED_BY_RAIN) {
      return ModuleTechBasicConfig.CAMPFIRE.TICKS_BEFORE_EXTINGUISHED;
    }

    return -1;
  }

  @Override
  protected void combustionOnDeactivatedByRain() {

    this.extinguishedByRain = true;
  }

  // ---------------------------------------------------------------------------
  // - Worker
  // ---------------------------------------------------------------------------

  @Override
  public void workerSetActive(boolean active) {

    if (this.isDead()) {
      return;
    }

    if (this.workerIsActive() && !active) {

      if (this.world.getBlockState(this.pos).getBlock() == ModuleTechBasic.Blocks.CAMPFIRE) {
        this.world.setBlockState(this.pos, this.world.getBlockState(this.pos)
            .withProperty(BlockCampfire.VARIANT, BlockCampfire.EnumType.NORMAL));
      }

    } else if (!this.workerIsActive() && active) {

      if (this.world.getBlockState(this.pos).getBlock() == ModuleTechBasic.Blocks.CAMPFIRE) {
        this.world.setBlockState(this.pos, this.world.getBlockState(this.pos)
            .withProperty(BlockCampfire.VARIANT, BlockCampfire.EnumType.LIT));
      }
    }

    super.workerSetActive(active);

    if (active) {
      this.interactionCooldown = 5;
    }
  }

  @Override
  public boolean workerIsActive() {

    return !this.isDead() && super.workerIsActive();
  }

  @Override
  public boolean workerConsumeFuel() {

    if (super.workerConsumeFuel()) {
      return true;
    }

    // No fuel remaining, deactivate, set dead, and drop any input or output
    // stacks.

    this.setDead();

    ItemStack contents = this.inputStackHandler.extractItem(0, 64, false);

    if (!contents.isEmpty()) {
      StackHelper.spawnStackOnTop(this.world, contents, this.pos, -0.125);
    }

    contents = this.outputStackHandler.extractItem(0, 64, false);

    if (!contents.isEmpty()) {
      StackHelper.spawnStackOnTop(this.world, contents, this.pos, -0.125);
    }

    return false;
  }

  @Override
  protected float workerCalculateProgress(int taskIndex) {

    if (this.cookTime < 0) {
      return 0;
    }

    return 1f - (this.cookTime / (float) this.cookTimeTotal);
  }

  @Override
  public void update() {

    if (!this.world.isRemote
        && this.interactionCooldown > 0) {
      this.interactionCooldown -= 1;
    }

    super.update();

    if (!this.world.isRemote) {
      return;
    }

    if (ModuleCoreConfig.CLIENT.SHOW_RECIPE_PROGRESSION_PARTICLES
        && this.workerIsActive()
        && !this.getInputStackHandler().getStackInSlot(0).isEmpty()
        && this.world.getTotalWorldTime() % 40 == 0) {
      ParticleHelper.spawnProgressParticlesClient(1, this.pos.getX() + 0.5, this.pos.getY() + 0.85, this.pos.getZ() + 0.5, 0.25, 0.30, 0.25);
    }

    if (this.workerIsActive()
        && !this.getOutputStackHandler().getStackInSlot(0).isEmpty()
        && this.world.getTotalWorldTime() % 5 == 0) {

      for (int i = 0; i < 4; i++) {
        double offsetX = (RandomHelper.random().nextDouble() * 2.0 - 1.0) * 0.4;
        double offsetY = (RandomHelper.random().nextDouble() * 2.0 - 1.0) * 0.4;
        double offsetZ = (RandomHelper.random().nextDouble() * 2.0 - 1.0) * 0.4;
        double x = this.pos.getX() + 0.5 + offsetX;
        double y = this.pos.getY() + 0.6 + offsetY;
        double z = this.pos.getZ() + 0.5 + offsetZ;
        this.world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0.0, 0.0, 0.0);
      }
    }
  }

  @Override
  public boolean workerDoWork() {

    if (!super.workerDoWork()) {
      return false;
    }

    // Deactivate the worker if the ash level gets too high.
    if (this.ashLevel.get() == 8) {
      return false; // Deactivate the worker
    }

    // If the block underneath the campfire is flammable, there's a small chance
    // that it will turn into a fire. When this happens, the campfire will be
    // destroyed and pop its contents into the world.
    if (Math.random() < 0.05
        && this.world.getBlockState(this.pos.down()).getBlock().isFlammable(this.world, this.pos.down(), EnumFacing.UP)) {
      this.world.setBlockState(this.pos.down(), Blocks.FIRE.getDefaultState(), 3);
      return false; // Deactivate the worker
    }

    // Apply the tier 1 campfire effects
    long worldTime = this.world.getWorldTime() % 24000;

    if (this.world.getTotalWorldTime() % 20 == 0
        && worldTime >= ModuleTechBasicConfig.CAMPFIRE_EFFECTS.EFFECTS_START_TIME
        && worldTime <= ModuleTechBasicConfig.CAMPFIRE_EFFECTS.EFFECTS_STOP_TIME) {

      if (ModuleTechBasicConfig.CAMPFIRE_EFFECTS.COMFORT_EFFECT_ENABLED
          || ModuleTechBasicConfig.CAMPFIRE_EFFECTS.RESTING_EFFECT_ENABLED) {

        if (this.effectBounds == null) {
          this.effectBounds = new AxisAlignedBB(this.pos).grow(15);
        }

        List<EntityPlayer> players = this.world.getEntitiesWithinAABB(
            EntityPlayer.class,
            this.effectBounds,
            player -> player != null && this.isEntityInEffectRange(player, this.pos)
        );

        players.forEach(player -> {

          List<EntityMob> mobs = this.world.getEntitiesWithinAABB(
              EntityMob.class,
              this.effectBounds,
              mob -> mob != null && mob.getDistanceSq(player.getPosition()) < 15 * 15
          );

          if (mobs.isEmpty()) {

            // Only apply the effects if there aren't any mobs within range
            // of the player.

            if (ModuleTechBasicConfig.CAMPFIRE_EFFECTS.COMFORT_EFFECT_ENABLED) {

              if (player.getActivePotionEffect(ModuleTechBasic.Potions.COMFORT) == null
                  || this.world.getTotalWorldTime() % 200 == 0) {
                player.addPotionEffect(new PotionEffect(ModuleTechBasic.Potions.COMFORT, Short.MAX_VALUE, 0, true, true));

                if (ModuleTechBasicConfig.CAMPFIRE_EFFECTS.DEBUG) {
                  String message = "Campfire at " + this.pos + " added comfort effect";
                  ModuleCore.LOGGER.debug(message);
                  player.sendMessage(new TextComponentString(message));
                }
              }
            }

            if (ModuleTechBasicConfig.CAMPFIRE_EFFECTS.RESTING_EFFECT_ENABLED) {

              if (player.getActivePotionEffect(ModuleTechBasic.Potions.RESTING) == null) {
                PotionResting.addEffect(player);

                if (ModuleTechBasicConfig.CAMPFIRE_EFFECTS.DEBUG) {
                  String message = "Campfire at " + this.pos + " added resting effect";
                  ModuleCore.LOGGER.debug(message);
                  player.sendMessage(new TextComponentString(message));
                }
              }
            }

            CampfireEffectTracker.TRACKING_CAMPFIRES
                .computeIfAbsent(player.getUniqueID(), uuid -> new HashSet<>())
                .add(this.getPos());

          } else {

            // If there are mobs within range, remove this campfire and player
            // from the tracked campfires.

            CampfireEffectTracker.TRACKING_CAMPFIRES
                .computeIfAbsent(player.getUniqueID(), uuid -> new HashSet<>())
                .remove(this.getPos());
          }
        });
      }
    }

    // Decrement the cook time and check for recipe completion.
    // Update worker progress.
    if (this.cookTime > 0) {
      this.cookTime -= this.getFuelRemaining();
    }

    if (this.cookTime <= 0) {
      ItemStack itemStack = this.inputStackHandler.extractItem(0, 1, false);

      if (!itemStack.isEmpty()) {
        CampfireRecipe recipe = CampfireRecipe.getRecipe(itemStack);

        if (recipe != null) {
          ItemStack result = recipe.getOutput();
          this.outputStackHandler.insertItem(0, result, false);
        }
      }
    }

    if (!this.outputStackHandler.getStackInSlot(0).isEmpty()) {
      ItemStack stackInSlot = this.outputStackHandler.getStackInSlot(0);

      if (stackInSlot.getItem() != ModuleCore.Items.BURNED_FOOD
          && stackInSlot.getItem() instanceof ItemFood) {

        if (this.burnedFoodTickCounter.increment(this.getFuelRemaining())) {
          this.outputStackHandler.setStackInSlot(0, new ItemStack(ModuleCore.Items.BURNED_FOOD));
        }
      }

    } else {
      this.burnedFoodTickCounter.reset();
    }

    // Randomly add ash.

    if (this.combustionGetBurnTimeRemaining() <= 0) {

      if (Math.random() < ModuleTechBasicConfig.CAMPFIRE.ASH_CHANCE) {
        this.ashLevel.add(1);
      }
    }

    return true;
  }

  public boolean isEntityInEffectRange(EntityLivingBase entity) {

    return this.isEntityInEffectRange(entity, this.pos);
  }

  public boolean isEntityInEffectRange(EntityLivingBase entity, BlockPos blockPos) {

    int effectRadius = this.getEffectRadius();
    int effectRadiusSq = effectRadius * effectRadius;
    return entity.getDistanceSqToCenter(blockPos) <= effectRadiusSq;
  }

  public int getEffectRadius() {

    if (this.effectRadii == null) {
      // Precalculate the effect radius
      this.effectRadii = new int[9];

      for (int i = 0; i < 9; i++) {
        float lightPercentage = i / 8f;
        int max = Math.min(6, Math.max(ModuleTechBasicConfig.CAMPFIRE.MAXIMUM_LIGHT_LEVEL, ModuleTechBasicConfig.CAMPFIRE.MINIMUM_LIGHT_LEVEL));
        int min = Math.max(1, Math.min(ModuleTechBasicConfig.CAMPFIRE.MAXIMUM_LIGHT_LEVEL, ModuleTechBasicConfig.CAMPFIRE.MINIMUM_LIGHT_LEVEL));
        this.effectRadii[i] = (int) MathHelper.clamp((max - min) * lightPercentage + min, 0, 15);
      }
    }

    return this.effectRadii[this.getFuelRemaining()];
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

    compound.setBoolean("dead", this.dead.get());
    compound.setInteger("ashLevel", this.ashLevel.get());

    compound.setInteger("cookTime", this.cookTime);
    compound.setInteger("cookTimeTotal", this.cookTimeTotal);
    compound.setBoolean("extinguishedByRain", this.extinguishedByRain);
    return compound;
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {

    super.readFromNBT(compound);

    this.inputStackHandler.deserializeNBT(compound.getCompoundTag("inputStackHandler"));
    this.outputStackHandler.deserializeNBT(compound.getCompoundTag("outputStackHandler"));
    this.fuelStackHandler.deserializeNBT(compound.getCompoundTag("fuelStackHandler"));

    this.dead.set(compound.getBoolean("dead"));
    this.ashLevel.set(compound.getInteger("ashLevel"));

    this.cookTime = compound.getInteger("cookTime");
    this.cookTimeTotal = compound.getInteger("cookTimeTotal");
    this.extinguishedByRain = compound.getBoolean("extinguishedByRain");
  }

  // ---------------------------------------------------------------------------
  // - Container
  // ---------------------------------------------------------------------------

  public void dropContents() {

    if (this.isDead()) {
      StackHelper.spawnStackOnTop(this.world, ItemMaterial.EnumType.PIT_ASH.asStack(1), this.pos, -0.125);

    } else if (!this.extinguishedByRain
        && !this.workerIsActive()) {
      StackHelper.spawnStackOnTop(this.world, new ItemStack(ModuleTechBasic.Items.TINDER), this.pos, -0.125);
    }

    ItemStack itemStack;

    for (int i = 0; i < this.fuelStackHandler.getSlots(); i++) {

      itemStack = this.fuelStackHandler.extractItem(i, 64, false);

      if (!itemStack.isEmpty()) {
        StackHelper.spawnStackOnTop(this.world, itemStack, this.pos, -0.125);

      } else {
        break;
      }
    }

    this.dropInput();
    this.dropOutput();

    if (this.ashLevel.get() > 0) {
      ItemStack ashStack = ItemMaterial.EnumType.PIT_ASH.asStack(this.ashLevel.get());
      StackHelper.spawnStackOnTop(this.world, ashStack, this.pos, -0.125);
    }

    BlockHelper.notifyBlockUpdate(this.world, this.pos);
  }

  public void dropOutput() {

    ItemStack itemStack;
    ItemStackHandler stackHandler = this.getOutputStackHandler();
    itemStack = stackHandler.extractItem(0, 64, false);

    if (!itemStack.isEmpty()) {
      StackHelper.spawnStackOnTop(this.world, itemStack, this.pos, -0.125);
    }
  }

  public void dropInput() {

    ItemStack itemStack;
    ItemStackHandler stackHandler = this.getInputStackHandler();
    itemStack = stackHandler.extractItem(0, 64, false);

    if (!itemStack.isEmpty()) {
      StackHelper.spawnStackOnTop(this.world, itemStack, this.pos, -0.125);
    }
  }

  // ---------------------------------------------------------------------------
  // - Network
  // ---------------------------------------------------------------------------

  @SideOnly(Side.CLIENT)
  @Override
  public void onTileDataUpdate() {

    // This tells the client that when one of these properties updates,
    // we need to update the actual block state and do lighting changes.

    if (this.ashLevel.isDirty()) {
      BlockHelper.notifyBlockUpdate(this.world, this.pos);
    }

    if (this.fuelStackHandlerData.isDirty()) {
      BlockHelper.notifyBlockUpdate(this.world, this.pos);
      this.world.checkLightFor(EnumSkyBlock.BLOCK, this.pos);

    } else if (this.active.isDirty()) {
      BlockHelper.notifyBlockUpdate(this.world, this.pos);
      this.world.checkLightFor(EnumSkyBlock.BLOCK, this.pos);

    } else if (this.dead.isDirty() && this.dead.get()) {
      BlockHelper.notifyBlockUpdate(this.world, this.pos);
      this.world.checkLightFor(EnumSkyBlock.BLOCK, this.pos);
    }
  }

  // ---------------------------------------------------------------------------
  // - Rendering
  // ---------------------------------------------------------------------------

  @Override
  public boolean shouldRenderInPass(int pass) {

    // Required in both passes for the interactable TESR.
    return (pass == 0) || (pass == 1);
  }

  @Nonnull
  @Override
  public AxisAlignedBB getRenderBoundingBox() {

    return new AxisAlignedBB(this.getPos());
  }

  // ---------------------------------------------------------------------------
  // - Interaction
  // ---------------------------------------------------------------------------

  @Nullable
  @Override
  public Stages getStages() {

    return ModuleTechBasicConfig.STAGES_CAMPFIRE;
  }

  @Override
  public IInteraction<?>[] getInteractions() {

    return this.interactions;
  }

  @Override
  public int getInteractionCooldown() {

    return this.interactionCooldown;
  }

  private static class InteractionExtinguish
      extends InteractionExtinguishable<TileCampfire> {

    public InteractionExtinguish() {

      super(
          tile -> (tile.workerIsActive() && !tile.isDead()),
          tile -> {
            tile.combustionOnDeactivatedByRain();
            tile.workerSetActive(false);
          }
      );
    }
  }

  private class InteractionFood
      extends InteractionItemStack<TileCampfire> {

    /* package */ InteractionFood(ItemStackHandler[] stackHandlers) {

      super(
          stackHandlers,
          0,
          new EnumFacing[]{EnumFacing.UP},
          BlockCampfire.AABB_FULL,
          new Transform(
              Transform.translate(0.5, 0.5, 0.5),
              Transform.rotate(),
              Transform.scale(0.75, 0.75, 0.75)
          )
      );
    }

    @Override
    public boolean isEnabled() {

      TileCampfire tile = TileCampfire.this;
      return !tile.isDead() && !(tile.world.getTileEntity(tile.pos.up()) instanceof TileSoakingPot);
    }

    @Override
    protected boolean doItemStackValidation(ItemStack itemStack) {

      return CampfireRecipe.getRecipe(itemStack) != null;
    }

    @Override
    public boolean interact(EnumType type, TileCampfire tile, World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing hitSide, float hitX, float hitY, float hitZ) {

      if (player.isSneaking()
          && type == EnumType.MouseClick) {
        return false;
      }

      return super.interact(type, tile, world, hitPos, state, player, hand, hitSide, hitX, hitY, hitZ);
    }
  }

  public static class InteractionLog
      extends InteractionBase<TileCampfire> {

    private final TileCampfire tile;

    /* package */ InteractionLog(TileCampfire tile) {

      super(EnumFacing.VALUES, BlockCampfire.AABB_FULL);
      this.tile = tile;
    }

    public int getLogCount() {

      int firstEmptyIndex = this.tile.fuelStackHandler.getFirstEmptyIndex();
      return firstEmptyIndex == -1 ? this.tile.fuelStackHandler.getSlots() : firstEmptyIndex;
    }

    public ItemStack getLog(int slot) {

      return this.tile.fuelStackHandler.getStackInSlot(slot);
    }

    @Override
    public boolean isEnabled() {

      return !this.tile.isDead();
    }

    public boolean isItemStackValid(ItemStack itemStack) {

      if (itemStack.isEmpty()) {
        return false;
      }

      return this.doItemStackValidation(itemStack);
    }

    private boolean doItemStackValidation(ItemStack itemStack) {

      return ModuleTechBasicConfig.CAMPFIRE.isValidFuel(itemStack);
    }

    @Override
    public boolean interact(EnumType type, TileCampfire tile, World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing hitSide, float hitX, float hitY, float hitZ) {

      ItemStack heldItem = player.getHeldItemMainhand();

      if ((type == EnumType.MouseClick && !this.isItemStackValid(heldItem))
          || type == EnumType.MouseWheelDown) {

        // If the player is sneaking with an empty hand, or it's a mouse wheel
        // down type, remove logs and damage the player.

        ItemStack itemStack = tile.fuelStackHandler.extractItem(0, 1, world.isRemote);

        if (!itemStack.isEmpty()) {

          if (!world.isRemote) {

            if (ModuleTechBasicConfig.CAMPFIRE.PLAYER_LOG_BURN_DAMAGE > 0
                && Math.random() < ModuleTechBasicConfig.CAMPFIRE.PLAYER_BURN_CHANCE) {

              if (!player.isImmuneToFire()
                  && !EnchantmentHelper.hasFrostWalkerEnchantment(player)
                  && ModuleTechBasic.Blocks.CAMPFIRE.getActualState(state, world, hitPos).getValue(BlockCampfire.VARIANT) == BlockCampfire.EnumType.LIT) {
                player.attackEntityFrom(DamageSource.HOT_FLOOR, (float) ModuleTechBasicConfig.CAMPFIRE.PLAYER_LOG_BURN_DAMAGE);
              }
            }

            StackHelper.addToInventoryOrSpawn(world, player, itemStack, hitPos, -0.125, false, (type == EnumType.MouseClick));
          }

          return true;
        }

      } else if ((type == EnumType.MouseClick && !heldItem.isEmpty())
          || type == EnumType.MouseWheelUp) {

        // If the player is not sneaking with a full hand, attempt to add wood.

        if (this.isItemStackValid(heldItem)) {
          LIFOStackHandler fuelStackHandler = tile.fuelStackHandler;

          if (!world.isRemote) {
            int firstEmptyIndex = fuelStackHandler.getFirstEmptyIndex();

            if (firstEmptyIndex > -1) {
              fuelStackHandler.insertItem(0, new ItemStack(heldItem.getItem(), 1, heldItem.getMetadata()), false);
              world.playSound(null, hitPos, SoundEvents.BLOCK_WOOD_PLACE, SoundCategory.BLOCKS, 1, 1);

              if (!player.isCreative()) {
                heldItem.setCount(heldItem.getCount() - 1);
              }
            }
          }

          return true;
        }
      }

      return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderSolidPass(World world, RenderItem renderItem, BlockPos pos, IBlockState blockState, float partialTicks) {

      CampfireInteractionLogRenderer.INSTANCE.renderSolidPass(this, world, renderItem, pos, blockState, partialTicks);
    }

    @Override
    public boolean forceRenderAdditivePassWhileSneaking() {

      return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean renderAdditivePass(World world, RenderItem renderItem, EnumFacing hitSide, Vec3d hitVec, BlockPos hitPos, IBlockState blockState, ItemStack heldItemMainHand, float partialTicks) {

      return CampfireInteractionLogRenderer.INSTANCE.renderAdditivePass(this, world, renderItem, hitSide, hitVec, hitPos, blockState, heldItemMainHand, partialTicks);
    }

    @Override
    public boolean shouldRenderAdditivePassForHeldItem(ItemStack heldItemMainHand) {
      return this.isItemStackValid(heldItemMainHand);
    }

    @Override
    public boolean shouldRenderAdditivePassForStackInSlot(boolean sneaking, ItemStack heldItemMainHand) {
      Item item = heldItemMainHand.getItem();
      return !this.isItemStackValid(heldItemMainHand) && !(item instanceof ItemIgniterBase) && item != Items.FLINT_AND_STEEL && item != Items.FIRE_CHARGE;
    }
  }

  private static class InteractionShovel
      extends InteractionBase<TileCampfire> {

    /* package */ InteractionShovel() {

      super(EnumFacing.VALUES, BlockCampfire.AABB_FULL);
    }

    @Override
    public boolean interact(EnumType type, TileCampfire tile, World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing hitSide, float hitX, float hitY, float hitZ) {

      if (type != EnumType.MouseClick) {
        return false;
      }

      ItemStack heldItem = player.getHeldItemMainhand();

      if (tile.getAshLevel() > 0
          && heldItem.getItem().getToolClasses(heldItem).contains("shovel")) {

        if (!world.isRemote) {
          tile.setAshLevel(tile.getAshLevel() - 1);
          StackHelper.spawnStackOnTop(world, ItemMaterial.EnumType.PIT_ASH.asStack(), hitPos, 0);
          heldItem.damageItem(1, player);
          world.playSound(null, hitPos, SoundEvents.BLOCK_SAND_BREAK, SoundCategory.BLOCKS, 1, 1);
        }

        return true;
      }

      return false;
    }
  }

  // ---------------------------------------------------------------------------
  // - Stack Handlers
  // ---------------------------------------------------------------------------

  private class InputStackHandler
      extends ObservableStackHandler
      implements ITileDataItemStackHandler {

    /* package */ InputStackHandler() {

      super(1);
    }

    @Override
    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {

      return 1;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {

      if (stack.isEmpty()
          || CampfireRecipe.getRecipe(stack) == null
          || !TileCampfire.this.outputStackHandler.getStackInSlot(0).isEmpty()) {
        return stack;
      }

      return super.insertItem(slot, stack, simulate);
    }
  }

  private static class OutputStackHandler
      extends ObservableStackHandler
      implements ITileDataItemStackHandler {

    /* package */ OutputStackHandler() {

      super(1);
    }
  }

  private static class FuelStackHandler
      extends LIFOStackHandler
      implements ITileDataItemStackHandler {

    /* package */ FuelStackHandler() {

      super(8);
    }

    @Override
    protected int getStackLimit(int slot, @Nonnull ItemStack stack) {

      return 1;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {

      if (!ModuleTechBasicConfig.CAMPFIRE.isValidFuel(stack)) {
        return stack;
      }

      return super.insertItem(slot, stack, simulate);
    }
  }

}
