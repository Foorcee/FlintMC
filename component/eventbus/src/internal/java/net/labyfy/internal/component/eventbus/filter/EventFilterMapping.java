package net.labyfy.internal.component.eventbus.filter;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class EventFilterMapping {

  private final Method annotationMethod;
  private final Method eventMethod;

  public EventFilterMapping(Method annotationMethod, Method eventMethod) {
    this.annotationMethod = annotationMethod;
    this.eventMethod = eventMethod;
  }

  public boolean matches(Object event, Annotation annotation) throws InvocationTargetException, IllegalAccessException {
    Object required = this.annotationMethod.invoke(annotation);
    Object provided = this.eventMethod.invoke(event);

    if (required instanceof Class && provided instanceof Class) {
      return ((Class<?>) required).isAssignableFrom((Class<?>) provided);
    }

    return Objects.equals(required, provided);
  }

}
