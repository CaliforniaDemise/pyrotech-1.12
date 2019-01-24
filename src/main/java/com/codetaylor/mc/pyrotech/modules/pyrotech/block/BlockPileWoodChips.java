package com.codetaylor.mc.pyrotech.modules.pyrotech.block;

import com.codetaylor.mc.pyrotech.modules.pyrotech.block.spi.BlockPileBase;
import com.codetaylor.mc.pyrotech.modules.pyrotech.init.ModuleBlocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockPileWoodChips
    extends BlockPileBase {

  public static final String NAME = "pile_wood_chips";

  public BlockPileWoodChips() {

    super(Material.WOOD);
    this.setHardness(0.25f);
    this.setResistance(0);
    this.setHarvestLevel("shovel", 0);
    this.setSoundType(SoundType.GROUND);
  }

  @Override
  protected ItemStack getDrop(World world, BlockPos pos, IBlockState state) {

    return new ItemStack(ModuleBlocks.ROCK, 1, BlockRock.EnumType.WOOD_CHIPS.getMeta());
  }
}
