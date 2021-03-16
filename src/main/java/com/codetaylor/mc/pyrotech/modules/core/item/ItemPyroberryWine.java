package com.codetaylor.mc.pyrotech.modules.core.item;

import com.codetaylor.mc.pyrotech.modules.core.ModuleCoreConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class ItemPyroberryWine
    extends ItemFood {

  public static final String NAME = "pyroberry_wine";

  public ItemPyroberryWine() {

    super(ModuleCoreConfig.FOOD.PYROBERRY_WINE_HUNGER, (float) ModuleCoreConfig.FOOD.PYROBERRY_WINE_SATURATION, false);
    this.setMaxStackSize(3);
    this.setAlwaysEdible();
  }

  @ParametersAreNonnullByDefault
  @Nonnull
  @Override
  public ItemStack onItemUseFinish(ItemStack itemStack, World world, EntityLivingBase entityLiving) {

    ItemStack resultItemStack = super.onItemUseFinish(itemStack, world, entityLiving);

    entityLiving.setFire(5);

    PotionEffect potionEffect = entityLiving.getActivePotionEffect(MobEffects.FIRE_RESISTANCE);

    int effectDuration = (30 * 20);

    if (potionEffect != null) {
      effectDuration += potionEffect.getDuration();
    }

    if (effectDuration > (60 * 20)) {
      int nauseaDuration = (effectDuration - (60 * 20)) / 2;
      entityLiving.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, nauseaDuration, 1));
    }

    entityLiving.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, effectDuration));

    if (resultItemStack.isEmpty()) {
      return new ItemStack(Items.GLASS_BOTTLE);
    }

    if (entityLiving instanceof EntityPlayer) {
      ((EntityPlayer) entityLiving).addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE));
    }

    return resultItemStack;
  }
}