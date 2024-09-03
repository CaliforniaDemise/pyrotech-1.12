package com.codetaylor.mc.pyrotech.modules.worldgen.feature.spi;

import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import javax.annotation.Nonnull;
import java.util.Random;

public abstract class WorldGenOre extends WorldGenerator implements IWorldGenFeature {

    private final IBlockState oreBlock;
    private final IRandomIntSupplier countSupplier;
    private final Predicate<IBlockState> predicate;

    private final boolean allowed;
    private final int[] dimWhitelist;
    private final int[] dimBlacklist;

    private final int minY;
    private final int maxY;

    private final int spawnChance;

    public WorldGenOre(IBlockState state, int minY, int maxY, boolean enabled, int chanceToSpawn, int[] dimWhitelist, int[] dimBlacklist, IRandomIntSupplier countSupplier) {
        this(state, minY, maxY, enabled, chanceToSpawn, dimWhitelist, dimBlacklist, countSupplier, new StonePredicate());
    }

    public WorldGenOre(IBlockState state, int minY, int maxY, boolean enabled, int chanceToSpawn, int[] dimWhitelist, int[] dimBlacklist, IRandomIntSupplier countSupplier, Predicate<IBlockState> predicate) {
        this.oreBlock = state;
        this.countSupplier = countSupplier;
        this.predicate = predicate;
        this.minY = minY;
        this.maxY = maxY;
        this.spawnChance = chanceToSpawn;
        this.allowed = enabled && chanceToSpawn != 0;
        this.dimWhitelist = dimWhitelist;
        this.dimBlacklist = dimBlacklist;
    }

    @Override
    public boolean isAllowed(int dimensionId) {
        return this.allowed && this.isAllowedDimension(dimensionId, this.dimWhitelist, this.dimBlacklist);
    }

    @Override
    public boolean generateOre(@Nonnull World world, Random rand, BlockPos pos) {
        boolean a = true;
        for (int i = 0; i < this.spawnChance; i++) {
            BlockPos position = new BlockPos(pos.getX() + rand.nextInt(16), this.minY + rand.nextInt(Math.max(1, this.maxY - this.minY + 1)), pos.getZ() + rand.nextInt(16));
            a &= this.generate(world, rand, position);
        }
        return a;
    }

    @Override
    public boolean generate(@Nonnull World world, @Nonnull Random rand, @Nonnull BlockPos position) {
        int numberOfBlocks = this.countSupplier.get(rand);

        float f = rand.nextFloat() * (float) Math.PI;
        double d0 = (float) (position.getX() + 8) + MathHelper.sin(f) * (float) numberOfBlocks / 8.0F;
        double d1 = (float) (position.getX() + 8) - MathHelper.sin(f) * (float) numberOfBlocks / 8.0F;
        double d2 = (float) (position.getZ() + 8) + MathHelper.cos(f) * (float) numberOfBlocks / 8.0F;
        double d3 = (float) (position.getZ() + 8) - MathHelper.cos(f) * (float) numberOfBlocks / 8.0F;
        double d4 = position.getY() + rand.nextInt(3) - 2;
        double d5 = position.getY() + rand.nextInt(3) - 2;

        for (int i = 0; i < numberOfBlocks; ++i) {
            float f1 = (float) i / (float) numberOfBlocks;
            double d6 = d0 + (d1 - d0) * (double) f1;
            double d7 = d4 + (d5 - d4) * (double) f1;
            double d8 = d2 + (d3 - d2) * (double) f1;
            double d9 = rand.nextDouble() * (double) numberOfBlocks / 16.0D;
            double d10 = (double) (MathHelper.sin((float) Math.PI * f1) + 1.0F) * d9 + 1.0D;
            double d11 = (double) (MathHelper.sin((float) Math.PI * f1) + 1.0F) * d9 + 1.0D;
            int j = MathHelper.floor(d6 - d10 / 2.0D);
            int k = MathHelper.floor(d7 - d11 / 2.0D);
            int l = MathHelper.floor(d8 - d10 / 2.0D);
            int i1 = MathHelper.floor(d6 + d10 / 2.0D);
            int j1 = MathHelper.floor(d7 + d11 / 2.0D);
            int k1 = MathHelper.floor(d8 + d10 / 2.0D);

            for (int l1 = j; l1 <= i1; ++l1) {
                double d12 = ((double) l1 + 0.5D - d6) / (d10 / 2.0D);

                if (d12 * d12 < 1.0D) {

                    for (int i2 = k; i2 <= j1; ++i2) {
                        double d13 = ((double) i2 + 0.5D - d7) / (d11 / 2.0D);

                        if (d12 * d12 + d13 * d13 < 1.0D) {

                            for (int j2 = l; j2 <= k1; ++j2) {
                                double d14 = ((double) j2 + 0.5D - d8) / (d10 / 2.0D);

                                if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0D) {
                                    BlockPos blockpos = new BlockPos(l1, i2, j2);

                                    IBlockState state = world.getBlockState(blockpos);
                                    if (state.getBlock().isReplaceableOreGen(state, world, blockpos, this.predicate)) {
                                        world.setBlockState(blockpos, this.oreBlock, 2);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    static class StonePredicate
            implements Predicate<IBlockState> {

        private StonePredicate() {

        }

        public boolean apply(IBlockState blockState) {

            if (blockState != null && blockState.getBlock() == Blocks.STONE) {
                BlockStone.EnumType type = blockState.getValue(BlockStone.VARIANT);
                return type.isNatural();

            } else {
                return false;
            }
        }
    }

    public static class BlockPredicate
            implements Predicate<IBlockState> {

        private final Block toReplace;

        public BlockPredicate(Block toReplace) {

            this.toReplace = toReplace;
        }

        public boolean apply(IBlockState blockState) {

            return blockState != null
                    && blockState.getBlock() == this.toReplace;
        }
    }

    public static class BlockStatePredicate
            implements Predicate<IBlockState> {

        private final IBlockState toReplace;

        public BlockStatePredicate(IBlockState toReplace) {

            this.toReplace = toReplace;
        }

        public boolean apply(IBlockState blockState) {

            return (blockState == this.toReplace);
        }
    }
}