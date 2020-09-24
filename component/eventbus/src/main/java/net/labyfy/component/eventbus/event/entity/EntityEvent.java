package net.labyfy.component.eventbus.event.entity;

import com.google.inject.name.Named;
import net.labyfy.component.eventbus.event.filter.EventGroup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class EntityEvent {

  private final Object entity;

  public EntityEvent(Object entity) {
    this.entity = entity;
  }

  @Named("filter")
  public Object getEntity() {
    return this.entity;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  @EventGroup(groupEvent = EntityEvent.class)
  public @interface EntityFilter {

    @Named("filter")
    Class<?> filter();

  }

}
