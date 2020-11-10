package net.flintmc.mcapi.v1_15_2.entity;

import net.flintmc.framework.inject.assisted.Assisted;
import net.flintmc.framework.inject.assisted.AssistedInject;
import net.flintmc.framework.inject.implement.Implement;
import net.flintmc.mcapi.entity.CreatureEntity;
import net.flintmc.mcapi.entity.ai.EntitySenses;
import net.flintmc.mcapi.entity.mapper.EntityFoundationMapper;
import net.flintmc.mcapi.entity.type.EntityType;
import net.flintmc.mcapi.world.World;
import net.flintmc.mcapi.world.math.BlockPosition;
import net.minecraft.util.math.BlockPos;

/** 1.15.2 implementation of the {@link CreatureEntity}. */
@Implement(value = CreatureEntity.class, version = "1.15.2")
public class VersionedCreatureEntity extends VersionedMobEntity implements CreatureEntity {

  private final net.minecraft.entity.CreatureEntity creatureEntity;

  @AssistedInject
  public VersionedCreatureEntity(
      @Assisted("entity") Object entity,
      @Assisted("entityType") EntityType entityType,
      World world,
      EntityFoundationMapper entityFoundationMapper,
      EntitySenses.Factory entitySensesFactory) {
    super(entity, entityType, world, entityFoundationMapper, entitySensesFactory);

    if (!(entity instanceof net.minecraft.entity.CreatureEntity)) {
      throw new IllegalArgumentException(
          entity.getClass().getName()
              + " is not an instance of "
              + net.minecraft.entity.CreatureEntity.class.getName());
    }

    this.creatureEntity = (net.minecraft.entity.CreatureEntity) entity;
  }

  /** {@inheritDoc} */
  @Override
  public float getBlockPathWeight(BlockPosition position) {
    return this.creatureEntity.getBlockPathWeight(
        (BlockPos) this.getWorld().toMinecraftBlockPos(position));
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasPath() {
    return this.creatureEntity.hasPath();
  }
}
