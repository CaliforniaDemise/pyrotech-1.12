package com.codetaylor.mc.athenaeum.interaction.spi;

import com.codetaylor.mc.athenaeum.interaction.api.InteractionRenderers;
import com.codetaylor.mc.athenaeum.interaction.api.Transform;
import com.codetaylor.mc.athenaeum.util.RandomHelper;
import com.codetaylor.mc.athenaeum.util.StackHelper;
import com.codetaylor.mc.pyrotech.library.spi.block.IBlockIgnitableWithIgniterItem;
import com.codetaylor.mc.pyrotech.modules.ignition.item.ItemIgniterBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;

import java.util.Iterator;

public class InteractionItemStack <T extends TileEntity & ITileInteractable> extends InteractionBase<T> implements IInteractionItemStack<T> {
    private static final Vec3d TEXT_OFFSET = new Vec3d(0.0, 0.1, 0.0);
    protected final ItemStackHandler[] stackHandlers;
    protected final int slot;
    protected final Transform transform;
    protected ItemStack lastItemChecked;
    protected boolean lastItemValid;

    public InteractionItemStack(ItemStackHandler[] stackHandlers, int slot, EnumFacing[] sides, AxisAlignedBB bounds, Transform transform) {
        super(sides, bounds);
        this.stackHandlers = stackHandlers;
        this.slot = slot;
        this.transform = transform;
    }

    public Transform getTransform(World world, BlockPos pos, IBlockState blockState, ItemStack itemStack, float partialTicks) {
        return this.transform;
    }

    public ItemStack getStackInSlot() {
        for(int i = 0; i < this.stackHandlers.length; ++i) {
            ItemStack itemStack = this.stackHandlers[i].getStackInSlot(this.slot);
            if (!itemStack.isEmpty()) {
                return itemStack;
            }
        }

        return ItemStack.EMPTY;
    }

    public ItemStack extract(int amount, boolean simulate) {
        for(int i = 0; i < this.stackHandlers.length; ++i) {
            ItemStack itemStack = this.stackHandlers[i].getStackInSlot(this.slot);
            if (!itemStack.isEmpty()) {
                return this.stackHandlers[i].extractItem(this.slot, amount, simulate);
            }
        }

        return ItemStack.EMPTY;
    }

    public ItemStack insert(ItemStack itemStack, boolean simulate) {
        if (!this.isItemStackValid(itemStack)) {
            return itemStack;
        } else {
            for(int i = 0; i < this.stackHandlers.length; ++i) {
                int count = itemStack.getCount();
                ItemStack result = this.stackHandlers[i].insertItem(this.slot, itemStack, true);
                if (result.getCount() != count) {
                    return this.stackHandlers[i].insertItem(this.slot, itemStack, simulate);
                }
            }

            return itemStack;
        }
    }

    public boolean isEmpty() {
        return this.getStackInSlot().isEmpty();
    }

    public boolean isItemStackValid(ItemStack itemStack) {
        return itemStack.isEmpty() ? false : this.doItemStackValidation(itemStack);
    }

    protected boolean doItemStackValidation(ItemStack itemStack) {
        return true;
    }

