package com.codetaylor.mc.pyrotech.modules.pyrotech.recipe;

import com.codetaylor.mc.athenaeum.recipe.IRecipeSingleOutput;
import com.codetaylor.mc.athenaeum.util.RecipeHelper;
import com.codetaylor.mc.pyrotech.modules.pyrotech.ModulePyrotechConfig;
import com.codetaylor.mc.pyrotech.modules.pyrotech.ModulePyrotechRegistries;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

public class CompactingBinRecipe
    extends IForgeRegistryEntry.Impl<CompactingBinRecipe>
    implements IRecipeSingleOutput {

  @Nullable
  public static CompactingBinRecipe getRecipe(ItemStack input) {

    for (CompactingBinRecipe recipe : ModulePyrotechRegistries.COMPACTING_BIN_RECIPE) {

      if (recipe.matches(input)) {
        return recipe;
      }
    }

    return null;
  }

  public static boolean removeRecipes(Ingredient output) {

    return RecipeHelper.removeRecipesByOutput(ModulePyrotechRegistries.COMPACTING_BIN_RECIPE, output);
  }

  private final Ingredient input;
  private final ItemStack output;
  private final int amount;
  private final int[] requiredToolUses;

  public CompactingBinRecipe(ItemStack output, Ingredient input, int amount) {

    this(output, input, amount, ModulePyrotechConfig.COMPACTING_BIN.TOOL_USES_REQUIRED_PER_HARVEST_LEVEL);
  }

  public CompactingBinRecipe(
      ItemStack output,
      Ingredient input,
      int amount,
      int[] requiredToolUses
  ) {

    this.input = input;
    this.output = output;
    this.amount = amount;
    this.requiredToolUses = requiredToolUses;
  }

  public Ingredient getInput() {

    return this.input;
  }

  public ItemStack getOutput() {

    return this.output.copy();
  }

  public int getAmount() {

    return this.amount;
  }

  public int[] getRequiredToolUses() {

    return this.requiredToolUses;
  }

  public boolean matches(ItemStack input) {

    return this.input.apply(input);
  }
}