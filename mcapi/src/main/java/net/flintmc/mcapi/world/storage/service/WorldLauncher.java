package net.flintmc.mcapi.world.storage.service;

import net.flintmc.mcapi.world.storage.WorldConfiguration;
import net.flintmc.mcapi.world.storage.WorldOverview;

/**
 * Represents a launcher for the launch of worlds.
 */
public interface WorldLauncher {

  /**
   * Launch a world with the given {@code worldOverview}.
   *
   * @param worldOverview The overview of the world.
   */
  void launchWorld(WorldOverview worldOverview);

  /**
   * Launch a world with the given {@code worldOverview} and {@code configuration}
   *
   * @param worldOverview The overview of the world.
   * @param configuration The world configuration.
   */
  void launchWorld(WorldOverview worldOverview, WorldConfiguration configuration);

  /**
   * Launch a world with the given {@code fileName} and the {@code displayName}.
   *
   * @param fileName    The file name of the world.
   * @param displayName The display name of the world.
   */
  void launchWorld(String fileName, String displayName);

  /**
   * Launch a world with the given {@code fileName}, {@code displayName} and {@code configuration}.
   *
   * @param fileName      The file name of the world.
   * @param displayName   The display name of the world.
   * @param configuration The world configuration.
   */
  void launchWorld(String fileName, String displayName, WorldConfiguration configuration);

}
