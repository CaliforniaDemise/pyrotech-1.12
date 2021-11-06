package com.codetaylor.mc.pyrotech.modules.tech.machine.init.recipe;

import com.codetaylor.mc.pyrotech.ModPyrotech;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.core.item.ItemMaterial;
import com.codetaylor.mc.pyrotech.modules.ignition.ModuleIgnition;
import com.codetaylor.mc.pyrotech.modules.tech.machine.ModuleTechMachine;
import com.codetaylor.mc.pyrotech.modules.tech.machine.recipe.StoneCrucibleRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.refractory.ModuleTechRefractory;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.IForgeRegistryModifiable;

public class StoneCrucibleRecipesAdd {

  public static void apply(IForgeRegistryModifiable<StoneCrucibleRecipe> registry) {

    // Water from Ice
    registry.register(new StoneCrucibleRecipe(
        new FluidStack(FluidRegistry.WATER, 1000),
        Ingredient.fromStacks(
            new ItemStack(Blocks.ICE, 1, 0)
        ),
        60 * 20
    ).setRegistryName(ModuleTechMachine.MOD_ID, "water_from_ice"));

    // Water from Snow
    registry.register(new StoneCrucibleRecipe(
        new FluidStack(FluidRegistry.WATER, 500),
        Ingredient.fromStacks(
            new ItemStack(Blocks.SNOW, 1, 0)
        ),
        15 * 20
    ).setRegistryName(ModuleTechMachine.MOD_ID, "water_from_snow"));

    // Water from Snowballs
    registry.register(new StoneCrucibleRecipe(
        new FluidStack(FluidRegistry.WATER, 125),
        Ingredient.fromStacks(
            new ItemStack(Items.SNOWBALL, 1, 0)
        ),
        15 * 20
    ).setRegistryName(ModuleTechMachine.MOD_ID, "water_from_snowballs"));

    // Water from Packed Ice
    registry.register(new StoneCrucibleRecipe(
        new FluidStack(FluidRegistry.WATER, 2000),
        Ingredient.fromStacks(
            new ItemStack(Blocks.PACKED_ICE, 1, 0)
        ),
        4 * 60 * 20
    ).setRegistryName(ModuleTechMachine.MOD_ID, "water_from_packed_ice"));

    // Liquid Clay from Clay Ball
    registry.register(new StoneCrucibleRecipe(
        new FluidStack(ModuleCore.Fluids.CLAY, 250),
        Ingredient.fromStacks(
            new ItemStack(Items.CLAY_BALL, 1, 0)
        ),
        60 * 20
    ).setRegistryName(ModuleTechMachine.MOD_ID, "liquid_clay_from_clay_ball"));

    // Liquid Clay from Clay Block
    registry.register(new StoneCrucibleRecipe(
        new FluidStack(ModuleCore.Fluids.CLAY, 1000),
        Ingredient.fromStacks(
            new ItemStack(Blocks.CLAY, 1, 0)
        ),
        3 * 60 * 20
    ).setRegistryName(ModuleTechMachine.MOD_ID, "liquid_clay_from_clay_block"));

    // Wood Tar from Wood Tar Block
    if (ModPyrotech.INSTANCE.isModuleEnabled(ModuleTechRefractory.class)) {
      registry.register(new StoneCrucibleRecipe(
          new FluidStack(ModuleTechRefractory.Fluids.WOOD_TAR, 1000),
          Ingredient.fromStacks(
              new ItemStack(ModuleCore.Blocks.WOOD_TAR_BLOCK, 1, 0)
          ),
          4 * 60 * 20
      ).setRegistryName(ModuleTechMachine.MOD_ID, "wood_tar_from_wood_tar_block"));
    }

    // Lamp Oil from Lard
    if (ModPyrotech.INSTANCE.isModuleEnabled(ModuleIgnition.class)) {
      registry.register(new StoneCrucibleRecipe(
          new FluidStack(ModuleIgnition.Fluids.LAMP_OIL, 125),
          Ingredient.fromStacks(ItemMaterial.EnumType.LARD.asStack()),
          10 * 60 * 20
      ).setRegistryName(ModuleTechMachine.MOD_ID, "lamp_oil_from_lard"));
    }
  }
}