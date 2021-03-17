package com.codetaylor.mc.pyrotech.modules.core.entity;

import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCoreConfig;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class EntityRockGrass
    extends EntityThrowable {

  public static final String NAME = "rock_grass";

  private static final DataParameter<Integer> META = EntityDataManager.createKey(EntityRockGrass.class, DataSerializers.VARINT);
  private int meta;

  // serialization
  @SuppressWarnings("unused")
  public EntityRockGrass(World world) {

    super(world);
  }

  public EntityRockGrass(World world, EntityLivingBase thrower, int meta) {

    super(world, thrower);
    this.dataManager.set(META, meta);
    this.meta = meta;
  }

  public int getMeta() {

    return this.meta;
  }

  @SideOnly(Side.CLIENT)
  public void handleStatusUpdate(byte id) {

    if (id == 3) {
      IBlockState blockState = ModuleCore.Blocks.ROCK_GRASS.getDefaultState();

      for (int i = 0; i < 8; ++i) {
        this.world.spawnParticle(EnumParticleTypes.BLOCK_CRACK, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D, Block.getStateId(blockState));
      }
    }
  }

  @Override
  protected void onImpact(@Nonnull RayTraceResult result) {

    if (result.entityHit != null) {
      float damage = (float) Math.max(0, ModuleCoreConfig.ROCKS.DAMAGE);
      result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), damage);
    }

    if (!this.world.isRemote) {
      this.world.setEntityState(this, (byte) 3);
      this.setDead();
    }
  }

  @Override
  protected void entityInit() {

    super.entityInit();
    this.dataManager.register(META, this.meta);
  }

  @Override
  public void notifyDataManagerChange(@Nonnull DataParameter<?> key) {

    if (this.world.isRemote) {

      if (META.equals(key)) {
        this.meta = this.dataManager.get(META);
      }
    }

    super.notifyDataManagerChange(key);
  }
}
