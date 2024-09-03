package com.codetaylor.mc.pyrotech.modules.worldgen.feature.spi;

import com.codetaylor.mc.pyrotech.modules.worldgen.feature.*;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldGenerator {

    private final List<IWorldGenFeature> featureList;

    private final Int2ObjectMap<List<IWorldGenFeature>> perDimensionFeatureList;

    public WorldGenerator() {
        this.featureList = Lists.newArrayList(
                new WorldGenFossil(),
                new WorldGenLimestone(),
                new WorldGenDenseCoal(),
                new WorldGenDenseNetherCoal(),
                new WorldGenRocks(),
                new WorldGenDenseRedstoneOre(),
                new WorldGenDenseQuartzOre(),
                new WorldGenPyroberryBush(),
                new WorldGenGloamberryBush(),
                new WorldGenFreckleberryPlant(),
                new WorldGenMud()
        );

        this.perDimensionFeatureList = new Int2ObjectOpenHashMap<>();
    }

    public void generateDecoration(World world, Random random, ChunkPos pos, DecorateBiomeEvent.Decorate.EventType type) {
        int dimension = world.provider.getDimension();
        List<IWorldGenFeature> features = this.perDimensionFeatureList.get(dimension);
        for (IWorldGenFeature feature : features) {
            feature.generateDecoration(world, random, pos, type);
        }
    }

    public void generateOre(World world, Random random, BlockPos pos) {
        int dimension = world.provider.getDimension();
        List<IWorldGenFeature> features = this.perDimensionFeatureList.get(dimension);
        for (IWorldGenFeature feature : features) {
            feature.generateOre(world, random, pos);
        }
    }

    public boolean check(World world, Random random) {

        int dimension = world.provider.getDimension();
        List<IWorldGenFeature> features = this.perDimensionFeatureList.get(dimension);

        if (features == null) {
            features = new ArrayList<>();

            for (IWorldGenFeature feature : this.featureList) {

                if (feature.isAllowed(dimension)) {
                    features.add(feature);
                }
            }

            this.perDimensionFeatureList.put(dimension, features);
        }

        return true;
    }
}
