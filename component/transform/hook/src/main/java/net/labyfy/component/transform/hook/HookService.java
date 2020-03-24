package net.labyfy.component.transform.hook;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Key;
import com.google.inject.name.Names;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import net.labyfy.base.structure.identifier.Identifier;
import net.labyfy.base.structure.property.Property;
import net.labyfy.base.structure.representation.Types;
import net.labyfy.base.structure.service.Service;
import net.labyfy.base.structure.service.ServiceHandler;
import net.labyfy.component.inject.InjectionHolder;
import net.labyfy.component.inject.invoke.InjectedInvocationHelper;
import net.labyfy.component.transform.javassist.ClassTransform;
import net.labyfy.component.transform.javassist.ClassTransformContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

@Singleton
@Service(Hook.class)
public class HookService implements ServiceHandler {

  private final InjectedInvocationHelper injectedInvocationHelper;
  private final Collection<Identifier.Base> hooks;

  @Inject
  private HookService(InjectedInvocationHelper injectedInvocationHelper) {
    this.injectedInvocationHelper = injectedInvocationHelper;
    this.hooks = Sets.newConcurrentHashSet();
  }

  public void discover(Identifier.Base property) {
    this.hooks.add(property);
  }

  @ClassTransform
  public void transform(ClassTransformContext classTransformContext) {
    CtClass ctClass = classTransformContext.getCtClass();
    for (Identifier.Base identifier : hooks) {
      Hook hook = identifier.getProperty().getLocatedIdentifiedAnnotation().getAnnotation();
      if (!hook.className().isEmpty()) {
        String className =
            InjectionHolder.getInjectedInstance(hook.classNameResolver()).resolve(hook.className());
        if (className != null && className.equals(ctClass.getName())) {
          this.modify(
              hook,
              ctClass,
              identifier.getProperty().getLocatedIdentifiedAnnotation().getLocation());
        }
      } else {
        for (Property.Base subProperty :
            identifier.getProperty().getSubProperties(HookFilter.class)) {
          HookFilter hookFilter = subProperty.getLocatedIdentifiedAnnotation().getAnnotation();

          if (hookFilter
              .value()
              .test(
                  ctClass,
                  InjectionHolder.getInjectedInstance(hookFilter.type().typeNameResolver())
                      .resolve(hookFilter.type()))) {
            this.modify(
                hook,
                ctClass,
                identifier.getProperty().getLocatedIdentifiedAnnotation().getLocation());
          }
        }
      }
    }
  }

  private void modify(Hook hook, CtClass ctClass, Method callback) {
    try {
      CtClass[] parameters = new CtClass[hook.parameters().length];

      for (int i = 0; i < hook.parameters().length; i++) {
        parameters[i] =
            ClassPool.getDefault()
                .get(
                    InjectionHolder.getInjectedInstance(hook.parameterTypeNameResolver())
                        .resolve(hook.parameters()[i]));
      }

      CtMethod declaredMethod =
          ctClass.getDeclaredMethod(
              InjectionHolder.getInjectedInstance(hook.methodNameResolver()).resolve(hook),
              parameters);
      if (declaredMethod != null) {
        for (Hook.ExecutionTime executionTime : hook.executionTime()) {
          this.insert(declaredMethod, executionTime, callback);
        }
      }
    } catch (NotFoundException e) {
    }
  }

  private void insert(CtMethod target, Hook.ExecutionTime executionTime, Method hook) {
    StringBuilder stringBuilder = new StringBuilder();
    for (Class<?> parameterType : hook.getParameterTypes()) {
      if (stringBuilder.toString().isEmpty()) {
        stringBuilder.append(parameterType.getName()).append(".class");
      } else {
        stringBuilder.append(", ").append(parameterType.getName()).append(".class");
      }
    }

    executionTime.insert(
        target,
        "net.labyfy.component.transform.hook.HookService.notify("
            + "this,"
            + "net.labyfy.component.transform.hook.Hook.ExecutionTime."
            + executionTime
            + ","
            + hook.getDeclaringClass().getName()
            + ".class, \""
            + hook.getName()
            + "\", "
            + (stringBuilder.toString().isEmpty()
                ? "new Class[0]"
                : "new Class[]{" + stringBuilder.toString() + "}")
            + ");");
  }

  public static void notify(
      Object instance,
      Hook.ExecutionTime executionTime,
      Class<?> clazz,
      String method,
      Class<?>[] parameters) {
    try {

      Map<Key<?>, Object> availableParameters = Maps.newHashMap();
      availableParameters.put(Key.get(Hook.ExecutionTime.class), executionTime);
      availableParameters.put(Key.get(Object.class, Names.named("instance")), instance);
      availableParameters.put(Key.get(instance.getClass()), instance);

      Method declaredMethod = clazz.getDeclaredMethod(method, parameters);
      InjectionHolder.getInjectedInstance(InjectedInvocationHelper.class)
          .invokeMethod(
              declaredMethod,
              InjectionHolder.getInjectedInstance(declaredMethod.getDeclaringClass()),
              availableParameters);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      e.printStackTrace();
    }
  }
}
