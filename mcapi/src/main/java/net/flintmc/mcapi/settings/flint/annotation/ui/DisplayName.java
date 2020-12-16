package net.flintmc.mcapi.settings.flint.annotation.ui;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.flintmc.framework.config.generator.method.ConfigObjectReference;
import net.flintmc.mcapi.chat.annotation.Component;
import net.flintmc.mcapi.chat.annotation.ComponentAnnotationSerializer;
import net.flintmc.mcapi.chat.serializer.GsonComponentSerializer;
import net.flintmc.mcapi.settings.flint.annotation.ApplicableSetting;
import net.flintmc.mcapi.settings.flint.serializer.SettingsSerializationHandler;

/**
 * Can be used with any {@link ApplicableSetting} to add a displayName to the setting. If it is not
 * set, the {@link ConfigObjectReference#getKey() key} will be used as a plain text.
 *
 * <p>The resulting json from the {@link SettingsSerializationHandler} will contain a 'displayName'
 * (if {@link #value()} contains at least one component) serialized with the {@link
 * GsonComponentSerializer} and {@link ComponentAnnotationSerializer}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.FIELD})
public @interface DisplayName {

  /**
   * Retrieves the description of the setting.
   *
   * @return The description of the setting
   */
  Component[] value();
}
