package com.codetaylor.mc.pyrotech.modules.tech.basic.item;

import com.codetaylor.mc.athenaeum.util.StackHelper;
import com.codetaylor.mc.pyrotech.modules.tech.basic.ModuleTechBasic;
import com.codetaylor.mc.pyrotech.modules.tech.basic.ModuleTechBasicConfig;
import com.codetaylor.mc.pyrotech.modules.tech.basic.network.SCPacketMarshmallowStickTimestamp;
import com.codetaylor.mc.pyrotech.modules.tech.basic.tile.TileCampfire;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;

public class ItemMarshmallowStick
    extends ItemFood {

  public static final String NAME = "marshmallow_stick";

  private static final MethodHandle entityLivingBase$activeItemStackUseCountSetter;

  static {

    try {

      entityLivingBase$activeItemStackUseCountSetter = MethodHandles.lookup().unreflectSetter(
          /*
          MC 1.12: net/minecraft/entity/EntityLivingBase.activeItemStackUseCount
          Name: bp => field_184628_bn => activeItemStackUseCount
          Comment: None
          Side: BOTH
          AT: public net.minecraft.entity.EntityLivingBase field_184628_bn # activeItemStackUseCount
          */
          ObfuscationReflectionHelper.findField(EntityLivingBase.class, "field_184628_bn")
      );

    } catch (Throwable t) {
      throw new RuntimeException(String.format("Error unreflecting setter for field %s", "field_184628_bn"), t);
    }
  }

  private static final int ROASTING_DURATION = 10 * 60 * 20;
  private static final int EATING_DURATION = 32;

  public ItemMarshmallowStick() {

    super(
        ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.ROASTED_MARSHMALLOW_HUNGER,
        (float) ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.ROASTED_MARSHMALLOW_SATURATION,
        false
    );
    this.setMaxStackSize(1);
    this.setMaxDamage(ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.MARSHMALLOW_STICK_DURABILITY);

    this.addPropertyOverride(
        new ResourceLocation(ModuleTechBasic.MOD_ID, "marshmallow_type"),
        (itemStack, world, entity) -> ItemMarshmallowStick.getType(itemStack).id
    );
  }

  // ---------------------------------------------------------------------------
  // - Accessors
  // ---------------------------------------------------------------------------

  @Override
  public int getMaxDamage(ItemStack stack) {

    return ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.MARSHMALLOW_STICK_DURABILITY;
  }

  @Override
  public int getMaxItemUseDuration(ItemStack stack) {

    if (ItemMarshmallowStick.getRoastByTimestamp(stack) < Long.MAX_VALUE) {
      // This means we've aimed it at a fire and we're roasting.
      return ROASTING_DURATION;
    }

    // If eating return a duration of 32
    return EATING_DURATION;
  }

  @Nonnull
  @Override
  public EnumAction getItemUseAction(ItemStack stack) {

    if (ItemMarshmallowStick.getRoastByTimestamp(stack) < Long.MAX_VALUE) {
      // This means we've aimed it at a fire and we're roasting.
      return EnumAction.DRINK;
    }

    return EnumAction.EAT;
  }

  @Nonnull
  @Override
  public String getUnlocalizedName(ItemStack stack) {

    EnumType type = ItemMarshmallowStick.getType(stack);

    if (type == EnumType.MARSHMALLOW) {
      return "item.pyrotech.marshmallow.on.stick";

    } else if (type == EnumType.MARSHMALLOW_BURNED) {
      return "item.pyrotech.marshmallow.on.stick.burned";

    } else if (type == EnumType.MARSHMALLOW_ROASTED) {
      return "item.pyrotech.marshmallow.on.stick.roasted";
    }

    return super.getUnlocalizedName(stack);
  }

  @Override
  public boolean shouldCauseReequipAnimation(ItemStack oldStack, @Nonnull ItemStack newStack, boolean slotChanged) {

    return ItemMarshmallowStick.getType(oldStack) != ItemMarshmallowStick.getType(newStack);
  }

  @Nullable
  @Override
  public NBTTagCompound getNBTShareTag(ItemStack stack) {

    // We remove the timestamp here because when it is sent to the client,
    // it replaces the item on the client, and it will interrupt the item
    // being used. We send a custom packet to the client and update this
    // value manually to bypass the interrupt.

    NBTTagCompound tag = super.getNBTShareTag(stack);

    if (tag != null) {
      tag = tag.copy();
      tag.removeTag("RoastByTimestamp");
    }
    return tag;
  }

  // ---------------------------------------------------------------------------
  // - Creative Menu
  // ---------------------------------------------------------------------------

  @Nullable
  @Override
  public CreativeTabs getCreativeTab() {

    return null;
  }

  // ---------------------------------------------------------------------------
  // - NBT Accessors
  // ---------------------------------------------------------------------------

  public static void setType(EnumType type, ItemStack itemStack) {

    NBTTagCompound tag = StackHelper.getTagSafe(itemStack);
    tag.setInteger("MarshmallowType", type.id);
    itemStack.setTagCompound(tag);
  }

  public static EnumType getType(ItemStack itemStack) {

    NBTTagCompound tag = StackHelper.getTagSafe(itemStack);
    return EnumType.from(tag.getInteger("MarshmallowType"));
  }

  public static void setRoastByTimestamp(ItemStack itemStack, long timestamp) {

    NBTTagCompound tag = StackHelper.getTagSafe(itemStack);
    tag.setLong("RoastByTimestamp", timestamp);
    itemStack.setTagCompound(tag);
  }

  public static long getRoastByTimestamp(ItemStack itemStack) {

    NBTTagCompound tag = StackHelper.getTagSafe(itemStack);

    if (!tag.hasKey("RoastByTimestamp")) {
      return Long.MAX_VALUE;
    }

    return tag.getLong("RoastByTimestamp");
  }

  public static void setRoastedAtTimestamp(ItemStack itemStack, long timestamp) {

    NBTTagCompound tag = StackHelper.getTagSafe(itemStack);
    tag.setLong("RoastedAtTimestamp", timestamp);
    itemStack.setTagCompound(tag);
  }

  public static long getRoastedAtTimestamp(ItemStack itemStack) {

    NBTTagCompound tag = StackHelper.getTagSafe(itemStack);

    if (!tag.hasKey("RoastedAtTimestamp")) {
      return 0;
    }

    return tag.getLong("RoastedAtTimestamp");
  }

  // ---------------------------------------------------------------------------
  // - Interaction
  // ---------------------------------------------------------------------------

  @Nonnull
  @Override
  public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand) {

    if (hand == EnumHand.MAIN_HAND) {
      ItemStack itemMainHand = player.getHeldItemMainhand();
      ItemStack itemOffhand = player.getHeldItemOffhand();

      // -----------------------------------------------------------------------
      // - Roast
      // -----------------------------------------------------------------------

      RayTraceResult rayTraceResult = this.rayTrace(world, player, false);

      // If this is a roasted marshmallow, we need to check that the player
      // has stopped using it and, if they have, prevent them from using it again.
      // This will prevent players from being able to start roasting an already
      // roasted marshmallow.
      //
      // We can check for the roasted timestamp.
      if (ItemMarshmallowStick.getRoastedAtTimestamp(itemMainHand) == 0) {

        // ray trace result can be null
        //noinspection ConstantConditions
        if (rayTraceResult != null
            && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK
            && this.isRoastingBlock(world, rayTraceResult.getBlockPos())
            && this.isWithinRoastingRange(player, rayTraceResult.getBlockPos())) {

          // Here we send a packet to the client instead of setting the timestamp
          // directly. This avoids triggering the new item or reequip animation
          // and stuttering the experience.
          if (!world.isRemote && ItemMarshmallowStick.getRoastByTimestamp(itemMainHand) == Long.MAX_VALUE) {

            int roastDuration = ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.ROASTING_DURATION_TICKS;
            double roastVariance = (world.rand.nextDouble() * 2 - 1) * ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.ROASTING_DURATION_VARIANCE_PERCENTAGE;
            roastDuration = Math.max(0, (int) (roastDuration + roastDuration * roastVariance));

            long timestamp = world.getTotalWorldTime() + roastDuration;
            ItemMarshmallowStick.setRoastByTimestamp(itemMainHand, timestamp);
            ModuleTechBasic.PACKET_SERVICE.sendToAll(new SCPacketMarshmallowStickTimestamp(player, timestamp));
          }

          player.setActiveHand(hand);

          if (world.isRemote) {

            try {
              // This will prevent the client from using the initial duration set before
              // the packet arrives indicating that we're actually roasting.
              entityLivingBase$activeItemStackUseCountSetter.invokeExact((EntityLivingBase) player, ROASTING_DURATION);

            } catch (Throwable t) {
              throw new RuntimeException("Error setting activeItemStackUseCount", t);
            }
          }

          return new ActionResult<>(EnumActionResult.SUCCESS, itemMainHand);
        }
      }

      // -----------------------------------------------------------------------
      // - Eat
      // -----------------------------------------------------------------------

      ActionResult<ItemStack> eatActionResult = super.onItemRightClick(world, player, hand);

      if (!player.isSneaking() && eatActionResult.getType() == EnumActionResult.SUCCESS) {
        return eatActionResult;
      }

      // -----------------------------------------------------------------------
      // - Remove Marshmallow
      // -----------------------------------------------------------------------

      if (player.isSneaking()) {
        // Try removing a marshmallow from the stick.
        EnumType type = ItemMarshmallowStick.getType(itemMainHand);

        if (itemOffhand.getItem() == ModuleTechBasic.Items.MARSHMALLOW) {

          // If the player is holding marshmallows in their offhand,
          // remove the stick's marshmallow and add it to the offhand stack.
          if (type == EnumType.MARSHMALLOW
              && itemOffhand.getCount() < itemOffhand.getMaxStackSize()) {
            player.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(ModuleTechBasic.Items.MARSHMALLOW, itemOffhand.getCount() + 1));
            ItemStack newItemStack = new ItemStack(ModuleTechBasic.Items.MARSHMALLOW_STICK_EMPTY, 1, itemOffhand.getItemDamage());
            this.setCooldownOnMarshmallows(player);
            return new ActionResult<>(EnumActionResult.SUCCESS, newItemStack);
          }

        } else if (itemOffhand.isEmpty()) {

          boolean shouldDamage = false;

          // Remove any marshmallow from the stick.
          switch (type) {
            case MARSHMALLOW_BURNED:
              player.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(ModuleTechBasic.Items.MARSHMALLOW_BURNED));
              shouldDamage = true;
              break;

            case MARSHMALLOW_ROASTED:
              ItemStack stack = new ItemStack(ModuleTechBasic.Items.MARSHMALLOW_ROASTED);
              ItemMarshmallow.setRoastedAtTimestamp(stack, ItemMarshmallowStick.getRoastedAtTimestamp(itemMainHand));
              player.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, stack);
              shouldDamage = true;
              break;

            case MARSHMALLOW:
              player.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, new ItemStack(ModuleTechBasic.Items.MARSHMALLOW));
          }

          ItemStack newItemStack = new ItemStack(ModuleTechBasic.Items.MARSHMALLOW_STICK_EMPTY, 1, itemMainHand.getItemDamage());

          if (shouldDamage) {
            newItemStack.damageItem(1, player);
          }

          this.setCooldownOnMarshmallows(player);
          return new ActionResult<>(EnumActionResult.SUCCESS, newItemStack);
        }
      }
    }

    // This call will handle eating with the offhand.
    return super.onItemRightClick(world, player, hand);
  }

  @Override
  public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {

    if (!(player instanceof EntityPlayer)) {
      return;
    }

    long roastByTimestamp = ItemMarshmallowStick.getRoastByTimestamp(stack);
    World world = player.world;

    // Only want to check for campfire if we're roasting as indicated by the
    // roast timestamp. We check that the player is still looking at the
    // campfire and is still within range.
    if (roastByTimestamp < Long.MAX_VALUE) {
      RayTraceResult rayTraceResult = this.rayTrace(world, (EntityPlayer) player, false);

      // The ray trace result can be null
      //noinspection ConstantConditions
      if (rayTraceResult == null
          || rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK
          || !this.isRoastingBlock(world, rayTraceResult.getBlockPos())
          || !this.isWithinRoastingRange(player, rayTraceResult.getBlockPos())) {
        player.stopActiveHand();
        this.setCooldownOnMarshmallows((EntityPlayer) player);
      }
    }

    // Turn into a roasted or burned stick when it passes the threshold.
    if (world.isRemote) {
      long totalWorldTime = world.getTotalWorldTime();

      if (roastByTimestamp < Long.MAX_VALUE) {
        EnumType type = ItemMarshmallowStick.getType(stack);

        if (type == EnumType.MARSHMALLOW_ROASTED
            && totalWorldTime >= roastByTimestamp + ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.ROASTING_BURN_DURATION_TICKS) {
          ItemMarshmallowStick.setType(EnumType.MARSHMALLOW_BURNED, stack);

        } else if (type == EnumType.MARSHMALLOW && totalWorldTime >= roastByTimestamp) {
          ItemMarshmallowStick.setType(EnumType.MARSHMALLOW_ROASTED, stack);
        }
      }
    }
  }

  private boolean isWithinRoastingRange(EntityLivingBase player, BlockPos blockPos) {

    return player.getDistanceSqToCenter(blockPos) <= ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.ROASTING_RANGE_BLOCKS * ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.ROASTING_RANGE_BLOCKS;
  }

  @Override
  public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase player, int timeLeft) {

    if (!(player instanceof EntityPlayer)) {
      return;
    }

    // Mallow is roasted or burned
    if (ItemMarshmallowStick.getRoastedAtTimestamp(stack) > 0) {
      return;
    }

    long totalWorldTime = world.getTotalWorldTime();
    long roastTimestamp = ItemMarshmallowStick.getRoastByTimestamp(stack);

    if (roastTimestamp < Long.MAX_VALUE) {

      if (totalWorldTime >= roastTimestamp + ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.ROASTING_BURN_DURATION_TICKS) {
        ItemMarshmallowStick.setType(EnumType.MARSHMALLOW_BURNED, stack);

        if (!world.isRemote) {
          ItemMarshmallowStick.setRoastedAtTimestamp(stack, world.getTotalWorldTime());

          if (ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.ENABLE_BURNED_MARSHMALLOW_BROADCAST_MESSAGE) {
            MinecraftServer minecraftServer = world.getMinecraftServer();

            if (minecraftServer != null) {
              PlayerList playerList = minecraftServer.getPlayerList();

              for (EntityPlayerMP playerMP : playerList.getPlayers()) {
                playerMP.sendMessage(new TextComponentTranslation("gui.pyrotech.marshmallow.burned.broadcast.message", player.getName()));
              }
            }
          }
        }

      } else if (totalWorldTime >= roastTimestamp) {
        ItemMarshmallowStick.setType(EnumType.MARSHMALLOW_ROASTED, stack);

        if (!world.isRemote) {
          ItemMarshmallowStick.setRoastedAtTimestamp(stack, world.getTotalWorldTime());
        }
      }

      ItemMarshmallowStick.setRoastByTimestamp(stack, Long.MAX_VALUE);
    }
  }

  @Nonnull
  @Override
  public ItemStack onItemUseFinish(@Nonnull ItemStack stack, @Nonnull World world, EntityLivingBase player) {

    if (!(player instanceof EntityPlayer)) {
      return stack;
    }

    // Handle eating.
    if (this.canEat(stack)) {

      ItemStack newItemStack = new ItemStack(ModuleTechBasic.Items.MARSHMALLOW_STICK_EMPTY, 1, stack.getItemDamage());
      EnumType type = ItemMarshmallowStick.getType(stack);

      // Allow eating a marshmallow from the stick without damaging it.
      if (type != EnumType.MARSHMALLOW) {
        newItemStack.damageItem(1, player);
      }

      // Apply the appropriate marshmallow effects.
      switch (type) {
        case MARSHMALLOW:
          ItemMarshmallow.applyMarshmallowEffects(
              ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.MARSHMALLOW_SPEED_DURATION_TICKS,
              ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.MAX_MARSHMALLOW_SPEED_DURATION_TICKS,
              player,
              MobEffects.SPEED,
              true
          );
          break;

        case MARSHMALLOW_ROASTED:
          double potency = ItemMarshmallow.calculatePotency(world, ItemMarshmallowStick.getRoastedAtTimestamp(stack));
          ItemMarshmallow.applyMarshmallowEffects(
              (int) (ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.ROASTED_MARSHMALLOW_SPEED_DURATION_TICKS * potency),
              ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.MAX_ROASTED_MARSHMALLOW_SPEED_DURATION_TICKS,
              player,
              MobEffects.SPEED,
              true
          );
          break;

        case MARSHMALLOW_BURNED:

          if (!world.isRemote && ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.ENABLE_BURNED_MARSHMALLOW_EAT_BROADCAST_MESSAGE) {
            MinecraftServer minecraftServer = world.getMinecraftServer();

            if (minecraftServer != null) {
              PlayerList playerList = minecraftServer.getPlayerList();

              for (EntityPlayerMP playerMP : playerList.getPlayers()) {
                playerMP.sendMessage(new TextComponentTranslation("gui.pyrotech.marshmallow.burned.eat.broadcast.message", player.getName()));
              }
            }
          }

          ItemMarshmallow.applyMarshmallowEffects(
              ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.BURNED_MARSHMALLOW_SLOW_DURATION_TICKS,
              ModuleTechBasicConfig.CAMPFIRE_MARSHMALLOWS.BURNED_MARSHMALLOW_SLOW_DURATION_TICKS,
              player,
              MobEffects.SLOWNESS,
              false
          );
          break;
      }

      // Call super for the eating logic.
      super.onItemUseFinish(stack, world, player);

      // Set the cooldown.
      this.setCooldownOnMarshmallows((EntityPlayer) player);

      return newItemStack;
    }

    ItemMarshmallowStick.setRoastByTimestamp(stack, Long.MAX_VALUE);
    ItemMarshmallowStick.setType(EnumType.MARSHMALLOW_BURNED, stack);

    if (!world.isRemote) {
      ItemMarshmallowStick.setRoastedAtTimestamp(stack, world.getTotalWorldTime());
    }

    return stack;
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag tooltipFlag) {

    super.addInformation(stack, world, tooltip, tooltipFlag);

    if (world != null && ItemMarshmallowStick.getType(stack) == EnumType.MARSHMALLOW_ROASTED) {
      long roastedAtTimestamp = ItemMarshmallowStick.getRoastedAtTimestamp(stack);
      double potency = ItemMarshmallow.calculatePotency(world, roastedAtTimestamp);
      int displayPotency = (int) Math.round(potency * 100);
      tooltip.add(TextFormatting.GRAY + I18n.format("gui.pyrotech.tooltip.potency", String.valueOf(displayPotency)));
    }
  }

  /**
   * Return true if the stack has been roasted, burned, or is a plain marshmallow.
   */
  private boolean canEat(ItemStack stack) {

    return ItemMarshmallowStick.getRoastedAtTimestamp(stack) > 0 // is roasted or burned
        || ItemMarshmallowStick.getType(stack) == EnumType.MARSHMALLOW; // is plain mallow
  }

  public boolean isRoastingBlock(World world, BlockPos blockPos) {

    return world.getTileEntity(blockPos) instanceof TileCampfire;
  }

  private void setCooldownOnMarshmallows(EntityPlayer player) {

    player.getCooldownTracker().setCooldown(this, 10);
    player.getCooldownTracker().setCooldown(ModuleTechBasic.Items.MARSHMALLOW_STICK_EMPTY, 10);
    player.getCooldownTracker().setCooldown(ModuleTechBasic.Items.MARSHMALLOW, 10);
    player.getCooldownTracker().setCooldown(ModuleTechBasic.Items.MARSHMALLOW_ROASTED, 10);
    player.getCooldownTracker().setCooldown(ModuleTechBasic.Items.MARSHMALLOW_BURNED, 10);
  }

  public enum EnumType {
    MARSHMALLOW(0), MARSHMALLOW_ROASTED(1), MARSHMALLOW_BURNED(2);

    private static final Int2ObjectMap<EnumType> TYPES = new Int2ObjectOpenHashMap<>(EnumType.values().length);

    static {
      Arrays.stream(EnumType.values()).forEach(enumType -> TYPES.put(enumType.id, enumType));
    }

    public static EnumType from(int id) {

      return TYPES.get(id);
    }

    private final int id;

    EnumType(int id) {

      this.id = id;
    }
  }

}
