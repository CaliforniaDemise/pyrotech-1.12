package com.codetaylor.mc.pyrotech.modules.tech.basic.tile;

import com.codetaylor.mc.athenaeum.integration.gamestages.Stages;
import com.codetaylor.mc.pyrotech.modules.tech.basic.ModuleTechBasic;
import com.codetaylor.mc.pyrotech.modules.tech.basic.ModuleTechBasicConfig;
import com.codetaylor.mc.pyrotech.modules.tech.basic.block.spi.BlockAnvilBase;
import com.codetaylor.mc.pyrotech.modules.tech.basic.recipe.AnvilRecipe;
import com.codetaylor.mc.pyrotech.modules.tech.basic.tile.spi.TileAnvilBase;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileAnvilIronPlated
    extends TileAnvilBase {

  @Override
  protected boolean allowAutomation() {

    return ModuleTechBasicConfig.IRONCLAD_ANVIL.ALLOW_AUTOMATION;
  }

  @Override
  public int getBloomAnvilExtraDamagePerHit() {

    return ModuleTechBasicConfig.IRONCLAD_ANVIL.BLOOM_EXTRA_DAMAGE_PER_HIT;
  }

  @Override
  public double getBloomAnvilExtraDamageChance() {

    return ModuleTechBasicConfig.IRONCLAD_ANVIL.BLOOM_EXTRA_DAMAGE_CHANCE;
  }

  @Override
  protected int getHitsPerDamage() {

    return ModuleTechBasicConfig.IRONCLAD_ANVIL.HITS_PER_DAMAGE;
  }

  @Override
  protected double getExhaustionCostPerCraftComplete() {

    return ModuleTechBasicConfig.IRONCLAD_ANVIL.EXHAUSTION_COST_PER_CRAFT_COMPLETE;
  }

  @Override
  protected double getExhaustionCostPerHit() {

    return ModuleTechBasicConfig.IRONCLAD_ANVIL.EXHAUSTION_COST_PER_HIT;
  }

  @Override
  protected int getMinimumHungerToUse() {

    return ModuleTechBasicConfig.IRONCLAD_ANVIL.MINIMUM_HUNGER_TO_USE;
  }

  @Override
  public boolean useDurability() {

    return ModuleTechBasicConfig.IRONCLAD_ANVIL.USE_DURABILITY;
  }

  @Nonnull
  @Override
  protected BlockAnvilBase getBlock() {

    return ModuleTechBasic.Blocks.ANVIL_IRON_PLATED;
  }

  @Override
  public AnvilRecipe.EnumTier getRecipeTier() {

    return AnvilRecipe.EnumTier.IRONCLAD;
  }

  @Nullable
  @Override
  public Stages getStages() {

    return ModuleTechBasicConfig.STAGES_ANVIL_IRONCLAD;
  }
}
