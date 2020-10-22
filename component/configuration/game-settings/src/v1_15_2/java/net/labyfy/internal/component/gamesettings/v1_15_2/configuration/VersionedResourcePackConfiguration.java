package net.labyfy.internal.component.gamesettings.v1_15_2.configuration;

import com.google.inject.Singleton;
import net.labyfy.component.gamesettings.configuration.ResourcePackConfiguration;
import net.labyfy.component.inject.implement.Implement;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * 1.15.2 implementation of {@link ResourcePackConfiguration}.
 */
@Singleton
@Implement(value = ResourcePackConfiguration.class, version = "1.15.2")
public class VersionedResourcePackConfiguration implements ResourcePackConfiguration {

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getResourcePacks() {
    return Minecraft.getInstance().gameSettings.resourcePacks;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setResourcePacks(List<String> resourcePacks) {
    Minecraft.getInstance().gameSettings.resourcePacks = resourcePacks;
    Minecraft.getInstance().gameSettings.saveOptions();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getIncompatibleResourcePacks() {
    return Minecraft.getInstance().gameSettings.incompatibleResourcePacks;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setIncompatibleResourcePacks(List<String> incompatibleResourcePacks) {
    Minecraft.getInstance().gameSettings.incompatibleResourcePacks = incompatibleResourcePacks;
    Minecraft.getInstance().gameSettings.saveOptions();
  }

}
