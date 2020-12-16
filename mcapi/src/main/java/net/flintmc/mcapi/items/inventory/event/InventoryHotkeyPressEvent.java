package net.flintmc.mcapi.items.inventory.event;

import com.google.inject.name.Named;
import net.flintmc.framework.eventbus.event.Cancellable;
import net.flintmc.framework.eventbus.event.Event;
import net.flintmc.framework.eventbus.event.subscribe.Subscribe;
import net.flintmc.framework.inject.assisted.Assisted;
import net.flintmc.framework.inject.assisted.AssistedFactory;
import net.flintmc.mcapi.items.ItemStack;
import net.flintmc.mcapi.items.inventory.Inventory;
import net.flintmc.mcapi.items.inventory.InventoryController;

/**
 * This event will be fired whenever the player clicks into the inventory. It will also be fired by
 * {@link InventoryController#performHotkeyPress(int, int)} and in both the PRE and POST phases, but
 * cancellation will only have an effect in the PRE phase.
 *
 * @see Subscribe
 */
public interface InventoryHotkeyPressEvent extends Event, InventorySlotEvent, Cancellable {

  /**
   * Retrieves the hotkey which has been pressed.
   *
   * @return The hotkey in the range from 0 - 8
   */
  @Named("hotkey")
  int getHotkey();

  /**
   * Retrieves the item which has been moved from or to the slot that is bound to the hotkey in this
   * event.
   *
   * @return The non-null item that has been moved by this event
   */
  ItemStack getClickedItem();

  /**
   * Factory for the {@link InventoryHotkeyPressEvent}.
   */
  @AssistedFactory(InventoryHotkeyPressEvent.class)
  interface Factory {

    /**
     * Creates a new {@link InventoryHotkeyPressEvent} with the given values.
     *
     * @param inventory The non-null inventory where the press has happened
     * @param slot      The slot in the inventory where the press has happened (if the user performs
     *                  this action, it will be the slot where the mouse is located at when pressing
     *                  the hotkey)
     * @param hotkey    The hotkey which has been pressed for this event in the range from 0 - 8
     * @return The new non-null event
     */
    InventoryHotkeyPressEvent create(
        @Assisted("inventory") Inventory inventory,
        @Assisted("slot") int slot,
        @Assisted("hotkey") int hotkey);
  }
}
