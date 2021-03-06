package com.codetaylor.mc.pyrotech.modules.tech.basic.tile;

import com.codetaylor.mc.athenaeum.inventory.DynamicStackHandler;
import com.codetaylor.mc.athenaeum.network.tile.data.TileDataFloat;
import com.codetaylor.mc.athenaeum.network.tile.data.TileDataItemStackHandler;
import com.codetaylor.mc.athenaeum.network.tile.spi.ITileData;
import com.codetaylor.mc.athenaeum.util.ArrayHelper;
import com.codetaylor.mc.athenaeum.util.StackHelper;
import com.codetaylor.mc.athenaeum.interaction.api.InteractionBounds;
import com.codetaylor.mc.athenaeum.interaction.api.Transform;
import com.codetaylor.mc.athenaeum.interaction.spi.IInteraction;
import com.codetaylor.mc.athenaeum.interaction.spi.ITileInteractable;
import com.codetaylor.mc.athenaeum.interaction.spi.InteractionItemStack;
import com.codetaylor.mc.athenaeum.interaction.spi.InteractionUseItemBase;
import com.codetaylor.mc.pyrotech.library.CompactingBinRecipeBase;
import com.codetaylor.mc.athenaeum.integration.gamestages.Stages;
import com.codetaylor.mc.athenaeum.network.tile.spi.TileEntityDataBase;
import com.codetaylor.mc.pyrotech.library.util.Util;
import com.codetaylor.mc.pyrotech.modules.core.network.SCPacketNoHunger;
import com.codetaylor.mc.pyrotech.modules.tech.basic.ModuleTechBasic;
import com.codetaylor.mc.pyrotech.modules.tech.basic.ModuleTechBasicConfig;
import com.codetaylor.mc.pyrotech.modules.tech.basic.client.render.CompactingBinInteractionInputRenderer;
import com.codetaylor.mc.pyrotech.modules.tech.basic.recipe.CompactingBinRecipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileCompactingBin
    extends TileEntityDataBase
    implements ITileInteractable {

  private InputStackHandler inputStackHandler;
  private TileDataFloat recipeProgress;
  private TileDataItemStackHandler tileDataInputStackHandler;
  private CompactingBinRecipeBase currentRecipe;
  private IInteraction[] interactions;

  public TileCompactingBin() {

    super(ModuleTechBasic.TILE_DATA_SERVICE);

    this.inputStackHandler = new InputStackHandler(this);
    this.inputStackHandler.addObserver((handler, slot) -> {

      this.updateRecipe();

      if (this.currentRecipe != null) {

        if (this.currentRecipe.getAmount() > this.inputStackHandler.getTotalItemCount()) {
          this.recipeProgress.set(0);
        }

      } else {
        this.recipeProgress.set(0);
      }
      this.markDirty();
    });

    this.recipeProgress = new TileDataFloat(0);

    // --- Network ---

    this.tileDataInputStackHandler = new TileDataItemStackHandler<>(this.inputStackHandler);

    this.registerTileDataForNetwork(new ITileData[]{
        this.tileDataInputStackHandler,
        this.recipeProgress
    });

    // --- Interactions ---

    this.interactions = new IInteraction[]{
        new InteractionInput(this, this.inputStackHandler),
        new InteractionShovel()
    };
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
      return (T) this.inputStackHandler;
    }

    return null;
  }

  // ---------------------------------------------------------------------------
  // - Network
  // ---------------------------------------------------------------------------

  @Override
  public void onTileDataUpdate() {

    if (this.tileDataInputStackHandler.isDirty()) {
      this.updateRecipe();
    }
  }

  // ---------------------------------------------------------------------------
  // - Recipe
  // ---------------------------------------------------------------------------

  private void updateRecipe() {

    ItemStack itemStack = this.inputStackHandler.getFirstNonEmptyItemStack();

    if (!itemStack.isEmpty()) {
      this.currentRecipe = this.getRecipe(itemStack);

    } else {
      this.currentRecipe = null;
    }
  }

  private boolean isItemValidForInsertion(ItemStack itemStack) {

    CompactingBinRecipeBase recipe = this.getRecipe(itemStack);

    if (recipe == null) {
      return false;
    }

    if (this.currentRecipe == null) {
      return true;
    }

    return (recipe == this.currentRecipe);
  }

  // ---------------------------------------------------------------------------
  // - Accessors
  // ---------------------------------------------------------------------------

  public CompactingBinRecipeBase getRecipe(ItemStack itemStack) {

    return CompactingBinRecipe.getRecipe(itemStack);
  }

  public float getRecipeProgress() {

    return this.recipeProgress.get();
  }

  public float addRecipeProgress(float progress) {

    return this.recipeProgress.add(progress);
  }

  public void resetRecipeProgress() {

    this.recipeProgress.set(0);
  }

  public InputStackHandler getInputStackHandler() {

    return this.inputStackHandler;
  }

  public CompactingBinRecipeBase getCurrentRecipe() {

    return this.currentRecipe;
  }

  protected boolean allowAutomation() {

    return ModuleTechBasicConfig.COMPACTING_BIN.ALLOW_AUTOMATION;
  }

  protected int getMinimumHungerToUse() {

    return ModuleTechBasicConfig.COMPACTING_BIN.MINIMUM_HUNGER_TO_USE;
  }

  protected String[] getShovelBlacklist() {

    return ModuleTechBasicConfig.COMPACTING_BIN.SHOVEL_BLACKLIST;
  }

  protected String[] getShovelWhitelist() {

    return ModuleTechBasicConfig.COMPACTING_BIN.SHOVEL_WHITELIST;
  }

  protected double getExhaustionCostPerHit() {

    return ModuleTechBasicConfig.COMPACTING_BIN.EXHAUSTION_COST_PER_HIT;
  }

  protected int getToolDamagePerCraft() {

    return ModuleTechBasicConfig.COMPACTING_BIN.TOOL_DAMAGE_PER_CRAFT;
  }

  protected double getExhaustionCostPerCraftComplete() {

    return ModuleTechBasicConfig.COMPACTING_BIN.EXHAUSTION_COST_PER_CRAFT_COMPLETE;
  }

  public int getInputCapacity() {

    return ModuleTechBasicConfig.COMPACTING_BIN.MAX_CAPACITY;
  }

  // ---------------------------------------------------------------------------
  // - Serialization
  // ---------------------------------------------------------------------------

  @Nonnull
  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {

    super.writeToNBT(compound);
    compound.setTag("inputStackHandler", this.inputStackHandler.serializeNBT());
    compound.setFloat("recipeProgress", this.recipeProgress.get());
    return compound;
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {

    super.readFromNBT(compound);
    this.inputStackHandler.deserializeNBT(compound.getCompoundTag("inputStackHandler"));
    this.recipeProgress.set(compound.getFloat("recipeProgress"));
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
  // - Interactions
  // ---------------------------------------------------------------------------

  @Nullable
  @Override
  public Stages getStages() {

    return ModuleTechBasicConfig.STAGES_COMPACTING_BIN;
  }

  @Override
  public IInteraction[] getInteractions() {

    return this.interactions;
  }

  public static class InteractionInput
      extends InteractionItemStack<TileCompactingBin> {

    private final TileCompactingBin tile;

    /* package */ InteractionInput(TileCompactingBin tile, ItemStackHandler stackHandler) {

      super(new ItemStackHandler[]{stackHandler}, 0, new EnumFacing[]{EnumFacing.UP}, InteractionBounds.BLOCK, new Transform(
          Transform.translate(0.5, 1.0, 0.5),
          Transform.rotate(),
          Transform.scale(0.75, 0.75, 0.75)
      ));
      this.tile = tile;
    }

    public TileCompactingBin getTile() {

      return this.tile;
    }

    @Override
    protected boolean doItemStackValidation(ItemStack itemStack) {

      return this.tile.isItemValidForInsertion(itemStack);
    }

    @Override
    protected void onInsert(EnumType type, ItemStack itemStack, World world, EntityPlayer player, BlockPos pos) {

      super.onInsert(type, itemStack, world, player, pos);

      if (!world.isRemote
          && type == EnumType.MouseClick) {
        world.playSound(
            null,
            pos.getX(),
            pos.getY(),
            pos.getZ(),
            SoundEvents.BLOCK_WOOD_PLACE,
            SoundCategory.BLOCKS,
            0.5f,
            (float) (1 + Util.RANDOM.nextGaussian() * 0.4f)
        );
      }
    }

    @Override
    public void renderSolidPass(World world, RenderItem renderItem, BlockPos pos, IBlockState blockState, float partialTicks) {

      CompactingBinInteractionInputRenderer.INSTANCE.renderSolidPass(this, world, renderItem, pos, blockState, partialTicks);
    }

    @Override
    public void renderSolidPassText(World world, FontRenderer fontRenderer, int yaw, Vec3d offset, BlockPos pos, IBlockState blockState, float partialTicks) {

      CompactingBinInteractionInputRenderer.INSTANCE.renderSolidPassText(this, world, fontRenderer, yaw, offset, pos, blockState, partialTicks);
    }

    @Override
    public boolean renderAdditivePass(World world, RenderItem renderItem, EnumFacing hitSide, Vec3d hitVec, BlockPos hitPos, IBlockState blockState, ItemStack heldItemMainHand, float partialTicks) {

      return CompactingBinInteractionInputRenderer.INSTANCE.renderAdditivePass(this, world, renderItem, hitSide, hitVec, hitPos, blockState, heldItemMainHand, partialTicks);
    }
  }

  private class InteractionShovel
      extends InteractionUseItemBase<TileCompactingBin> {

    /* package */ InteractionShovel() {

      super(new EnumFacing[]{EnumFacing.UP}, InteractionBounds.BLOCK);
    }

    @Override
    protected boolean allowInteraction(TileCompactingBin tile, World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing hitSide, float hitX, float hitY, float hitZ) {

      if (player.getFoodStats().getFoodLevel() < tile.getMinimumHungerToUse()) {

        if (!world.isRemote) {
          ModuleTechBasic.PACKET_SERVICE.sendTo(new SCPacketNoHunger(), (EntityPlayerMP) player);
        }
        return false;
      }

      if (tile.currentRecipe == null
          || tile.currentRecipe.getAmount() > tile.getInputStackHandler().getTotalItemCount()) {
        return false;
      }

      ItemStack heldItemStack = player.getHeldItem(hand);
      Item heldItem = heldItemStack.getItem();
      ResourceLocation resourceLocation = heldItem.getRegistryName();

      if (resourceLocation == null) {
        return false;
      }

      String registryName = resourceLocation.toString();

      if (heldItem.getToolClasses(heldItemStack).contains("shovel")) {
        return !ArrayHelper.contains(tile.getShovelBlacklist(), registryName);

      } else {
        return ArrayHelper.contains(tile.getShovelWhitelist(), registryName);
      }
    }

    @Override
    protected void applyItemDamage(ItemStack itemStack, EntityPlayer player) {

      // We apply our own item damage on recipe completion.
    }

    @Override
    protected boolean doInteraction(TileCompactingBin tile, World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing hitSide, float hitX, float hitY, float hitZ) {

      ItemStack heldItem = player.getHeldItemMainhand();

      if (!world.isRemote) {

        if (tile.getExhaustionCostPerHit() > 0) {
          player.addExhaustion((float) tile.getExhaustionCostPerHit());
        }

        int harvestLevel = heldItem.getItem().getHarvestLevel(heldItem, "shovel", player, null);
        int[] requiredToolUses = tile.currentRecipe.getRequiredToolUses();
        tile.recipeProgress.add(1f / ArrayHelper.getOrLast(requiredToolUses, harvestLevel));

        if (tile.recipeProgress.get() > 0.9999) {
          // recipe complete
          StackHelper.spawnStackOnTop(world, tile.currentRecipe.getOutput(), hitPos, 1.0);

          // reduce input items
          tile.getInputStackHandler().removeItems(tile.currentRecipe.getAmount());

          // damage tool
          heldItem.damageItem(tile.getToolDamagePerCraft(), player);

          if (tile.getExhaustionCostPerCraftComplete() > 0) {
            player.addExhaustion((float) tile.getExhaustionCostPerCraftComplete());
          }

          // reset recipe progress
          tile.recipeProgress.set(0);
        }
      }

      return true;
    }
  }

  // ---------------------------------------------------------------------------
  // - Stack Handlers
  // ---------------------------------------------------------------------------

  public class InputStackHandler
      extends DynamicStackHandler {

    private final TileCompactingBin tile;

    /* package */ InputStackHandler(TileCompactingBin tile) {

      super(1);
      this.tile = tile;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {

      if (!this.tile.isItemValidForInsertion(stack)) {
        return stack; // item is not valid for insertion, fail
      }

      CompactingBinRecipeBase recipe = this.tile.getRecipe(stack);

      if (recipe == null) {
        // This should never happen because the item's recipe is checked above.
        return stack; // item has no recipe, fail
      }

      int max = this.tile.getInputCapacity() * recipe.getAmount();
      int currentTotal = this.tile.getInputStackHandler().getTotalItemCount();

      if (currentTotal == max) {
        return stack; // There's no room for insert, fail

      } else if (currentTotal + stack.getCount() <= max) {
        // There's enough room for all items in the stack
        this.insertItem(stack, simulate);
        return ItemStack.EMPTY;

      } else {
        // Trim the input stack down to size and insert
        ItemStack toInsert = stack.copy();
        int insertCount = max - currentTotal;
        toInsert.setCount(insertCount);
        this.insertItem(toInsert, simulate);
        ItemStack toReturn = stack.copy();
        toReturn.setCount(toReturn.getCount() - insertCount);
        return toReturn;
      }
    }

    public int removeItems(int amount) {

      int remaining = amount;

      for (int i = this.getSlots() - 1; i >= 0; i--) {

        if (!this.getStackInSlot(i).isEmpty()) {
          remaining -= super.extractItem(i, remaining, false).getCount();

          if (remaining == 0) {
            return amount;
          }
        }
      }

      return amount - remaining;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {

      for (int i = this.getSlots() - 1; i >= 0; i--) {

        if (!this.getStackInSlot(i).isEmpty()) {
          return super.extractItem(i, amount, simulate);
        }
      }

      return ItemStack.EMPTY;
    }
  }
}
