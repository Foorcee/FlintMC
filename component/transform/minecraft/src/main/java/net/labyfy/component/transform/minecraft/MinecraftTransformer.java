package net.labyfy.component.transform.minecraft;

import net.flintmc.processing.autoload.DetectableAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link net.labyfy.component.transform.launchplugin.LateInjectedTransformer} to be registered
 * and detected automatically by a {@link net.labyfy.component.stereotype.service.ServiceHandler}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@DetectableAnnotation
public @interface MinecraftTransformer {

  /**
   * Defines the priority order in which the {@link net.labyfy.component.transform.launchplugin.LateInjectedTransformer}
   * will handle class transformations.
   * <p>
   * Lowest priority will be called first.
   *
   * @return the priority for class transformations
   */
  int priority() default 0;
}