    public boolean interact(IInteraction.EnumType type, T tile, World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing hitSide, float hitX, float hitY, float hitZ) {
        if (this.allowInteractionWithHand(hand) && this.allowInteractionWithType(type)) {
            BlockPos tilePos = tile.getPos();
            ItemStack heldItem = player.getHeldItem(hand);
            if ((this.isItemStackValid(heldItem) || type != EnumType.MouseClick) && type != EnumType.MouseWheelDown) {
                ItemStack itemStackToInsert = heldItem;
                int insertIndex = this.getInsertionIndex(tile, world, hitPos, state, player, hand, hitSide, hitX, hitY, hitZ);
                ItemStack alternateItemStack = null;
                if (!this.isEmpty() && type == EnumType.MouseWheelUp) {
                    ItemStack result = this.stackHandlers[insertIndex].insertItem(this.slot, itemStackToInsert, true);
                    if (result.getCount() == itemStackToInsert.getCount()) {
                        Iterator var18 = player.inventory.mainInventory.iterator();

                        while(var18.hasNext()) {
                            ItemStack itemStack = (ItemStack)var18.next();
                            result = this.stackHandlers[insertIndex].insertItem(this.slot, itemStack, true);
                            if (result.getCount() != itemStack.getCount()) {
                                alternateItemStack = itemStack;
                                break;
                            }
                        }
                    }
                }

                if (alternateItemStack != null) {
                    itemStackToInsert = alternateItemStack;
                }

                int count = itemStackToInsert.getCount();
                ItemStack itemStack = itemStackToInsert.copy();
                int insertItemCount = this.getInsertItemCount(type, itemStack);
                itemStack.setCount(insertItemCount);
                ItemStack result = this.stackHandlers[insertIndex].insertItem(this.slot, itemStack, world.isRemote);
                if (result.getCount() != count) {
                    int actualInsertCount = insertItemCount - result.getCount();
                    if (!world.isRemote) {
                        itemStackToInsert.setCount(itemStackToInsert.getCount() - actualInsertCount);
                    }

                    itemStack.setCount(actualInsertCount);
                    this.onInsert(type, itemStack, world, player, hitPos);
                    return true;
                }

                if (!this.isEmpty() && type == EnumType.MouseClick) {
                    ItemStack slotStack = this.stackHandlers[insertIndex].getStackInSlot(this.slot);
                    if (!ItemStack.areItemStacksEqual(itemStackToInsert, slotStack) || slotStack.getCount() == slotStack.getMaxStackSize()) {
                        if (this.doExtract(type, world, player, tilePos)) {
                            this.onExtract(type, world, player, tilePos);
                            return true;
                        }
                    }
                }

            } else if (!this.isEmpty()) {
                if (this.doExtract(type, world, player, tilePos)) {
                    this.onExtract(type, world, player, tilePos);
                    return true;
                }

                return false;
            }

            return false;
        } else {
            return false;
        }
    }

    protected int getInsertItemCount(IInteraction.EnumType type, ItemStack itemStack) {
        return type == EnumType.MouseWheelUp ? 1 : itemStack.getCount();
    }

    protected void onInsert(IInteraction.EnumType type, ItemStack itemStack, World world, EntityPlayer player, BlockPos pos) {
    }

    protected void onExtract(IInteraction.EnumType type, World world, EntityPlayer player, BlockPos pos) {
        if (!world.isRemote && type == EnumType.MouseClick) {
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, 0.25F, (float)(1.0 + RandomHelper.random().nextGaussian() * 0.4000000059604645));
        }

    }

    protected boolean doExtract(IInteraction.EnumType type, World world, EntityPlayer player, BlockPos tilePos) {
        int extractItemCount;
        if (type == EnumType.MouseWheelDown) {
            extractItemCount = 1;
        } else {
            extractItemCount = Integer.MAX_VALUE;
        }

        ItemStack result = this.extract(extractItemCount, world.isRemote);
        if (!result.isEmpty()) {
            if (!world.isRemote) {
                StackHelper.addToInventoryOrSpawn(world, player, result, tilePos, 1.0, false, type == EnumType.MouseClick);
            }

            return true;
        } else {
            return false;
        }
    }

    protected int getInsertionIndex(T tile, World world, BlockPos hitPos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing hitSide, float hitX, float hitY, float hitZ) {
        return 0;
    }

    @SideOnly(Side.CLIENT)
    public void renderSolidPass(World world, RenderItem renderItem, BlockPos pos, IBlockState blockState, float partialTicks) {
        InteractionRenderers.ITEM_STACK.renderSolidPass(this, world, renderItem, pos, blockState, partialTicks);
    }

    @SideOnly(Side.CLIENT)
    public void renderSolidPassText(World world, FontRenderer fontRenderer, int yaw, Vec3d offset, BlockPos pos, IBlockState blockState, float partialTicks) {
        InteractionRenderers.ITEM_STACK.renderSolidPassText(this, world, fontRenderer, yaw, offset, pos, blockState, partialTicks);
    }

    public Vec3d getTextOffset(EnumFacing tileFacing, EnumFacing playerHorizontalFacing, EnumFacing sideHit) {
        return TEXT_OFFSET;
    }

    @SideOnly(Side.CLIENT)
    public boolean renderAdditivePass(World world, RenderItem renderItem, EnumFacing hitSide, Vec3d hitVec, BlockPos hitPos, IBlockState blockState, ItemStack heldItemMainHand, float partialTicks) {
        return InteractionRenderers.ITEM_STACK.renderAdditivePass(this, world, renderItem, hitSide, hitVec, hitPos, blockState, heldItemMainHand, partialTicks);
    }

    public boolean forceRenderAdditivePassWhileSneaking() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldRenderAdditivePassForHeldItem(ItemStack heldItemMainHand) {
        return this.doItemStackValidation(heldItemMainHand);
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldRenderAdditivePassForStackInSlot(boolean sneaking, ItemStack heldItemMainHand) {
        return true;
    }
}
