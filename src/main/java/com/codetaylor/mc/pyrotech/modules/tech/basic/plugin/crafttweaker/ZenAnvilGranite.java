package com.codetaylor.mc.pyrotech.modules.tech.basic.plugin.crafttweaker;

import com.codetaylor.mc.athenaeum.tools.*;
import com.codetaylor.mc.athenaeum.util.RecipeHelper;
import com.codetaylor.mc.pyrotech.library.crafttweaker.RemoveAllRecipesAction;
import com.codetaylor.mc.pyrotech.modules.core.plugin.crafttweaker.ZenStages;
import com.codetaylor.mc.pyrotech.modules.tech.basic.ModuleTechBasic;
import com.codetaylor.mc.pyrotech.modules.tech.basic.ModuleTechBasicConfig;
import com.codetaylor.mc.pyrotech.modules.tech.basic.init.recipe.AnvilIroncladRecipesAdd;
import com.codetaylor.mc.pyrotech.modules.tech.basic.init.recipe.AnvilObsidianRecipesAdd;
import com.codetaylor.mc.pyrotech.modules.tech.basic.recipe.AnvilRecipe;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.minecraft.CraftTweakerMC;
import crafttweaker.mc1120.CraftTweaker;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenDocClass("mods.pyrotech.GraniteAnvil")
@ZenDocPrepend({"docs/include/header.md"})
@ZenDocAppend({"docs/include/anvil_granite.example.md"})
@ZenClass("mods.pyrotech.GraniteAnvil")
public class ZenAnvilGranite {

  @ZenDocMethod(
      order = 1,
      args = {
          @ZenDocArg(arg = "name", info = "unique recipe name"),
          @ZenDocArg(arg = "output", info = "recipe output"),
          @ZenDocArg(arg = "input", info = "recipe input"),
          @ZenDocArg(arg = "hits", info = "base number of hammer hits required"),
          @ZenDocArg(arg = "type", info = "hammer | pickaxe"),
          @ZenDocArg(arg = "inherited", info = "true if the recipe should be inherited")
      }
  )
  @ZenMethod
  public static void addRecipe(String name, IItemStack output, IIngredient input, int hits, String type, @Optional boolean inherited) {

    CraftTweaker.LATE_ACTIONS.add(new AddRecipe(
        name,
        CraftTweakerMC.getItemStack(output),
        CraftTweakerMC.getIngredient(input),
        hits,
        AnvilRecipe.EnumType.valueOf(type.toUpperCase()),
        inherited
    ));
  }

  @ZenDocMethod(
      order = 2,
      args = {
          @ZenDocArg(arg = "output", info = "recipe output to match")
      }
  )
  @ZenMethod
  public static void removeRecipes(IIngredient output) {

    CraftTweaker.LATE_ACTIONS.add(new RemoveRecipe(CraftTweakerMC.getIngredient(output)));
  }

  @ZenDocMethod(
      order = 3
  )
  @ZenMethod
  public static void removeAllRecipes() {

    CraftTweaker.LATE_ACTIONS.add(new RemoveAllRecipesAction<>(ModuleTechBasic.Registries.ANVIL_RECIPE, "anvil"));
  }

  @ZenDocMethod(
      order = 4,
      args = {
          @ZenDocArg(arg = "stages", info = "game stages")
      },
      description = {
          "Sets game stage logic required to use the device."
      }
  )
  @ZenMethod
  public static void setGameStages(ZenStages stages) {

    ModuleTechBasicConfig.STAGES_ANVIL_GRANITE = stages.getStages();
  }

  public static class RemoveRecipe
      implements IAction {

    private final Ingredient output;

    public RemoveRecipe(Ingredient output) {

      this.output = output;
    }

    @Override
    public void apply() {

      AnvilRecipe.removeRecipes(this.output);
    }

    @Override
    public String describe() {

      return "Removing granite anvil recipes for " + this.output;
    }
  }

  public static class AddRecipe
      implements IAction {

    private final ItemStack output;
    private final int hits;
    private final AnvilRecipe.EnumType type;
    private final boolean inherited;
    private final String name;
    private final Ingredient input;

    public AddRecipe(
        String name,
        ItemStack output,
        Ingredient input,
        int hits,
        AnvilRecipe.EnumType type,
        boolean inherited
    ) {

      this.name = name;
      this.input = input;
      this.output = output;
      this.hits = hits;
      this.type = type;
      this.inherited = inherited;
    }

    @Override
    public void apply() {

      AnvilRecipe recipe = new AnvilRecipe(
          this.output,
          this.input,
          this.hits,
          this.type,
          AnvilRecipe.EnumTier.GRANITE
      );

      ModuleTechBasic.Registries.ANVIL_RECIPE.register(recipe.setRegistryName(new ResourceLocation("crafttweaker", this.name)));

      if (this.inherited) {
        RecipeHelper.inherit("granite_anvil", ModuleTechBasic.Registries.ANVIL_RECIPE, AnvilIroncladRecipesAdd.INHERIT_TRANSFORMER, recipe);
        RecipeHelper.inherit("ironclad_anvil", ModuleTechBasic.Registries.ANVIL_RECIPE, AnvilObsidianRecipesAdd.INHERIT_TRANSFORMER, recipe);
      }
    }

    @Override
    public String describe() {

      return "Adding granite anvil recipe for " + this.output + ", inherited=" + this.inherited;
    }
  }

}
