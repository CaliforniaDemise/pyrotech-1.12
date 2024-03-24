package com.codetaylor.mc.pyrotech.modules.tech.machine.init.recipe;

import com.codetaylor.mc.pyrotech.ModPyrotech;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.core.item.ItemMaterial;
import com.codetaylor.mc.pyrotech.modules.tech.basic.ModuleTechBasic;
import com.codetaylor.mc.pyrotech.modules.tech.basic.recipe.ChoppingBlockRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.machine.ModuleTechMachine;
import com.codetaylor.mc.pyrotech.modules.tech.machine.ModuleTechMachineConfig;
import com.codetaylor.mc.pyrotech.modules.tech.machine.recipe.StoneSawmillRecipe;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.registries.IForgeRegistryModifiable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StoneSawmillRecipesAdd {

  public static void apply(IForgeRegistryModifiable<StoneSawmillRecipe> registry) {

    // board
    registerSawmillRecipeWood(registry, "board",
        ItemMaterial.EnumType.BOARD.asStack(),
        new OreIngredient("slabWood")
    );

    // stick
    registerSawmillRecipeWood(registry, "stick",
        new ItemStack(Items.STICK),
        Ingredient.fromStacks(ItemMaterial.EnumType.BOARD.asStack())
    );

    // tarred board
    registerSawmillRecipeWood(registry, "board_tarred",
        ItemMaterial.EnumType.BOARD_TARRED.asStack(),
        Ingredient.fromStacks(new ItemStack(ModuleCore.Blocks.PLANKS_TARRED))
    );
  }

  public static void registerInheritedChoppingBlockRecipes(
      IForgeRegistryModifiable<ChoppingBlockRecipe> fromRegistry,
      IForgeRegistryModifiable<StoneSawmillRecipe> toRegistry
  ) {

    if (ModPyrotech.INSTANCE.isModuleEnabled(ModuleTechBasic.class)
        && ModuleTechMachineConfig.STONE_SAWMILL.INHERIT_CHOPPING_BLOCK_RECIPES) {

      Collection<ChoppingBlockRecipe> valuesCollection = fromRegistry.getValuesCollection();
      ArrayList<ChoppingBlockRecipe> snapshot = new ArrayList<>(valuesCollection);

      for (ChoppingBlockRecipe recipe : snapshot) {
        ResourceLocation registryName = recipe.getRegistryName();

        if (registryName == null) {
          continue;
        }

        StoneSawmillRecipesAdd.registerSawmillRecipeWood(
            toRegistry,
            "chopping_block/" + registryName.getPath(),
            recipe.getOutput(),
            recipe.getInput(),
            ModuleTechMachineConfig.STONE_SAWMILL.INHERITED_CHOPPING_BLOCK_RECIPE_DURATION_MODIFIER
        );
      }
    }
  }

  private static void registerSawmillRecipeWood(IForgeRegistryModifiable<StoneSawmillRecipe> registry, String name, ItemStack output, Ingredient input) {

    StoneSawmillRecipesAdd.registerSawmillRecipeWood(registry, name, output, input, 1);
  }

  public static List<StoneSawmillRecipe> registerSawmillRecipeWood(IForgeRegistryModifiable<StoneSawmillRecipe> registry, String name, ItemStack output, Ingredient input, double durationModifier) {

    List<StoneSawmillRecipe> result = new ArrayList<>();

    {
      output = output.copy();
      output.setCount(1);

      StoneSawmillRecipe recipe = new StoneSawmillRecipe(
          output,
          input,
          (int) (12 * 20 * durationModifier),
          Ingredient.fromStacks(new ItemStack(ModuleTechMachine.Items.STONE_MILL_BLADE, 1, OreDictionary.WILDCARD_VALUE)),
          4
      ).setRegistryName(ModuleTechMachine.MOD_ID, name + "_tier_0");
      registry.register(recipe);
      result.add(recipe);
    }

    {
      output = output.copy();
      output.setCount(2);

      StoneSawmillRecipe recipe = new StoneSawmillRecipe(
          output,
          input,
          (int) (8 * 20 * durationModifier),
          Ingredient.fromStacks(
              new ItemStack(ModuleTechMachine.Items.FLINT_MILL_BLADE, 1, OreDictionary.WILDCARD_VALUE),
              new ItemStack(ModuleTechMachine.Items.BONE_MILL_BLADE, 1, OreDictionary.WILDCARD_VALUE)
          ),
          2
      ).setRegistryName(ModuleTechMachine.MOD_ID, name + "_tier_1");
      registry.register(recipe);
      result.add(recipe);
    }

    {
      output = output.copy();
      output.setCount(2);

      StoneSawmillRecipe recipe = new StoneSawmillRecipe(
          output,
          input,
          (int) (6 * 20 * durationModifier),
          Ingredient.fromStacks(
              new ItemStack(ModuleTechMachine.Items.IRON_MILL_BLADE, 1, OreDictionary.WILDCARD_VALUE),
              new ItemStack(ModuleTechMachine.Items.OBSIDIAN_MILL_BLADE, 1, OreDictionary.WILDCARD_VALUE)
          ),
          1
      ).setRegistryName(ModuleTechMachine.MOD_ID, name + "_tier_2");
      registry.register(recipe);
      result.add(recipe);
    }

    {
      output = output.copy();
      output.setCount(3);

      StoneSawmillRecipe recipe = new StoneSawmillRecipe(
          output,
          input,
          (int) (8 * 20 * durationModifier),
          Ingredient.fromStacks(new ItemStack(ModuleTechMachine.Items.DIAMOND_MILL_BLADE, 1, OreDictionary.WILDCARD_VALUE)),
          1
      ).setRegistryName(ModuleTechMachine.MOD_ID, name + "_tier_3");
      registry.register(recipe);
      result.add(recipe);
    }

    return result;
  }
}