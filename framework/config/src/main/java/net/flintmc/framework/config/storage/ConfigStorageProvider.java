package net.flintmc.framework.config.storage;

import net.flintmc.framework.config.annotation.ExcludeStorage;
import net.flintmc.framework.config.annotation.IncludeStorage;
import net.flintmc.framework.config.event.ConfigStorageEvent;
import net.flintmc.framework.config.generator.ParsedConfig;

/** Provider for all {@link ConfigStorage}s. */
public interface ConfigStorageProvider extends ConfigStorage {

  /**
   * Registers the given storage in this provider. The given storage needs to have a {@link
   * StoragePriority} annotation. If the annotation is missing, nothing will happen.
   *
   * @param storage The non-null storage to be registered
   * @throws IllegalStateException If a storage with the name inside of this storage already exists
   */
  void registerStorage(ConfigStorage storage) throws IllegalStateException;

  /**
   * Queues the given config to be stored in every storage registered in this provider, this doesn't
   * store the config instantly so that the config won't be stored multiple times just a few
   * changes.
   *
   * <p>This also fires the {@link ConfigStorageEvent}.
   *
   * @param config The non-null config to be stored
   */
  @Override
  void write(ParsedConfig config);

  /**
   * Instantly fills the given config with information stored in the registered storages in this
   * provider. The storage with the highest priority (which is {@link Integer#MIN_VALUE}) will be
   * called last so that it overrides the other storages. Only values that aren't ignored with
   * {@link ExcludeStorage} and/or {@link IncludeStorage} for a storage can be ignored.
   *
   * <p>This also fires the {@link ConfigStorageEvent}.
   *
   * @param config The non-null config to be read
   */
  @Override
  void read(ParsedConfig config);
}
