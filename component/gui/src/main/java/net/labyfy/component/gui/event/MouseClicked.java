package net.labyfy.component.gui.event;

public class MouseClicked implements GuiInputEvent {
  public static final int LEFT = 0;
  public static final int RIGHT = 1;
  public static final int MIDDLE = 2;

  private final int value;

  public MouseClicked(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }
}
