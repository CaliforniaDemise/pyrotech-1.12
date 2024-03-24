package com.codetaylor.mc.pyrotech.modules.tech.basic.init.recipe;

import com.codetaylor.mc.athenaeum.parser.recipe.item.MalformedRecipeItemException;
import com.codetaylor.mc.athenaeum.parser.recipe.item.ParseResult;
import com.codetaylor.mc.athenaeum.parser.recipe.item.RecipeItemParser;
import com.codetaylor.mc.pyrotech.modules.core.init.CompatInitializerWood;
import com.codetaylor.mc.pyrotech.modules.tech.basic.ModuleTechBasic;
import com.codetaylor.mc.pyrotech.modules.tech.basic.recipe.ChoppingBlockRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

public class ChoppingBlockRecipesAdd {

  public static final RecipeItemParser RECIPE_ITEM_PARSER = new RecipeItemParser();

  public static void applyCompatRecipes(Path configurationPath, IForgeRegistry<ChoppingBlockRecipe> registry) {

    CompatInitializerWood.WoodCompatData woodCompatData = CompatInitializerWood.read(configurationPath);

    if (woodCompatData == null) {
      return;
    }

    Iterator<Map.Entry<String, String>> iterator = woodCompatData.entries.entrySet().iterator();

    for (; iterator.hasNext(); ) {
      Map.Entry<String, String> modEntry = iterator.next();
      String inputString = modEntry.getKey();
      String outputString = modEntry.getValue();

      ChoppingBlockRecipesAdd.createRecipe(registry, inputString, outputString);
    }
  }

  protected static void createRecipe(IForgeRegistry<ChoppingBlockRecipe> registry, String recipeInput, String recipeOutput) {

    ItemStack inputItemStack = ChoppingBlockRecipesAdd.getItemStack(recipeInput);
    ItemStack outputItemStack = ChoppingBlockRecipesAdd.getItemStack(recipeOutput);

    if (inputItemStack.isEmpty()
        || outputItemStack.isEmpty()) {
      return;
    }

    // create a recipe

    Item inputItem = inputItemStack.getItem();
    ResourceLocation inputItemRegistryName = inputItem.getRegistryName();

    if (inputItemRegistryName == null) {
      ModuleTechBasic.LOGGER.error("Item missing registry name: " + inputItem);
      return;
    }

    Item outputItem = outputItemStack.getItem();
    ResourceLocation outputItemRegistryName = outputItem.getRegistryName();

    if (outputItemRegistryName == null) {
      ModuleTechBasic.LOGGER.error("Item missing registry name: " + outputItem);
      return;
    }

    String recipeName = outputItemRegistryName.getNamespace() + "_" + outputItemRegistryName.getPath() + "_" + outputItemStack.getMetadata()
        + "_from_" + inputItemRegistryName.getNamespace() + "_" + inputItemRegistryName.getPath() + "_" + inputItemStack.getMetadata();

    registry.register(new ChoppingBlockRecipe(
        outputItemStack,
        Ingredient.fromStacks(inputItemStack)
    ).setRegistryName(ModuleTechBasic.MOD_ID, recipeName));
  }

  protected static ItemStack getItemStack(String itemString) {

    try {
      ParseResult parse = RECIPE_ITEM_PARSER.parse(itemString);

      Item inputItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(parse.getDomain(), parse.getPath()));

      if (inputItem == null) {
        ModuleTechBasic.LOGGER.warn("Can't find registered item for: " + itemString);
        return ItemStack.EMPTY;
      }

      return new ItemStack(inputItem, 1, parse.getMeta());

    } catch (MalformedRecipeItemException e) {
      ModuleTechBasic.LOGGER.error("", e);
      return ItemStack.EMPTY;
    }
  }
}