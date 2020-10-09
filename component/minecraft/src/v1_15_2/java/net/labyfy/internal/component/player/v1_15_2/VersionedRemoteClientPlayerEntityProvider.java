package net.labyfy.internal.component.player.v1_15_2;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mojang.authlib.GameProfile;
import net.labyfy.component.entity.EntityMapper;
import net.labyfy.component.entity.type.EntityTypeRegister;
import net.labyfy.component.inject.implement.Implement;
import net.labyfy.component.player.RemoteClientPlayerEntity;
import net.labyfy.component.player.network.NetworkPlayerInfoRegistry;
import net.labyfy.component.player.serializer.gameprofile.GameProfileSerializer;
import net.labyfy.component.player.type.model.ModelMapper;
import net.labyfy.component.world.ClientWorld;

@Singleton
@Implement(value = RemoteClientPlayerEntity.Provider.class, version = "1.15.2")
public class VersionedRemoteClientPlayerEntityProvider implements RemoteClientPlayerEntity.Provider {

  private final RemoteClientPlayerEntity.Factory remoteClientPlayerEntity;
  private final EntityTypeRegister entityTypeRegister;
  private final ClientWorld clientWorld;
  private final EntityMapper entityMapper;
  private final GameProfileSerializer<GameProfile> gameProfileSerializer;
  private final ModelMapper modelMapper;
  private final NetworkPlayerInfoRegistry networkPlayerInfoRegistry;

  @Inject
  private VersionedRemoteClientPlayerEntityProvider(
          RemoteClientPlayerEntity.Factory remoteClientPlayerEntity,
          EntityTypeRegister entityTypeRegister,
          ClientWorld clientWorld,
          EntityMapper entityMapper,
          GameProfileSerializer gameProfileSerializer,
          ModelMapper modelMapper,
          NetworkPlayerInfoRegistry networkPlayerInfoRegistry
  ) {
    this.remoteClientPlayerEntity = remoteClientPlayerEntity;
    this.entityTypeRegister = entityTypeRegister;
    this.clientWorld = clientWorld;
    this.entityMapper = entityMapper;
    this.gameProfileSerializer = gameProfileSerializer;
    this.modelMapper = modelMapper;
    this.networkPlayerInfoRegistry = networkPlayerInfoRegistry;
  }

  @Override
  public RemoteClientPlayerEntity get(Object entity) {
    return this.remoteClientPlayerEntity.create(
            entity,
            this.entityTypeRegister.getEntityType("player"),
            this.clientWorld,
            this.entityMapper,
            this.gameProfileSerializer,
            this.modelMapper,
            this.networkPlayerInfoRegistry
    );
  }
}
