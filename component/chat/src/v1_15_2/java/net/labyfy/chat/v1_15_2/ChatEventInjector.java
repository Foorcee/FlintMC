package net.labyfy.chat.v1_15_2;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.labyfy.chat.MinecraftComponentMapper;
import net.labyfy.chat.component.ChatComponent;
import net.labyfy.chat.event.ChatReceiveEvent;
import net.labyfy.chat.event.ChatSendEvent;
import net.labyfy.component.eventbus.EventBus;
import net.labyfy.component.processing.autoload.AutoLoad;
import net.labyfy.component.stereotype.type.Type;
import net.labyfy.component.transform.hook.Hook;
import net.minecraft.util.text.ITextComponent;

@Singleton
@AutoLoad
public class ChatEventInjector {

  private final EventBus eventBus;
  private final MinecraftComponentMapper componentMapper;
  private final ChatSendEvent.Factory sendFactory;
  private final ChatReceiveEvent.Factory receiveFactory;

  @Inject
  public ChatEventInjector(EventBus eventBus, MinecraftComponentMapper componentMapper,
                           ChatSendEvent.Factory sendFactory, ChatReceiveEvent.Factory receiveFactory) {
    this.eventBus = eventBus;
    this.componentMapper = componentMapper;
    this.sendFactory = sendFactory;
    this.receiveFactory = receiveFactory;
  }

  @Hook(
      className = "net.minecraft.client.gui.NewChatGui",
      methodName = "printChatMessageWithOptionalDeletion",
      executionTime = {Hook.ExecutionTime.BEFORE, Hook.ExecutionTime.AFTER},
      parameters = {@Type(reference = ITextComponent.class), @Type(reference = int.class)}
  )
  public void injectChatReceive(@Named("args") Object[] args, Hook.ExecutionTime executionTime) {
    ChatComponent component = this.componentMapper.fromMinecraft(args[0]);
    int deletedChatLineId = (int) args[1];

    boolean cancelled = this.eventBus.fireEvent(this.receiveFactory.create(component), executionTime).isCancelled();
    if (cancelled) {
      // TODO: return and modify the component (Hooks don't support this yet)
    }
  }

  @Hook(
      className = "net.minecraft.client.entity.player.ClientPlayerEntity",
      methodName = "sendChatMessage",
      executionTime = {Hook.ExecutionTime.BEFORE, Hook.ExecutionTime.AFTER},
      parameters = @Type(reference = String.class)
  )
  public void injectChatSend(@Named("args") Object[] args, Hook.ExecutionTime executionTime) {
    String message = (String) args[0];

    boolean cancelled = this.eventBus.fireEvent(this.sendFactory.create(message), executionTime).isCancelled();
    if (cancelled) {
      // TODO: return and modify the message that is being sent to the server (Hooks don't support this yet)
    }
  }

}
