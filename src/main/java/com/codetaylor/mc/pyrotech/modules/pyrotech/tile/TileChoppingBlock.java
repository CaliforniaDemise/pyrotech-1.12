package com.codetaylor.mc.pyrotech.modules.pyrotech.tile;

import com.codetaylor.mc.athenaeum.util.ArrayHelper;
import com.codetaylor.mc.athenaeum.util.BlockHelper;
import com.codetaylor.mc.athenaeum.util.StackHelper;
import com.codetaylor.mc.pyrotech.modules.pyrotech.ModulePyrotechConfig;
import com.codetaylor.mc.pyrotech.modules.pyrotech.block.BlockChoppingBlock;
import com.codetaylor.mc.pyrotech.modules.pyrotech.block.BlockRock;
import com.codetaylor.mc.pyrotech.modules.pyrotech.client.render.Transform;
import com.codetaylor.mc.pyrotech.modules.pyrotech.init.ModuleBlocks;
import com.codetaylor.mc.pyrotech.modules.pyrotech.interaction.*;
import com.codetaylor.mc.pyrotech.modules.pyrotech.recipe.ChoppingBlockRecipe;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileChoppingBlock
    extends TileEntity
    implements ITileInteractable {

  private ItemStackHandler stackHandler;
  private int sawdust;
  private int durabilityUntilNextDamage;
  private float recipeProgress;

  // transient
  private IInteraction[] interactions;

  public TileChoppingBlock() {

    this.stackHandler = new InputStackHandler();
    this.interactions = new IInteraction[]{
        new Interaction(new ItemStackHandler[]{this.stackHandler}),
        new InteractionShovel(),
        new InteractionChop()
    };
    this.durabilityUntilNextDamage = ModulePyrotechConfig.CHOPPING_BLOCK.CHOPS_PER_DAMAGE;
  }

  private class InputStackHandler
      extends ItemStackHandler {

    public InputStackHandler() {

      super(1);
    }

    @Override
    public int getSlotLimit(int slot) {

      return 1;
    }

    @Override
    protected void onContentsChanged(int slot) {

      TileChoppingBlock _this = TileChoppingBlock.this;

      // We explicitly don't use the setter here to avoid calling markDirty and
      // notifyBlockUpdate redundantly.
      _this.recipeProgress = 0;

      _this.markDirty();
      BlockHelper.notifyBlockUpdate(_this.world, _this.pos);
    }
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

  public void setSawdust(int sawdust) {

    // This requires a full update because it is used for actual state.

    this.sawdust = Math.max(0, Math.min(5, sawdust));
    this.markDirty();
    BlockHelper.notifyBlockUpdate(this.world, this.pos);
  }

  public int getSawdust() {

    return this.sawdust;
  }

  public void setDamage(int damage) {

    this.world.setBlockState(this.pos, ModuleBlocks.CHOPPING_BLOCK.getDefaultState()
        .withProperty(BlockChoppingBlock.DAMAGE, damage), 3);
  }

  public int getDamage() {

    return this.world.getBlockState(this.pos).getValue(BlockChoppingBlock.DAMAGE);
  }

  public void setDurabilityUntilNextDamage(int durabilityUntilNextDamage) {

    // TODO: Network
    // This doesn't require a full update.

    this.durabilityUntilNextDamage = durabilityUntilNextDamage;
    this.markDirty();
    BlockHelper.notifyBlockUpdate(this.world, this.pos);
  }

  public int getDurabilityUntilNextDamage() {

    return this.durabilityUntilNextDamage;
  }

  public void setRecipeProgress(float recipeProgress) {

    // TODO: Network
    // This doesn't require a full update.

    this.recipeProgress = recipeProgress;
    this.markDirty();
    BlockHelper.notifyBlockUpdate(this.world, this.pos);
  }

  public float getRecipeProgress() {

    return this.recipeProgress;
  }

  public ItemStackHandler getStackHandler() {

    return this.stackHandler;
  }

  // ---------------------------------------------------------------------------
  // - Serialization
  // ---------------------------------------------------------------------------

  @Nonnull
  @Override
  public NBTTagCompound writeToNBT(NBTTagCompound compound) {

    super.writeToNBT(compound);
    compound.setTag("stackHandler", this.stackHandler.serializeNBT());
    compound.setInteger("sawdust", this.sawdust);
    compound.setInteger("durabilityUntilNextDamage", this.durabilityUntilNextDamage);
    compound.setFloat("recipeProgress", this.recipeProgress);
    return compound;
  }

  @Override
  public void readFromNBT(NBTTagCompound compound) {

    super.readFromNBT(compound);
    this.stackHandler.deserializeNBT(compound.getCompoundTag("stackHandler"));
    this.sawdust = compound.getInteger("sawdust");
    this.durabilityUntilNextDamage = compound.getInteger("durabilityUntilNextDamage");
    this.recipeProgress = compound.getFloat("recipeProgress");
  }

  // ---------------------------------------------------------------------------
  // - Network
  // ---------------------------------------------------------------------------

  @Nonnull
  @Override
  public NBTTagCompound getUpdateTag() {

    return this.writeToNBT(new NBTTagCompound());
  }

  @Nullable
  @Override
  public SPacketUpdateTileEntity getUpdatePacket() {

    return new SPacketUpdateTileEntity(this.pos, -1, this.getUpdateTag());
  }

  @Override
  public void onDataPacket(NetworkManager networkManager, SPacketUpdateTileEntity packet) {

    this.readFromNBT(packet.getNbtCompound());
    BlockHelper.notifyBlockUpdate(this.world, this.pos);
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

  @Override
  public IInteraction[] getInteractions() {

    return this.interactions;
  }

  private class Interaction
      extends InteractionItemStack<TileChoppingBlock> {

    /* package */ Interaction(ItemStackHandler[] stackHandlers) {

      super(stackHandlers, 0, new EnumFacing[]{EnumFacing.UP}, InteractionBounds.INFINITE, new Transform(
          Transform.translate(0.5, 0.75, 0.5),
          Transform.rotate(),
          Transform.scale(0.75, 0.75, 0.75)
      ));
    }

    @Override
    protected boolean doItemStackValidation(ItemStack itemStack) {

      return (ChoppingBlockRecipe.getRecipe(itemStack) != null);
    }
  }

  private class InteractionShovel
      extends InteractionUseItemBase<TileChoppingBlock> {

    /* package */ InteractionShovel() {

      super(EnumFacing.VALUES, InteractionBounds.INFINITE);
    }

    @Override
    protected boolean allowInteraction(TileChoppingBlock tile, World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing hitSide, float hitX, float hitY, float hitZ) {

      ItemStack heldItemStack = player.getHeldItem(hand);

      return tile.getSawdust() > 0
          && heldItemStack.getItem().getToolClasses(heldItemStack).contains("shovel");
    }

    @Override
    protected boolean doInteraction(TileChoppingBlock tile, World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing hitSide, float hitX, float hitY, float hitZ) {

      ItemStack heldItem = player.getHeldItemMainhand();

      if (!world.isRemote) {
        tile.setSawdust(tile.getSawdust() - 1);
        StackHelper.spawnStackOnTop(world, new ItemStack(ModuleBlocks.ROCK, 1, BlockRock.EnumType.WOOD_CHIPS.getMeta()), hitPos, 0);
        heldItem.damageItem(1, player);
        world.playSound(null, hitPos, SoundEvents.BLOCK_SAND_BREAK, SoundCategory.BLOCKS, 1, 1);
      }

      return true;
    }
  }

  private class InteractionChop
      extends InteractionUseItemBase<TileChoppingBlock> {

    /* package */ InteractionChop() {

      super(new EnumFacing[]{EnumFacing.UP}, InteractionBounds.INFINITE);
    }

    @Override
    protected boolean allowInteraction(TileChoppingBlock tile, World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing hitSide, float hitX, float hitY, float hitZ) {

      ItemStack heldItemStack = player.getHeldItem(hand);
      Item heldItem = heldItemStack.getItem();

      ResourceLocation resourceLocation = heldItem.getRegistryName();

      if (resourceLocation == null) {
        return false;
      }

      String registryName = resourceLocation.toString();

      if (heldItem.getToolClasses(heldItemStack).contains("axe")) {
        return !ArrayHelper.contains(ModulePyrotechConfig.CHOPPING_BLOCK.AXE_BLACKLIST, registryName);

      } else {
        return ArrayHelper.contains(ModulePyrotechConfig.CHOPPING_BLOCK.AXE_WHITELIST, registryName);
      }
    }

    @Override
    protected boolean doInteraction(TileChoppingBlock tile, World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing hitSide, float hitX, float hitY, float hitZ) {

      if (!world.isRemote) {

        // Server logic

        // Decrement the chopping block's damage and reset the chops
        // remaining until next damage. If the damage reaches the threshold,
        // destroy the block and drop its contents.

        if (tile.getDurabilityUntilNextDamage() <= 1) {

          tile.setDurabilityUntilNextDamage(ModulePyrotechConfig.CHOPPING_BLOCK.CHOPS_PER_DAMAGE);

          if (tile.getDamage() + 1 < 6) {
            tile.setDamage(tile.getDamage() + 1);

          } else {
            StackHelper.spawnStackHandlerContentsOnTop(world, tile.getStackHandler(), tile.getPos());
            world.destroyBlock(tile.getPos(), false);
            return true;
          }
        }

        // Spread wood chips.

        BlockHelper.forBlocksInCube(world, tile.getPos(), 1, 1, 1, (w, p, bs) -> {

          if (Math.random() < ModulePyrotechConfig.CHOPPING_BLOCK.WOOD_CHIPS_CHANCE) {

            if (w.getTileEntity(p) == tile
                && tile.getSawdust() < 5) {
              tile.setSawdust(tile.getSawdust() + 1);
              return false;
            }

            if (w.isAirBlock(p)
                && ModuleBlocks.ROCK.canPlaceBlockAt(w, p)
                && bs.getBlock() != ModuleBlocks.ROCK) {

              w.setBlockState(p, ModuleBlocks.ROCK.getDefaultState()
                  .withProperty(BlockRock.VARIANT, BlockRock.EnumType.WOOD_CHIPS));
              return false;
            }
          }

          return true;
        });

        // Decrement the durability until next damage and progress or
        // complete the recipe.

        tile.setDurabilityUntilNextDamage(tile.getDurabilityUntilNextDamage() - 1);

        if (tile.getRecipeProgress() < 1) {

          // Check the recipe's harvest level and advance recipe progress.

          ItemStack heldItem = player.getHeldItem(hand);
          int harvestLevel = heldItem.getItem().getHarvestLevel(heldItem, "axe", player, null);
          int[] chops = ModulePyrotechConfig.CHOPPING_BLOCK.CHOPS_REQUIRED_PER_HARVEST_LEVEL;

          if (chops.length > 0) {
            float increment = 1f / ArrayHelper.getOrLast(chops, harvestLevel);
            tile.setRecipeProgress(tile.getRecipeProgress() + increment);
          }
        }

        if (tile.getRecipeProgress() >= 0.9999) {
          ItemStackHandler stackHandler = tile.getStackHandler();
          ItemStack itemStack = stackHandler.extractItem(0, stackHandler.getSlotLimit(0), false);
          ChoppingBlockRecipe recipe = ChoppingBlockRecipe.getRecipe(itemStack);

          if (recipe != null) {
            StackHelper.spawnStackOnTop(world, recipe.getOutput(), tile.getPos(), 0);
          }

          tile.markDirty();
          BlockHelper.notifyBlockUpdate(world, tile.getPos());
        }

      } else {

        // Client particles

        IBlockState blockState = ModuleBlocks.CHOPPING_BLOCK.getDefaultState();

        for (int i = 0; i < 8; ++i) {
          world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, tile.getPos().getX() + hitX, tile.getPos().getY() + hitY, tile.getPos().getZ() + hitZ, 0.0D, 0.0D, 0.0D, Block.getStateId(blockState));
        }
      }

      return true;
    }
  }
}
