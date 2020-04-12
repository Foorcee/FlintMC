package net.labyfy.component.packages.impl;

import com.google.common.base.Preconditions;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import net.labyfy.component.inject.implement.Implement;
import net.labyfy.component.packages.Package;
import net.labyfy.component.packages.*;

import java.util.Optional;
import java.util.jar.JarFile;

@Implement(Package.class)
public class LabyPackage implements Package {

  private JarFile jarFile;
  private PackageDescription packageDescription;
  private PackageClassLoader.Factory classLoaderFactory;
  private PackageState packageState;
  private Exception loadException;

  @AssistedInject
  private LabyPackage(
      PackageDescriptionLoader descriptionLoader,
      PackageClassLoader.Factory classLoaderFactory,
      @Assisted JarFile jarFile) {
    Preconditions.checkNotNull(jarFile);
    Preconditions.checkArgument(
        descriptionLoader.isDescriptionPresent(jarFile),
        "The given JAR File (%s) does not contain a valid package description.",
        jarFile.getName());

    this.jarFile = jarFile;
    Optional<PackageDescription> optionalDescription = descriptionLoader.loadDescription(jarFile);
    if (optionalDescription.isPresent() && optionalDescription.get().isValid()) {
      this.classLoaderFactory = classLoaderFactory;
      this.packageState = PackageState.NOT_LOADED;
    } else {
      this.packageState = PackageState.INVALID_DESCRIPTION;
    }
    optionalDescription.ifPresent(description -> this.packageDescription = description);
    this.loadException = null;
  }

  @Override
  public String getName() {
    return this.packageDescription != null ? this.packageDescription.getName() : jarFile.getName();
  }

  @Override
  public String getDisplayName() {
    return this.packageDescription != null
        ? this.packageDescription.getDisplayName()
        : jarFile.getName();
  }

  @Override
  public String getVersion() {
    return this.packageDescription != null ? this.packageDescription.getVersion() : "unknown";
  }

  @Override
  public PackageState getState() {
    return this.packageState;
  }

  @Override
  public void setState(PackageState state) {
    Preconditions.checkState(this.packageState.equals(PackageState.NOT_LOADED));
    Preconditions.checkArgument(
        !state.equals(PackageState.LOADED),
        "The package state can't be explicitly set to LOADED. "
            + "To get into the LOADED state, you must call the load() method.");

    this.packageState = state;
  }

  @Override
  public PackageState load() {
    Preconditions.checkState(
        this.packageState.equals(PackageState.NOT_LOADED),
        "The package must be in NOT_LOADED state to be loaded.");

    // TODO: find autoload classes and submit them for annotation parsing

    try {
      PackageClassLoader classLoader = this.classLoaderFactory.create(this.jarFile);

      this.packageState = PackageState.LOADED;
    } catch (Exception e) {
      this.packageState = PackageState.ERRORED;
      this.loadException = e;
    }
    return this.packageState;
  }

  @Override
  public Exception getLoadException() {
    Preconditions.checkState(this.packageState.equals(PackageState.ERRORED));
    Preconditions.checkNotNull(this.loadException);
    return this.loadException;
  }
}
