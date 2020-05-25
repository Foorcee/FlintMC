package net.labyfy.component.transform.minecraft.obfuscate.remap;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import net.labyfy.component.launcher.classloading.RootClassLoader;
import net.labyfy.component.launcher.classloading.common.ClassInformation;
import net.labyfy.component.launcher.classloading.common.CommonClassLoaderHelper;
import net.labyfy.component.mappings.ClassMapping;
import net.labyfy.component.mappings.ClassMappingProvider;
import net.labyfy.component.mappings.FieldMapping;
import net.labyfy.component.mappings.MethodMapping;
import net.labyfy.component.transform.asm.ASMUtils;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Singleton
public class MinecraftClassRemapper extends SimpleRemapper {

  private final ClassMappingProvider classMappingProvider;
  private final RootClassLoader rootClassLoader;

  @Inject
  private MinecraftClassRemapper(ClassMappingProvider classMappingProvider) {
    super(
        collectMappings(classMappingProvider));
    this.classMappingProvider = classMappingProvider;
    assert this.getClass().getClassLoader() instanceof RootClassLoader;
    this.rootClassLoader = (RootClassLoader) getClass().getClassLoader();
  }

  private static Map<String, String> collectMappings(ClassMappingProvider classMappingProvider) {
    Map<String, String> mappings = new HashMap<>();
    mappings.putAll(classMappingProvider.getUnObfuscatedClassMappings().values().stream()
        .filter(classMapping -> !classMapping.isDefault())
        .collect(
            Collectors.toMap(
                classMapping -> classMapping.getUnObfuscatedName().replace('.', '/'),
                classMapping -> classMapping.getObfuscatedName().replace('.', '/')))
    );


    List<MethodMapping> methodMappings = new ArrayList<>();

    for (ClassMapping classMapping : classMappingProvider.getUnObfuscatedClassMappings().values()) {
      for (MethodMapping method : classMapping.getMethods()) {
        if (mappings.containsKey(method.getClassMapping().getUnObfuscatedName().replace('.', '/') + "." + method.getUnObfuscatedMethodIdentifier())) {
          methodMappings.add(method);
        } else {
          mappings.put(method.getClassMapping().getUnObfuscatedName().replace('.', '/') + "." + method.getUnObfuscatedMethodIdentifier(), method.getObfuscatedMethodName());
        }
      }
    }


//    mappings.putAll(classMappingProvider.getUnObfuscatedClassMappings().values().stream()
//        .map(ClassMapping::getMethods)
//        .flatMap(Collection::stream)
//        .filter(methodMapping -> !methodMapping.isDefault())
////        .distinct()
//        .collect(
//            Collectors.toMap(
//                methodMapping -> methodMapping.getClassMapping().getUnObfuscatedName().replace('.', '/') + "." + methodMapping.getUnObfuscatedMethodIdentifier(),
//                MethodMapping::getObfuscatedMethodName))
//    );

    mappings.putAll(classMappingProvider.getUnObfuscatedClassMappings().values().stream()
        .map(ClassMapping::getFields)
        .flatMap(Collection::stream)
        .filter(fieldMapping -> !fieldMapping.isDefault())
        .collect(
            Collectors.toMap(
                fieldMapping -> fieldMapping.getClassMapping().getUnObfuscatedName().replace('.', '/') + "." + fieldMapping.getUnObfuscatedFieldName(),
                FieldMapping::getObfuscatedFieldName)
        ));
    return mappings;
  }

  public List<String> getSuperClass(String clazz) {
    try {
      ClassNode theClazz =
          ASMUtils.getNode(
              CommonClassLoaderHelper.retrieveClass(this.rootClassLoader, clazz).getClassBytes());

      ClassInformation superClassInformation =
          CommonClassLoaderHelper.retrieveClass(this.rootClassLoader, theClazz.superName);

      ClassNode superClass =
          superClassInformation == null
              ? null
              : ASMUtils.getNode(superClassInformation.getClassBytes());

      ArrayList<String> classes = new ArrayList<>();
      if (superClass != null) classes.add(superClass.name);
      if (theClazz.interfaces != null) {
        classes.addAll(theClazz.interfaces);
      }

      ArrayList<String> transitiveSuperClasses = new ArrayList<>();
      classes.forEach(c -> transitiveSuperClasses.addAll(getSuperClass(c)));
      classes.addAll(transitiveSuperClasses);
      return classes;
    } catch (Exception e) {
      // Not found, can be ignored
      return new ArrayList<>();
    }
  }

  public String map(String key) {
    return super.map(key);
  }

  public String mapMethodName(String owner, String name, String desc) {
    String map = this.map(owner + "." + name + desc.substring(0, desc.lastIndexOf(')') + 1));
    if (map == null) {
      List<String> possibleOwners = this.getSuperClass(owner.replace('/', '.'));
      if (possibleOwners != null) {
        for (String possibleOwner : possibleOwners) {
          map = this.map(possibleOwner + "." + name + desc.substring(0, desc.lastIndexOf(')') + 1));
          if (map != null) return map;
        }
      }
    }

    return map != null ? map : super.mapMethodName(owner, name, desc);
  }

  public String mapInvokeDynamicMethodName(String name, String desc) {
    return super.mapInvokeDynamicMethodName(name, desc);
  }

  public String mapFieldName(String owner, String name, String desc) {
    return mapMethodName(owner, name, desc);
  }

  public String mapMethodDesc(String desc) {
    return super.mapMethodDesc(desc);
  }
}
