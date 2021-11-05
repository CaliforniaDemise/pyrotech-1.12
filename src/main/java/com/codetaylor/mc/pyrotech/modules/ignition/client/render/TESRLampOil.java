package com.codetaylor.mc.pyrotech.modules.ignition.client.render;

import com.codetaylor.mc.pyrotech.modules.ignition.tile.TileLampOil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.CullFace;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

public class TESRLampOil
    extends TileEntitySpecialRenderer<TileLampOil> {

  private static final float PX = 0.0625f;
  private static final float INSET = (PX * 3f) + (PX * 0.1f);

  @Override
  public void render(@Nonnull TileLampOil tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuffer();
    this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
    RenderHelper.disableStandardItemLighting();
    GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    GlStateManager.enableBlend();
    GlStateManager.depthMask(true);

    if (Minecraft.isAmbientOcclusionEnabled()) {
      GlStateManager.shadeModel(GL11.GL_SMOOTH);

    } else {
      GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    for (int i = 0; i <= 1; i++) {
      GlStateManager.cullFace(i == 0 ? CullFace.FRONT : CullFace.BACK);
      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
      this.renderTileEntityFast(tile, x, y, z, partialTicks, destroyStage, partialTicks, buffer);
      buffer.setTranslation(0, 0, 0);
      tessellator.draw();
    }

    RenderHelper.enableStandardItemLighting();
  }

  @Override
  public void renderTileEntityFast(
      @Nonnull TileLampOil tile,
      double x,
      double y,
      double z,
      float partialTicks,
      int destroyStage,
      float partial,
      @Nonnull BufferBuilder buffer
  ) {

    FluidTank fluidTank = tile.getFluidTank();
    FluidStack fluidStack = fluidTank.getFluid();

    if (fluidStack != null) {

      World world = tile.getWorld();
      Fluid fluid = fluidStack.getFluid();
      TextureMap textureMapBlocks = Minecraft.getMinecraft().getTextureMapBlocks();
      TextureAtlasSprite still = textureMapBlocks.getAtlasSprite(fluid.getStill(fluidStack).toString());

      int color = fluid.getColor(fluidStack);
      float r = ((color >> 16) & 0xFF) / 255f;
      float g = ((color >> 8) & 0xFF) / 255f;
      float b = ((color >> 0) & 0xFF) / 255f;

      BlockPos blockpos = new BlockPos(tile.getPos());
      int i = world.isBlockLoaded(blockpos) ? world.getCombinedLight(blockpos, 0) : 0;
      int j = i >> 0x10 & 0xFFFF;
      int k = i & 0xFFFF;

      buffer.setTranslation(x, y, z);

      float topHeight = PX * 4;
      float percent = fluidTank.getFluidAmount() / (float) fluidTank.getCapacity();
      float level = (PX + PX * 0.1f) + (topHeight * percent) - (PX * 0.1f);

      // TOP
      buffer
          .pos(INSET, level, INSET)
          .color(r, g, b, 1f)
          .tex(still.getMinU(), still.getMinV())
          .lightmap(j, k)
          .endVertex();
      buffer
          .pos(INSET, level, 1 - INSET)
          .color(r, g, b, 1f)
          .tex(still.getMinU(), still.getMaxV())
          .lightmap(j, k)
          .endVertex();
      buffer
          .pos(1 - INSET, level, 1 - INSET)
          .color(r, g, b, 1f)
          .tex(still.getMaxU(), still.getMaxV())
          .lightmap(j, k)
          .endVertex();
      buffer
          .pos(1 - INSET, level, INSET)
          .color(r, g, b, 1f)
          .tex(still.getMaxU(), still.getMinV())
          .lightmap(j, k)
          .endVertex();

      float yMin = (PX + PX * 0.1f);
      float yMax = level;
      float interpolatedV = still.getInterpolatedV((1 - level) * 16);

      // SIDE X+
      buffer
          .pos(1 - INSET, yMin, INSET)
          .color(r, g, b, 1f)
          .tex(still.getMinU(), still.getMaxV())
          .lightmap(j, k)
          .endVertex();
      buffer
          .pos(1 - INSET, yMax, INSET)
          .color(r, g, b, 1f)
          .tex(still.getMinU(), interpolatedV)
          .lightmap(j, k)
          .endVertex();
      buffer
          .pos(1 - INSET, yMax, 1 - INSET)
          .color(r, g, b, 1f)
          .tex(still.getMaxU(), interpolatedV)
          .lightmap(j, k)
          .endVertex();
      buffer
          .pos(1 - INSET, yMin, 1 - INSET)
          .color(r, g, b, 1f)
          .tex(still.getMaxU(), still.getMaxV())
          .lightmap(j, k)
          .endVertex();

      // SIDE X-
      buffer
          .pos(INSET, yMin, INSET)
          .color(r, g, b, 1f)
          .tex(still.getMinU(), still.getMaxV())
          .lightmap(j, k)
          .endVertex();
      buffer
          .pos(INSET, yMin, 1 - INSET)
          .color(r, g, b, 1f)
          .tex(still.getMaxU(), still.getMaxV())
          .lightmap(j, k)
          .endVertex();
      buffer
          .pos(INSET, yMax, 1 - INSET)
          .color(r, g, b, 1f)
          .tex(still.getMaxU(), interpolatedV)
          .lightmap(j, k)
          .endVertex();
      buffer
          .pos(INSET, yMax, INSET)
          .color(r, g, b, 1f)
          .tex(still.getMinU(), interpolatedV)
          .lightmap(j, k)
          .endVertex();

      // SIDE Z-
      buffer
          .pos(INSET, yMin, 1 - INSET)
          .color(r, g, b, 1f)
          .tex(still.getMinU(), still.getMaxV())
          .lightmap(j, k)
          .endVertex();
      buffer
          .pos(1 - INSET, yMin, 1 - INSET)
          .color(r, g, b, 1f)
          .tex(still.getMaxU(), still.getMaxV())
          .lightmap(j, k)
          .endVertex();
      buffer
          .pos(1 - INSET, yMax, 1 - INSET)
          .color(r, g, b, 1f)
          .tex(still.getMaxU(), interpolatedV)
          .lightmap(j, k)
          .endVertex();
      buffer
          .pos(INSET, yMax, 1 - INSET)
          .color(r, g, b, 1f)
          .tex(still.getMinU(), interpolatedV)
          .lightmap(j, k)
          .endVertex();

      // SIDE Z+
      buffer
          .pos(INSET, yMin, INSET)
          .color(r, g, b, 1f)
          .tex(still.getMinU(), still.getMaxV())
          .lightmap(j, k)
          .endVertex();
      buffer
          .pos(INSET, yMax, INSET)
          .color(r, g, b, 1f)
          .tex(still.getMinU(), interpolatedV)
          .lightmap(j, k)
          .endVertex();
      buffer
          .pos(1 - INSET, yMax, INSET)
          .color(r, g, b, 1f)
          .tex(still.getMaxU(), interpolatedV)
          .lightmap(j, k)
          .endVertex();
      buffer
          .pos(1 - INSET, yMin, INSET)
          .color(r, g, b, 1f)
          .tex(still.getMaxU(), still.getMaxV())
          .lightmap(j, k)
          .endVertex();
    }

  }
}
