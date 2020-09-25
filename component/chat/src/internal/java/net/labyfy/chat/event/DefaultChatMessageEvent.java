package net.labyfy.chat.event;

public class DefaultChatMessageEvent implements ChatMessageEvent {

  private final Type type;

  public DefaultChatMessageEvent(Type type) {
    this.type = type;
  }

  @Override
  public Type getType() {
    return this.type;
  }
}
