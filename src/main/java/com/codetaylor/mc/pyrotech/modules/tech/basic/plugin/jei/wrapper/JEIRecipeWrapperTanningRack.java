package com.codetaylor.mc.pyrotech.modules.tech.basic.plugin.jei.wrapper;

import com.codetaylor.mc.pyrotech.library.spi.plugin.jei.JEIRecipeWrapperTimed;
import com.codetaylor.mc.pyrotech.modules.tech.basic.recipe.TanningRackRecipe;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JEIRecipeWrapperTanningRack
    extends JEIRecipeWrapperTimed {

  private final ResourceLocation registryName;
  private final List<List<ItemStack>> inputs;
  private final ItemStack output;

  public JEIRecipeWrapperTanningRack(TanningRackRecipe recipe) {

    super(recipe);

    this.registryName = recipe.getRegistryName();
    this.inputs = Collections.singletonList(Arrays.asList(recipe.getInputItem().getMatchingStacks()));
    this.output = recipe.getOutput();
  }

  @Override
  public void getIngredients(@Nonnull IIngredients ingredients) {

    ingredients.setInputLists(VanillaTypes.ITEM, this.inputs);
    ingredients.setOutput(VanillaTypes.ITEM, this.output);
  }

  @Override
  protected int getTimeDisplayY() {

    return super.getTimeDisplayY() - 14;
  }

  @Nullable
  @Override
  public ResourceLocation getRegistryName() {

    return this.registryName;
  }
}
