package net.flintmc.util.session.internal.launcher;

import java.util.UUID;
import net.flintmc.framework.inject.assisted.Assisted;
import net.flintmc.framework.inject.assisted.AssistedInject;
import net.flintmc.framework.inject.implement.Implement;
import net.flintmc.mcapi.player.gameprofile.GameProfile;
import net.flintmc.util.session.launcher.LauncherProfile;

@Implement(LauncherProfile.class)
public class DefaultLauncherProfile implements LauncherProfile {

  private final String profileId;
  private final GameProfile[] profiles;

  private String accessToken;

  @AssistedInject
  private DefaultLauncherProfile(
      @Assisted("profileId") String profileId,
      @Assisted("accessToken") String accessToken,
      @Assisted("profiles") GameProfile[] profiles) {
    this.profileId = profileId;
    this.accessToken = accessToken;
    this.profiles = profiles;
  }

  @Override
  public GameProfile getProfile(UUID uniqueId) {
    for (GameProfile gameProfile : this.profiles) {
      if (gameProfile.getUniqueId().equals(uniqueId)) {
        return gameProfile;
      }
    }
    return null;
  }

  @Override
  public String getProfileId() {
    return this.profileId;
  }

  @Override
  public GameProfile[] getProfiles() {
    return this.profiles;
  }

  @Override
  public String getAccessToken() {
    return this.accessToken;
  }

  @Override
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }
}
