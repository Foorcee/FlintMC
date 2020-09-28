package net.labyfy.component.gamesettings.settings;

public enum AmbientOcclusionStatus {

  OFF("options.ao.off"),
  MIN("options.ao.min"),
  MAX("options.ao.max");

  private final String resourceKey;

  AmbientOcclusionStatus(String resourceKey) {
    this.resourceKey = resourceKey;
  }

  public String getResourceKey() {
    return resourceKey;
  }
}
