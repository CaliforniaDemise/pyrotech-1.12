package com.codetaylor.mc.pyrotech.modules.bucket;

import com.codetaylor.mc.athenaeum.module.ModuleBase;
import com.codetaylor.mc.athenaeum.registry.Registry;
import com.codetaylor.mc.pyrotech.ModPyrotech;
import com.codetaylor.mc.pyrotech.modules.bucket.init.ItemInitializer;
import com.codetaylor.mc.pyrotech.modules.bucket.init.VanillaFurnaceRecipesAdd;
import com.codetaylor.mc.pyrotech.modules.bucket.item.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModuleBucket
    extends ModuleBase {

  public static final String MODULE_ID = "module.bucket";
  public static final String MOD_ID = ModPyrotech.MOD_ID;
  public static final CreativeTabs CREATIVE_TAB = ModPyrotech.CREATIVE_TAB;

  public ModuleBucket() {

    super(0, MOD_ID);

    this.setRegistry(new Registry(MOD_ID, CREATIVE_TAB));
    this.enableAutoRegistry();
  }

  @Override
  public void onRegister(Registry registry) {

    ItemInitializer.onRegister(registry);
  }

  @SideOnly(Side.CLIENT)
  @Override
  public void onClientRegister(Registry registry) {

    ItemInitializer.onClientRegister(registry);
  }

  @Override
  public void onRegisterRecipesEvent(RegistryEvent.Register<IRecipe> event) {

    super.onRegisterRecipesEvent(event);

    VanillaFurnaceRecipesAdd.apply();
  }

  @GameRegistry.ObjectHolder(ModuleBucket.MOD_ID)
  public static class Items {

    @GameRegistry.ObjectHolder(ItemBucketWood.NAME)
    public static final ItemBucketWood BUCKET_WOOD;

    @GameRegistry.ObjectHolder(ItemBucketClay.NAME)
    public static final ItemBucketClay BUCKET_CLAY;

    @GameRegistry.ObjectHolder(ItemBucketStone.NAME)
    public static final ItemBucketStone BUCKET_STONE;

    @GameRegistry.ObjectHolder(ItemBucketRefractory.NAME)
    public static final ItemBucketRefractory BUCKET_REFRACTORY;

    @GameRegistry.ObjectHolder(ItemBucketRefractoryUnfired.NAME)
    public static final ItemBucketRefractoryUnfired BUCKET_REFRACTORY_UNFIRED;

    @GameRegistry.ObjectHolder(ItemBucketClayUnfired.NAME)
    public static final ItemBucketClayUnfired BUCKET_CLAY_UNFIRED;

    static {
      BUCKET_WOOD = null;
      BUCKET_CLAY = null;
      BUCKET_STONE = null;
      BUCKET_REFRACTORY = null;
      BUCKET_REFRACTORY_UNFIRED = null;
      BUCKET_CLAY_UNFIRED = null;
    }
  }
}
