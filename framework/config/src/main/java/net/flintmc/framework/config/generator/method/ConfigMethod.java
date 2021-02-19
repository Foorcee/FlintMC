/*
 * FlintMC
 * Copyright (C) 2020-2021 LabyMedia GmbH and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package net.flintmc.framework.config.generator.method;

import java.lang.reflect.Type;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;
import net.flintmc.framework.config.generator.ConfigGenerator;
import net.flintmc.framework.config.generator.GeneratingConfig;
import net.flintmc.framework.config.generator.ParsedConfig;
import net.flintmc.framework.config.storage.ConfigStorageProvider;

/**
 * Represents a method which should be defined in a config that is currently being generated by a
 * {@link ConfigGenerator}.
 */
public interface ConfigMethod {

  /**
   * Retrieves an object containing information about this method like the class where it is
   * declared.
   *
   * @return The non-null info about this method
   */
  ConfigMethodInfo getInfo();

  /**
   * Retrieves the name of the getter to get the result which should be serialized in the config. To
   * construct this, {@link ConfigMethodInfo#getConfigName()} will be used.
   *
   * @return The non-null name of the getter
   */
  String getGetterName();

  /**
   * Retrieves the name of the setter to set the result which has been be deserialized from the
   * config. To construct this, {@link ConfigMethodInfo#getConfigName()} will be used.
   *
   * @return The non-null name of the setter
   */
  String getSetterName();

  /**
   * Retrieves an array of all methods that are associated with this method, this can be for example
   * 'getAllX', 'setAllX', 'getX', 'setX'. It may be used to get all annotations for a specific
   * entry.
   *
   * @return The non-null and non-empty array of method names
   */
  String[] getMethodNames();

  /**
   * Retrieves all types that are associated with this method, this will be every parameter and the
   * return type (if not void) of the methods from {@link #getMethodNames()}.
   *
   * @return The non-null and non-empty array of types that belong to this method
   */
  CtClass[] getTypes();

  /**
   * Retrieves the type for serialization of this method. For example if the method is 'int getX()'
   * or 'void setX(int x)', the type would be {@link Integer#TYPE int.class}.
   *
   * <p>If the method consists of multiple values (key-value) and the method looks like this 'int
   * getY(String key)', the type would be {@code Map<String, Integer>}.
   *
   * <p>If the classes are already defined in the {@link GeneratingConfig}, the implementations
   * will be used for the Type.
   *
   * @return The non-null type for serialization
   */
  Type getSerializedType();

  /**
   * Generates all methods that are necessary like the getters and setters and the necessary field
   * for those setters/getters, this depends on the implementation. If any method already exists,
   * these methods won't be generated, but it doesn't affect other methods.
   *
   * @param target The non-null class where the methods and the field should be generated
   * @throws CannotCompileException If an internal error occurred while generating the code, should
   *                                basically never happen
   */
  void generateMethods(CtClass target) throws CannotCompileException, NotFoundException;

  /**
   * Adds the {@link ConfigStorageProvider#write(ParsedConfig)} call to every setter method.
   *
   * @param target The non-null class where the methods (and the fields) should be generated
   * @throws CannotCompileException If an internal error occurred while generating the code, should
   *                                basically never happen
   * @throws NotFoundException      If any of the methods in {@link #getMethodNames()} don't exist,
   *                                they cannot be generated because we don't know the
   *                                implementation
   */
  void implementExistingMethods(CtClass target) throws CannotCompileException, NotFoundException;

  /**
   * Adds the abstract methods that will be generated by {@link #implementExistingMethods(CtClass)}
   * to the given interface.
   *
   * @param target The non-null interface where the methods should be generated
   * @throws CannotCompileException If an internal error occurred while generating the code, should
   *                                basically never happen
   * @throws NotFoundException      If any of the methods in {@link #getMethodNames()} don't exist,
   *                                they cannot be generated because we don't know the
   *                                implementation
   */
  void addInterfaceMethods(CtClass target) throws CannotCompileException, NotFoundException;
}
