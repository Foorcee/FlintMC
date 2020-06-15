package net.labyfy.component.gui.v1_15_1;

import com.mojang.blaze3d.systems.RenderSystem;
import javassist.CannotCompileException;
import javassist.CtMethod;
import net.labyfy.base.structure.annotation.AutoLoad;
import net.labyfy.component.gui.GuiController;
import net.labyfy.component.gui.event.CursorPosChanged;
import net.labyfy.component.gui.event.MouseClicked;
import net.labyfy.component.gui.event.MouseScrolled;
import net.labyfy.component.gui.event.UnicodeTyped;
import net.labyfy.component.inject.InjectionHolder;
import net.labyfy.component.transform.javassist.ClassTransform;
import net.labyfy.component.transform.javassist.ClassTransformContext;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.glfw.*;
import org.lwjgl.system.Callback;
import org.lwjgl.system.CallbackI;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.BiFunction;

@AutoLoad
@Singleton
public class LabyInputInterceptor {
  private static double lastDrawTime = Double.MIN_VALUE;

  @Inject
  private LabyInputInterceptor() {}

  @ClassTransform(version = "1.15.1", value = "net.minecraft.client.util.InputMappings")
  public void transformInputMappings(ClassTransformContext context) throws CannotCompileException {
    CtMethod setKeyCallbacksMethod = context.getDeclaredMethod(
        "setKeyCallbacks", long.class, GLFWKeyCallbackI.class, GLFWCharModsCallbackI.class);
    setKeyCallbacksMethod.setBody(
        "net.labyfy.component.gui.v1_15_1.LabyInputInterceptor.interceptKeyboardCallbacks($$);");

    CtMethod setMouseCallbacksMethod = context.getDeclaredMethod(
        "setMouseCallbacks",
        long.class,
        GLFWCursorPosCallbackI.class,
        GLFWMouseButtonCallbackI.class,
        GLFWScrollCallbackI.class
    );
    setMouseCallbacksMethod.setBody(
        "net.labyfy.component.gui.v1_15_1.LabyInputInterceptor.interceptMouseCallbacks($$);");
  }

  public static void interceptKeyboardCallbacks(
      long windowHandle,
      GLFWKeyCallbackI keyCallback,
      GLFWCharModsCallbackI charModsCallback
  ) {
    GuiController guiController = InjectionHolder.getInjectedInstance(GuiController.class);

    overrideCallback(GLFW::glfwSetKeyCallback, windowHandle, keyCallback);
    overrideCallback(GLFW::glfwSetCharModsCallback, windowHandle, (window, codepoint, mods) -> {
      if(!guiController.doInput(new UnicodeTyped(codepoint))) {
        charModsCallback.invoke(window, codepoint, mods);
      }
    });
  }

  public static void interceptMouseCallbacks(
      long windowHandle,
      GLFWCursorPosCallbackI cursorPosCallback,
      GLFWMouseButtonCallbackI mouseButtonCallback,
      GLFWScrollCallbackI scrollCallback
  ) {
    GuiController guiController = InjectionHolder.getInjectedInstance(GuiController.class);

    overrideCallback(GLFW::glfwSetCursorPosCallback, windowHandle, (window, xpos, ypos) -> {
      if(!guiController.doInput(new CursorPosChanged(xpos, ypos))) {
        cursorPosCallback.invoke(window, xpos, ypos);
      }
    });

    overrideCallback(GLFW::glfwSetMouseButtonCallback, windowHandle, (window, button, action, mods) -> {
      if(!guiController.doInput(new MouseClicked(button))) {
        mouseButtonCallback.invoke(window, button, action, mods);
      }
    });

    overrideCallback(GLFW::glfwSetScrollCallback, windowHandle, (window, xoffset, yoffset) -> {
      if(!guiController.doInput(new MouseScrolled(xoffset, yoffset))) {
        scrollCallback.invoke(window, xoffset, yoffset);
      }
    });
  }

  private static <T extends Callback, C extends CallbackI> void overrideCallback(
      BiFunction<Long, C, T> setter, long windowHandle, C value) {
    T old = setter.apply(windowHandle, value);
    if(old != null) {
      old.free();
    }
  }

  @ClassTransform(version = "1.15.1", value = "com.mojang.blaze3d.systems.RenderSystem")
  public void transformRenderSystem(ClassTransformContext context) throws CannotCompileException {
    CtMethod flipFrameMethod = context.getDeclaredMethod("flipFrame", long.class);
    flipFrameMethod.setBody(
        "net.labyfy.component.gui.v1_15_1.LabyInputInterceptor.flipFrame($1);");

    CtMethod limitDisplayFPSMethod = context.getDeclaredMethod("limitDisplayFPS", int.class);
    limitDisplayFPSMethod.setBody(
        "net.labyfy.component.gui.v1_15_1.LabyInputInterceptor.limitDisplayFPS($1);");
  }

  public static void flipFrame(long windowHandle) {
    GuiController guiController = InjectionHolder.getInjectedInstance(GuiController.class);

    guiController.beginInput();
    GLFW.glfwPollEvents();
    guiController.endInput();

    RenderSystem.replayQueue();
    Tessellator.getInstance().getBuffer().reset();
    GLFW.glfwSwapBuffers(windowHandle);

    guiController.beginInput();
    GLFW.glfwPollEvents();
    guiController.endInput();
  }

  public static void limitDisplayFPS(int elapsedTicks) {
    GuiController guiController = InjectionHolder.getInjectedInstance(GuiController.class);
    double maxTime = lastDrawTime + 1.0d / (double) elapsedTicks;

    guiController.beginInput();
    for(double currentTime = GLFW.glfwGetTime(); currentTime < maxTime; currentTime = GLFW.glfwGetTime()) {
      GLFW.glfwWaitEventsTimeout(maxTime - currentTime);
    }
    guiController.endInput();
  }
}
