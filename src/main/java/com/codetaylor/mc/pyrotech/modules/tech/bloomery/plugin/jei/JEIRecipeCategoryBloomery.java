package com.codetaylor.mc.pyrotech.modules.tech.bloomery.plugin.jei;

import com.codetaylor.mc.pyrotech.library.spi.plugin.jei.PyrotechRecipeCategory;
import com.codetaylor.mc.pyrotech.modules.tech.bloomery.ModuleTechBloomery;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.util.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class JEIRecipeCategoryBloomery
    extends PyrotechRecipeCategory<JEIRecipeWrapperBloomery> {

  public static final String UID_BLOOMERY = ModuleTechBloomery.MOD_ID + ".bloomery";
  public static final String UID_WITHER_FORGE = ModuleTechBloomery.MOD_ID + ".wither.forge";

  private final IDrawableAnimated animatedFlame;
  private final IDrawableAnimated arrow;
  private final IDrawable background;

  private final String title;
  private final String uid;

  public JEIRecipeCategoryBloomery(IGuiHelper guiHelper, String uid, String langKey) {

    this.uid = uid;

    ResourceLocation resourceLocation = new ResourceLocation(ModuleTechBloomery.MOD_ID, "textures/gui/jei7.png");

    IDrawableStatic arrowDrawable = guiHelper.createDrawable(resourceLocation, 82 + 19, 14, 24, 17);
    IDrawableStatic staticFlame = guiHelper.createDrawable(resourceLocation, 82 + 19, 0, 14, 14);

    IDrawableAnimated.StartDirection left = IDrawableAnimated.StartDirection.LEFT;
    IDrawableAnimated.StartDirection top = IDrawableAnimated.StartDirection.TOP;

    this.animatedFlame = guiHelper.createAnimatedDrawable(staticFlame, 300, top, true);
    this.arrow = guiHelper.createAnimatedDrawable(arrowDrawable, 200, left, false);
    this.background = guiHelper.createDrawable(resourceLocation, 0, 0, 82 + 19, 40);

    this.title = Translator.translateToLocal(langKey);
  }

  @Nonnull
  @Override
  public String getUid() {

    return this.uid;
  }

  @Nonnull
  @Override
  public String getTitle() {

    return this.title;
  }

  @Nonnull
  @Override
  public String getModName() {

    return ModuleTechBloomery.MOD_ID;
  }

  @Nonnull
  @Override
  public IDrawable getBackground() {

    return this.background;
  }

  @Override
  public void drawExtras(@Nonnull Minecraft minecraft) {

    this.animatedFlame.draw(minecraft, 1, 7);
    this.arrow.draw(minecraft, 24, 18);
  }

  @ParametersAreNonnullByDefault
  @Override
  public void setRecipe(IRecipeLayout recipeLayout, JEIRecipeWrapperBloomery recipeWrapper, IIngredients ingredients) {

    super.setRecipe(recipeLayout, recipeWrapper, ingredients);

    IGuiItemStackGroup itemStacks = recipeLayout.getItemStacks();
    itemStacks.init(0, true, 0, 22);
    itemStacks.init(1, false, 60, 18);
    itemStacks.init(2, false, 83, 22);

    itemStacks.set(ingredients);
  }

  @Override
  protected int getOutputSlotIndex() {

    return 1;
  }
}
