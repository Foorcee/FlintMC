package net.labyfy.chat.serializer;

import com.google.inject.Singleton;
import net.labyfy.chat.serializer.gson.DefaultGsonComponentSerializer;
import org.apache.logging.log4j.Logger;

@Singleton
public class DefaultComponentSerializerFactory implements ComponentSerializer.Factory {

  private final ComponentSerializer legacy;
  private final ComponentSerializer plain;
  private final GsonComponentSerializer gson;

  public DefaultComponentSerializerFactory(Logger logger, boolean legacyHover) {
    this.legacy = new PlainComponentSerializer(logger, true); // plain serializer with all colors/formatting
    this.plain = new PlainComponentSerializer(logger, false); // plain serializer without any colors/formatting
    this.gson = new DefaultGsonComponentSerializer(logger, this, legacyHover); // in 1.16 the hoverEvent has completely changed
  }

  @Override
  public ComponentSerializer legacy() {
    return this.legacy;
  }

  @Override
  public ComponentSerializer plain() {
    return this.plain;
  }

  @Override
  public GsonComponentSerializer gson() {
    return this.gson;
  }
}
