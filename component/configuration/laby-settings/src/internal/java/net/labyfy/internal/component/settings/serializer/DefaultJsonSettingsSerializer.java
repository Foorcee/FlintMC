package net.labyfy.internal.component.settings.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.labyfy.chat.builder.ComponentBuilder;
import net.labyfy.chat.serializer.ComponentSerializer;
import net.labyfy.component.inject.implement.Implement;
import net.labyfy.component.inject.logging.InjectLogger;
import net.labyfy.component.settings.annotation.ApplicableSetting;
import net.labyfy.component.settings.annotation.TranslateKey;
import net.labyfy.component.settings.mapper.SettingHandler;
import net.labyfy.component.settings.registered.RegisteredCategory;
import net.labyfy.component.settings.registered.RegisteredSetting;
import net.labyfy.component.settings.registered.SettingsProvider;
import net.labyfy.component.settings.serializer.JsonSettingsSerializer;
import net.labyfy.component.settings.serializer.SettingsSerializationHandler;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.function.Function;

@Singleton
@Implement(JsonSettingsSerializer.class)
public class DefaultJsonSettingsSerializer implements JsonSettingsSerializer {

  private final Logger logger;
  private final SettingsProvider provider;
  private final SettingHandler<Annotation> handler;

  private final ComponentSerializer.Factory serializerFactory;
  private final ComponentBuilder.Factory builderFactory;

  private final Collection<RegisteredSettingsSerializer<?>> serializers;

  @Inject
  public DefaultJsonSettingsSerializer(@InjectLogger Logger logger, SettingsProvider provider,
                                       SettingHandler handler, ComponentSerializer.Factory serializerFactory,
                                       ComponentBuilder.Factory builderFactory) {
    this.logger = logger;
    this.provider = provider;
    this.handler = handler;
    this.serializerFactory = serializerFactory;
    this.builderFactory = builderFactory;

    this.serializers = new HashSet<>();
  }

  @Override
  public JsonArray serializeSettings() {
    JsonArray array = new JsonArray();

    for (RegisteredSetting setting : this.provider.getAllSettings()) {
      JsonElement element = this.serializeSetting(setting);
      if (element.isJsonArray()) {
        array.addAll(element.getAsJsonArray());
        continue;
      }

      array.add(element);
    }

    return array;
  }

  @Override
  public JsonElement serializeSetting(RegisteredSetting setting) {
    Object fullValue = setting.getCurrentValue();

    if (fullValue instanceof Map) {
      JsonArray array = new JsonArray();

      for (Map.Entry<?, ?> entry : ((Map<?, ?>) fullValue).entrySet()) {
        String key = this.formatKey(setting, entry.getKey());
        Object value = entry.getValue();

        JsonObject object = this.serializeSettingValue(setting, setting.getReference().getKey() + "#" + key, annotationType -> {
          if (!(value instanceof Enum<?>)) {
            return null;
          }

          try {
            // TODO caches?
            Field field = ((Enum<?>) value).getDeclaringClass().getDeclaredField(((Enum<?>) value).name());
            return field.getAnnotation(annotationType);
          } catch (NoSuchFieldException e) {
            this.logger.error("Failed to find enum constant field in "
                + ((Enum<?>) value).getDeclaringClass().getName() + ": " + ((Enum<?>) value).name(), e);
            return null;
          }

        }, value);

        if (setting.getReference().findLastAnnotation(TranslateKey.class) != null) {
          object.add("displayName", this.serializerFactory.gson().getGson().toJsonTree(
              this.builderFactory.translation().translationKey(key).build()
          ));
        }

        array.add(object);
      }

      return array;
    } else {
      return this.serializeSettingValue(setting, setting.getReference().getKey(), null, fullValue);
    }
  }

  private String formatKey(RegisteredSetting setting, Object raw) {
    if (raw instanceof Enum<?>) {
      return ((Enum<?>) raw).name();
    }

    if (raw instanceof String || raw instanceof Number || raw instanceof Boolean || raw instanceof Character) {
      return String.valueOf(raw);
    }

    throw new UnsupportedOperationException("Unsupported key type " + raw.getClass().getName() + " in setting " + setting.getReference().getKey());
  }

  private JsonObject serializeSettingValue(RegisteredSetting setting, String key,
                                           Function<Class<? extends Annotation>, Annotation> annotationResolver,
                                           Object value) {
    JsonObject object = this.handler.serialize(setting.getAnnotation(), setting, value);

    ApplicableSetting applicableSetting = setting.getAnnotation().annotationType().getAnnotation(ApplicableSetting.class);
    object.addProperty("type", applicableSetting.type());

    object.addProperty("name", key);
    object.addProperty("enabled", setting.isEnabled());

    for (RegisteredSettingsSerializer serializer : this.serializers) {
      Annotation annotation = setting.getReference().findLastAnnotation(serializer.getAnnotationType());
      if (annotationResolver != null) {
        Annotation resolved = annotationResolver.apply(serializer.getAnnotationType());
        if (resolved != null) {
          annotation = resolved;
        }
      }
      serializer.getHandler().append(object, setting, annotation);
    }

    object.addProperty("category", setting.getCategoryName());

    if (setting.isNative()) {
      object.addProperty("native", true);
    }

    return object;
  }

  @Override
  public JsonObject serializeCategories() {
    JsonObject result = new JsonObject();

    for (RegisteredCategory category : this.provider.getCategories()) {
      result.add(category.getRegistryName(), this.serializeCategory(category));
    }

    return result;
  }

  @Override
  public JsonObject serializeCategory(RegisteredCategory category) {
    JsonObject object = new JsonObject();

    Gson gson = this.serializerFactory.gson().getGson();
    object.add("displayName", gson.toJsonTree(category.getDisplayName()));
    object.add("description", gson.toJsonTree(category.getDescription()));

    return object;
  }

  @Override
  public <A extends Annotation> void registerHandler(Class<A> annotationType, SettingsSerializationHandler<A> handler) {
    this.serializers.add(new RegisteredSettingsSerializer<>(annotationType, handler));
  }

  @Override
  public Collection<SettingsSerializationHandler<Annotation>> getHandlers() {
    Collection<SettingsSerializationHandler<Annotation>> handlers = new HashSet<>(this.serializers.size());

    for (RegisteredSettingsSerializer<?> serializer : this.serializers) {
      handlers.add((SettingsSerializationHandler<Annotation>) serializer.getHandler());
    }

    return handlers;
  }

  @Override
  public <A extends Annotation> Collection<SettingsSerializationHandler<A>> getHandlers(Class<A> annotationType) {
    Collection<SettingsSerializationHandler<A>> handlers = new HashSet<>(this.serializers.size());

    for (RegisteredSettingsSerializer<?> serializer : this.serializers) {
      if (serializer.getAnnotationType().equals(annotationType)) {
        handlers.add((SettingsSerializationHandler<A>) serializer.getHandler());
      }
    }

    return handlers;
  }

}
