package net.labyfy.gradle;

import com.google.inject.Guice;
import com.google.inject.Module;
import groovy.lang.Closure;
import net.labyfy.gradle.library.VersionFetcher;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.util.Configurable;
import org.gradle.util.ConfigureUtil;

import javax.annotation.Nonnull;

public class LabyfyGradlePlugin implements Plugin<Project> {

  public void apply(@Nonnull Project project) {
    this.createInjector(project);
  }

  private void createInjector(Project project) {
    Guice.createInjector(this.createModules(project)).getInstance(LabyfyGradle.class).apply();
  }

  private Module[] createModules(Project project) {
    return new Module[]{LabyfyGradleModule.create(project, this.createExtension(project))};
  }

  private Extension createExtension(Project project) {
    return project.getExtensions().create("minecraft", Extension.class);
  }

  public static class Extension implements Configurable<LabyfyGradlePlugin.Extension> {
    private String version;

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
    }

    public Extension configure(@Nonnull Closure closure) {
      System.out.println("Debug123");
      Extension extension = ConfigureUtil.configureSelf(closure, this);
      return extension;
    }

    public VersionFetcher.Version getDetails() {
      return VersionFetcher.fetch(this.version).getDetails();
    }
  }
}
