package net.flintmc.mcapi.internal.server.event;

import net.flintmc.framework.inject.assisted.Assisted;
import net.flintmc.framework.inject.assisted.AssistedInject;
import net.flintmc.framework.inject.implement.Implement;
import net.flintmc.mcapi.chat.component.ChatComponent;
import net.flintmc.mcapi.event.DirectionalEvent;
import net.flintmc.mcapi.server.ServerAddress;
import net.flintmc.mcapi.server.event.ServerKickEvent;

import javax.annotation.Nullable;

@Implement(ServerKickEvent.class)
public class DefaultServerKickEvent extends DefaultServerAddressEvent implements ServerKickEvent {

  private final ChatComponent reason;

  @AssistedInject
  public DefaultServerKickEvent(
          @Assisted("address") @Nullable ServerAddress address, @Assisted("reason") ChatComponent reason) {
    super(address);
    this.reason = reason;
  }

  @Override
  public DirectionalEvent.Direction getDirection() {
    return DirectionalEvent.Direction.RECEIVE;
  }

  @Override
  public ChatComponent getReason() {
    return this.reason;
  }
}
