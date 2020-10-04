package net.labyfy.internal.component.gamesettings.frontend;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import net.labyfy.component.gamesettings.frontend.FrontendCommunicator;
import net.labyfy.component.gamesettings.frontend.FrontendOption;
import net.labyfy.component.gamesettings.settings.*;
import net.labyfy.component.inject.implement.Implement;
import net.labyfy.component.player.util.Hand;
import net.labyfy.component.world.difficult.Difficulty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Default implementation of {@link FrontendCommunicator}.
 */
@Singleton
@Implement(FrontendCommunicator.class)
public class DefaultFrontendCommunicator implements FrontendCommunicator {

  private static final Pattern NUMERIC_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

  private final Multimap<String, FrontendOption> configurations;
  private final JsonObject configurationObject;
  private final Map<String, String> launchArguments;
  private final EnumConstantHelper enumConstantHelper;

  @Inject
  private DefaultFrontendCommunicator(
          @Named("launchArguments") Map launchArguments,
          FrontendOption.Factory frontedTypeFactory,
          EnumConstantHelper enumConstantHelper
  ) {
    this.launchArguments = launchArguments;
    this.configurationObject = new JsonObject();
    this.configurations = HashMultimap.create();
    this.enumConstantHelper = enumConstantHelper;

    // Registers default options
    this.setupDefaultOption(frontedTypeFactory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JsonObject parseOption(Map<String, String> configurations) {
    Map<String, String> fixedConfiguration = this.prettyConfiguration(configurations);
    for (Map.Entry<String, FrontendOption> entry : this.configurations.entries()) {
      JsonObject configEntry = this.configurationObject.getAsJsonObject(entry.getKey());

      if (configEntry == null) {
        configEntry = new JsonObject();
      }
      FrontendOption type = entry.getValue();

      String configurationValue = fixedConfiguration.get(type.getConfigurationName());

      if (configurationValue == null) {
        configurationValue = type.getDefaultValue();
      }

      // Checks if the class not an enumeration
      if (!type.getType().isEnum()) {
        // Setups the configurations for all non enumerations.
        if (type.getType().equals(String.class)) {
          configEntry.addProperty(type.getConfigurationName(), configurationValue);
        } else if (type.getType().equals(Boolean.TYPE)) {
          configEntry.addProperty(type.getConfigurationName(), Boolean.parseBoolean(configurationValue));
        } else if (type.getType().equals(List.class)) {
          this.createListOption(configEntry, type, configurationValue);
        } else if (isNumeric(configurationValue)) {
          this.createNumberOption(configEntry, type, configurationValue);
        }
      } else {
        // Setups an enumeration option
        this.createEnumOption(configEntry, type, configurationValue);
      }

      this.configurationObject.add(entry.getKey(), configEntry);
    }

    return configurationObject;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<String, String> parseJson(JsonObject object) {
    Map<String, String> configurations = new HashMap<>();


    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
      for (Map.Entry<String, JsonElement> elementEntry : entry.getValue().getAsJsonObject().entrySet()) {
        if (elementEntry.getValue().isJsonPrimitive()) {
          if (entry.getKey().equalsIgnoreCase("keys")) {
            configurations.put("key_key." + elementEntry.getKey(), elementEntry.getValue().toString());
          } else if (entry.getKey().equalsIgnoreCase("sounds")) {
            configurations.put("soundCategory_" + elementEntry.getKey(), elementEntry.getValue().toString());
          } else if (entry.getKey().equalsIgnoreCase("skinModel")) {
            configurations.put("modelPart_" + this.convertToSnakeCase(elementEntry.getKey()), elementEntry.getValue().toString());
          } else {
            if (elementEntry.getKey().startsWith("discrete")) {
              configurations.put(this.convertToSnakeCase(elementEntry.getKey()), elementEntry.getValue().toString());
              continue;
            }
            configurations.put(elementEntry.getKey(), elementEntry.getValue().toString());
          }
        } else {
          if (!elementEntry.getValue().isJsonArray()) {
            String selected = elementEntry.getValue().getAsJsonObject().get("selected").toString().replace("\"", "");
            if (elementEntry.getKey().equalsIgnoreCase("tutorialStep") || elementEntry.getKey().equalsIgnoreCase("mainHand")) {
              configurations.put(elementEntry.getKey(), selected.toLowerCase());
            } else if (elementEntry.getKey().equalsIgnoreCase("renderClouds")) {
              CloudOption cloudOption = CloudOption.valueOf(selected);

              switch (cloudOption) {
                case OFF:
                  configurations.put(elementEntry.getKey(), "false");
                  break;
                case FAST:
                  configurations.put(elementEntry.getKey(), "fast");
                  break;
                case FANCY:
                  configurations.put(elementEntry.getKey(), "true");
                  break;
              }
            } else {
              Class<?> cls = this.getType(entry.getKey(), elementEntry.getKey());

              if (cls == null) continue;

              if (!cls.isEnum()) {
                configurations.put(elementEntry.getKey(), selected);
              } else {
                configurations.put(elementEntry.getKey(), String.valueOf(this.enumConstantHelper.getOrdinal(cls, selected)));
              }

            }

          }
        }
      }
    }

    return configurations;
  }

  /**
   * Creates an enumeration option for the `JSON` configuration. An example of what the enumeration option looks like:
   * <pre>
   *     {@code
   * "selected": "FOO",
   * "options": ["FOO", "BAR"]
   *     }
   * </pre>
   *
   * @param configEntry An entry of the configuration.
   * @param type        The type of the option.
   * @param value       The current value.
   */
  private void createEnumOption(JsonObject configEntry, FrontendOption type, String value) {
    if (type.getConfigurationName().equalsIgnoreCase("renderClouds")) {
      if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
        value = Boolean.parseBoolean(value) ? CloudOption.FANCY.name() : CloudOption.OFF.name();
      } else {
        value = CloudOption.FAST.name();
      }
    } else if (type.getConfigurationName().equalsIgnoreCase("fancyGraphics")) {
      if (this.getMinorVersion(this.launchArguments.get("--game-version")) < 16) {
        value = Boolean.getBoolean(value) ? GraphicsFanciness.FANCY.name() : GraphicsFanciness.FAST.name();
      } else {
        value = type.getType().getEnumConstants()[Integer.parseInt(value)].toString();
      }
    } else if (isNumeric(value)) {
      value = this.enumConstantHelper.getConstantByOrdinal(type.getType(), Integer.parseInt(value));
    } else {
      value = value.toUpperCase();
    }

    // Adds the enum to the configuration entry.
    configEntry.add(
            type.getConfigurationName(),
            this.createSelectEntry(
                    value,
                    this.enumConstantHelper.getConstants(type.getType()).values().toArray()
            )
    );

  }

  /**
   * Creates an number option for the `JSON` configuration. An example of what the number options look like:
   * Integer:
   * <pre>
   * {@code
   * "selected" 2,
   * "min": 1,
   * "max": 53
   * }
   * </pre>
   * Double:
   * <pre>
   * {@code
   * "selected" 2.0,
   * "min": 0.035,
   * "max": 3.364
   * }
   * </pre>
   *
   * @param configEntry An entry of the configuration.
   * @param type        The type of the option.
   * @param value       The current value.
   */
  private void createNumberOption(JsonObject configEntry, FrontendOption type, String value) {
    if (type.getType().equals(Integer.TYPE)) {
      if (type.getMin() != type.getMax()) {
        configEntry.add(
                type.getConfigurationName(),
                this.createSelectEntry(
                        Integer.parseInt(value),
                        type.getMin(),
                        type.getMax()
                )
        );
      } else {
        configEntry.addProperty(type.getConfigurationName(), Integer.parseInt(value));
      }
    } else if (type.getType().equals(Double.TYPE)) {
      if (type.getMinValue() != type.getMaxValue()) {
        configEntry.add(
                type.getConfigurationName(),
                this.createSelectEntry(
                        Double.parseDouble(value),
                        type.getMinValue(),
                        type.getMaxValue()
                )
        );
      } else {
        configEntry.addProperty(type.getConfigurationName(), Double.parseDouble(value));
      }
    }
  }

  /**
   * Creates a list option for the `JSON` configuration. An example of how the list options look like:
   * <pre>
   * {@code
   * "fooBarList": ["foo", "bar"]
   * }
   * </pre>
   *
   * @param configEntry An entry of the configuration.
   * @param type        The type of the option.
   * @param value       The current value.
   */
  private void createListOption(JsonObject configEntry, FrontendOption type, String value) {
    if (!value.contains(",") && !(value.startsWith("[") && value.endsWith("]"))) return;

    value = value.replace("[", "").replace("]", "");

    String[] split = value.split(",");

    JsonArray array = new JsonArray();

    for (String s : split) {
      array.add(s);
    }

    configEntry.add(type.getConfigurationName(), array);
  }

  /**
   * Creates a select entry.
   *
   * @param selected The current selected value.
   * @param array    An array with all available options.
   * @return A select entry for the configuration.
   * @see DefaultFrontendCommunicator#createEnumOption(JsonObject, FrontendOption, String)
   * @see DefaultFrontendCommunicator#createNumberOption(JsonObject, FrontendOption, String)
   */
  private JsonObject createSelectEntry(Object selected, Object... array) {
    JsonObject object = new JsonObject();
    if (selected instanceof String) {
      object.addProperty("selected", (String) selected);
      JsonArray jsonArray = new JsonArray();
      for (Object o : array) {
        jsonArray.add((String) o);
      }
      object.add("options", jsonArray);
    } else if (selected instanceof Number) {
      object.addProperty("selected", (Number) selected);
      object.addProperty("min", (Number) array[0]);
      object.addProperty("max", (Number) array[1]);
    } else if (selected instanceof Character) {
      object.addProperty("selected", (Character) selected);
      JsonArray jsonArray = new JsonArray();
      for (Object o : array) {
        jsonArray.add((Character) o);
      }
      object.add("options", jsonArray);

    }
    return object;
  }

  /**
   * Retrieves the class by the given category and configuration name.
   *
   * @param category          The category name.
   * @param configurationName The name of the configuration.
   * @return A class identified by the category and configuration name or {@code null}.
   */
  private Class<?> getType(String category, String configurationName) {
    for (FrontendOption type : this.configurations.get(category)) {
      if (type.getConfigurationName().equalsIgnoreCase(configurationName)) {
        return type.getType();
      }
    }
    return null;
  }

  /**
   * Converts the given name to snake case.
   *
   * @param name The name to be convert
   * @return The snake cased name.
   */
  private String convertToSnakeCase(String name) {
    StringBuilder builder = new StringBuilder();

    for (char c : name.toCharArray()) {
      if (Character.isUpperCase(c)) {
        builder.append("_").append(Character.toLowerCase(c));
        continue;
      }
      builder.append(c);
    }

    return builder.toString();
  }

  /**
   * Whether the given value is a numeric.
   *
   * @param value The value to be checked
   * @return {@code true} if the given value a numeric, otherwise {@code false}.
   */
  private boolean isNumeric(String value) {
    if (value == null || value.isEmpty()) {
      return false;
    }

    return NUMERIC_PATTERN.matcher(value).matches();
  }

  /**
   * Removes the ugly underscores and makes any options to lower camel case.
   *
   * @param configurations The configuration that is made pretty.
   * @return A key-value system with pretty keys.
   */
  private Map<String, String> prettyConfiguration(Map<String, String> configurations) {
    Map<String, String> prettyConfiguration = new HashMap<>();

    for (Map.Entry<String, String> entry : configurations.entrySet()) {
      StringBuilder key = new StringBuilder(entry.getKey()
              .replace("key_key.", "")
              .replace("modelPart_", "")
              .replace("soundCategory_", ""));

      if (key.toString().contains("_")) {
        String[] split = key.toString().split("_");

        key = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
          if (i == 0) {
            key.append(split[i]);
            continue;
          }

          key.append(split[i].substring(0, 1).toUpperCase()).append(split[i].substring(1));
        }
      }

      prettyConfiguration.put(key.toString(), entry.getValue());
    }
    return prettyConfiguration;
  }

  /**
   * Setups the default options.
   *
   * @param frontedOptionFactory The factory to create {@link FrontendOption}'s.
   */
  private void setupDefaultOption(FrontendOption.Factory frontedOptionFactory) {
    this.configurations.put("none", frontedOptionFactory.create("fov", Double.TYPE, "0.0").setRange(-1D, 1D));
    this.configurations.put("none", frontedOptionFactory.create("realmsNotifications", Boolean.TYPE, "true"));
    this.configurations.put("none", frontedOptionFactory.create("difficulty", Difficulty.class, "2"));

    this.configurations.put("accessibility", frontedOptionFactory.create("toggleSprint", Boolean.TYPE, "false"));
    this.configurations.put("accessibility", frontedOptionFactory.create("toggleCrouch", Boolean.TYPE, "false"));
    this.configurations.put("accessibility", frontedOptionFactory.create("autoJump", Boolean.TYPE, "true"));

    this.configurations.put("chat", frontedOptionFactory.create("chatLinksPrompt", Boolean.TYPE, "true"));
    this.configurations.put("chat", frontedOptionFactory.create("backgroundForChatOnly", Boolean.TYPE, "true"));
    this.configurations.put("chat", frontedOptionFactory.create("chatColors", Boolean.TYPE, "true"));
    this.configurations.put("chat", frontedOptionFactory.create("autoSuggestions", Boolean.TYPE, "true"));
    this.configurations.put("chat", frontedOptionFactory.create("chatLinks", Boolean.TYPE, "true"));
    this.configurations.put("chat", frontedOptionFactory.create("reducedDebugInfo", Boolean.TYPE, "false"));
    this.configurations.put("chat", frontedOptionFactory.create("chatOpacity", Double.TYPE, "1.0").setRange(0D, 1D));
    this.configurations.put("chat", frontedOptionFactory.create("chatHeightFocused", Double.TYPE, "1.0").setRange(0D, 1D));
    this.configurations.put("chat", frontedOptionFactory.create("chatHeightUnfocused", Double.TYPE, "0.44366195797920227").setRange(0D, 1D));
    this.configurations.put("chat", frontedOptionFactory.create("chatWidth", Double.TYPE, "1.0").setRange(0D, 1D));
    this.configurations.put("chat", frontedOptionFactory.create("chatScale", Double.TYPE, "1.0").setRange(0D, 1D));
    this.configurations.put("chat", frontedOptionFactory.create("textBackgroundOpacity", Double.TYPE, "0.5").setRange(0D, 1D));
    this.configurations.put("chat", frontedOptionFactory.create("chatVisibility", ChatVisibility.class, "0"));
    this.configurations.put("chat", frontedOptionFactory.create("narrator", NarratorStatus.class, "0"));
    // 1.16
    this.configurations.put("chat", frontedOptionFactory.create("chatDelay", Double.TYPE, "0.0").setRange(0D, 6D));
    this.configurations.put("chat", frontedOptionFactory.create("chatLineSpacing", Double.TYPE, "0.0").setRange(0D, 6D));

    this.configurations.put("graphics", frontedOptionFactory.create("attackIndicator", AttackIndicatorStatus.class, "1"));
    this.configurations.put("graphics", frontedOptionFactory.create("ao", AmbientOcclusionStatus.class, "2"));
    this.configurations.put("graphics", frontedOptionFactory.create("bobView", Boolean.TYPE, "true"));
    this.configurations.put("graphics", frontedOptionFactory.create("overrideHeight", Integer.TYPE, "0"));
    this.configurations.put("graphics", frontedOptionFactory.create("overrideWidth", Integer.TYPE, "0"));
    this.configurations.put("graphics", frontedOptionFactory.create("heldItemTooltips", Boolean.TYPE, "true"));
    this.configurations.put("graphics", frontedOptionFactory.create("gamma", Double.TYPE, "0.0").setRange(0D, 1D));
    this.configurations.put("graphics", frontedOptionFactory.create("biomeBlendRadius", Integer.TYPE, "2").setRange(0D, 7D));
    this.configurations.put("graphics", frontedOptionFactory.create("forceUnicodeFont", AttackIndicatorStatus.class, "false"));
    this.configurations.put("graphics", frontedOptionFactory.create("guiScale", Integer.TYPE, "0").setRange(0, 2));
    this.configurations.put("graphics", frontedOptionFactory.create("renderClouds", CloudOption.class, "true"));
    this.configurations.put("graphics", frontedOptionFactory.create("maxFps", Integer.TYPE, "120").setRange(10, 260));
    this.configurations.put("graphics", frontedOptionFactory.create("glDebugVerbosity", Integer.TYPE, "1"));
    this.configurations.put("graphics", frontedOptionFactory.create("skipMultiplayerWarning", Boolean.TYPE, "false"));
    this.configurations.put("graphics", frontedOptionFactory.create("renderDistance", Integer.TYPE, "12").setRange(2, 32));
    this.configurations.put("graphics", frontedOptionFactory.create("fullscreen", Boolean.TYPE, "false"));
    this.configurations.put("graphics", frontedOptionFactory.create("fullscreenResolution", String.class, ""));
    this.configurations.put("graphics", frontedOptionFactory.create("entityShadows", Boolean.TYPE, "true"));
    this.configurations.put("graphics", frontedOptionFactory.create("advancedItemTooltips", Boolean.TYPE, "false"));
    this.configurations.put("graphics", frontedOptionFactory.create("particles", ParticleStatus.class, "0"));
    this.configurations.put("graphics", frontedOptionFactory.create("enableVsync", Boolean.TYPE, "true"));
    this.configurations.put("graphics", frontedOptionFactory.create("fancyGraphics", GraphicsFanciness.class, "true"));
    this.configurations.put("graphics", frontedOptionFactory.create("mipmapLevels", Integer.class, "4").setRange(0, 4));
    // 1.16
    this.configurations.put("graphics", frontedOptionFactory.create("entityDistanceScaling", Double.TYPE, "1.0").setRange(0.5D, 5D));
    this.configurations.put("graphics", frontedOptionFactory.create("screenEffectScale", Double.TYPE, "1.0").setRange(0D, 1D));
    this.configurations.put("graphics", frontedOptionFactory.create("fovEffectScale", Double.TYPE, "1.0").setRange(0D, 1D));

    this.configurations.put("mouse", frontedOptionFactory.create("touchscreen", Boolean.TYPE, "false"));
    this.configurations.put("mouse", frontedOptionFactory.create("discreteMouseScroll", Boolean.TYPE, "false"));
    this.configurations.put("mouse", frontedOptionFactory.create("invertYMouse", Boolean.TYPE, "false"));
    this.configurations.put("mouse", frontedOptionFactory.create("rawMouseInput", Boolean.TYPE, "true"));
    this.configurations.put("mouse", frontedOptionFactory.create("mouseSensitivity", Double.TYPE, "0.5").setRange(0D, 1D));
    this.configurations.put("mouse", frontedOptionFactory.create("mouseWheelSensitivity", Double.TYPE, "1.0").setRange(0.01D, 10D));

    this.configurations.put("keys", frontedOptionFactory.create("pickItem", String.class, "key.mouse.middle"));
    this.configurations.put("keys", frontedOptionFactory.create("playerlist", String.class, "key.keyboard.tab"));
    this.configurations.put("keys", frontedOptionFactory.create("advancements", String.class, "key.keyboard.l"));
    this.configurations.put("keys", frontedOptionFactory.create("sprint", String.class, "key.keyboard.left.control"));
    this.configurations.put("keys", frontedOptionFactory.create("forward", String.class, "key.keyboard.w"));
    this.configurations.put("keys", frontedOptionFactory.create("drop", String.class, "key.keyboard.q"));
    this.configurations.put("keys", frontedOptionFactory.create("back", String.class, "key.keyboard.s"));
    this.configurations.put("keys", frontedOptionFactory.create("attack", String.class, "key.mouse.left"));
    this.configurations.put("keys", frontedOptionFactory.create("saveToolbarActivator", String.class, "key.keyboard.c"));
    this.configurations.put("keys", frontedOptionFactory.create("loadToolbarActivator", String.class, "key.keyboard.x"));
    this.configurations.put("keys", frontedOptionFactory.create("swapHands", String.class, "key.keyboard.f"));
    this.configurations.put("keys", frontedOptionFactory.create("fullscreen", String.class, "key.keyboard.f12"));
    this.configurations.put("keys", frontedOptionFactory.create("chat", String.class, "key.keyboard.t"));
    this.configurations.put("keys", frontedOptionFactory.create("togglePerspective", String.class, "key.keyboard.f5"));
    this.configurations.put("keys", frontedOptionFactory.create("screenshot", String.class, "key.keyboard.f2"));
    this.configurations.put("keys", frontedOptionFactory.create("command", String.class, "key.keyboard.slash"));
    this.configurations.put("keys", frontedOptionFactory.create("left", String.class, "key.keyboard.a"));
    this.configurations.put("keys", frontedOptionFactory.create("spectatorOutlines", String.class, "key.keyboard.unknown"));
    this.configurations.put("keys", frontedOptionFactory.create("sneak", String.class, "key.keyboard.left.shift"));
    this.configurations.put("keys", frontedOptionFactory.create("jump", String.class, "key.keyboard.space"));
    this.configurations.put("keys", frontedOptionFactory.create("right", String.class, "key.keyboard.d"));
    this.configurations.put("keys", frontedOptionFactory.create("smoothCamera", String.class, "key.keyboard.unknown"));
    this.configurations.put("keys", frontedOptionFactory.create("use", String.class, "key.mouse.right"));
    this.configurations.put("keys", frontedOptionFactory.create("hotbar.1", String.class, "key.keyboard.1"));
    this.configurations.put("keys", frontedOptionFactory.create("hotbar.2", String.class, "key.keyboard.2"));
    this.configurations.put("keys", frontedOptionFactory.create("hotbar.3", String.class, "key.keyboard.3"));
    this.configurations.put("keys", frontedOptionFactory.create("hotbar.4", String.class, "key.keyboard.4"));
    this.configurations.put("keys", frontedOptionFactory.create("hotbar.5", String.class, "key.keyboard.5"));
    this.configurations.put("keys", frontedOptionFactory.create("hotbar.6", String.class, "key.keyboard.6"));
    this.configurations.put("keys", frontedOptionFactory.create("hotbar.7", String.class, "key.keyboard.7"));
    this.configurations.put("keys", frontedOptionFactory.create("hotbar.8", String.class, "key.keyboard.8"));
    this.configurations.put("keys", frontedOptionFactory.create("hotbar.9", String.class, "key.keyboard.9"));

    this.configurations.put("skinModel", frontedOptionFactory.create("cape", Boolean.TYPE, "true"));
    this.configurations.put("skinModel", frontedOptionFactory.create("jacket", Boolean.TYPE, "true"));
    this.configurations.put("skinModel", frontedOptionFactory.create("leftSleeve", Boolean.TYPE, "true"));
    this.configurations.put("skinModel", frontedOptionFactory.create("rightSleeve", Boolean.TYPE, "true"));
    this.configurations.put("skinModel", frontedOptionFactory.create("rightPantsLeg", Boolean.TYPE, "true"));
    this.configurations.put("skinModel", frontedOptionFactory.create("leftPantsLeg", Boolean.TYPE, "true"));
    this.configurations.put("skinModel", frontedOptionFactory.create("mainHand", Hand.Side.class, "right"));

    this.configurations.put("sounds", frontedOptionFactory.create("master", Double.TYPE, "1.0").setRange(0D, 1D));
    this.configurations.put("sounds", frontedOptionFactory.create("voice", Double.TYPE, "1.0").setRange(0D, 1D));
    this.configurations.put("sounds", frontedOptionFactory.create("record", Double.TYPE, "1.0").setRange(0D, 1D));
    this.configurations.put("sounds", frontedOptionFactory.create("music", Double.TYPE, "1.0").setRange(0D, 1D));
    this.configurations.put("sounds", frontedOptionFactory.create("weather", Double.TYPE, "1.0").setRange(0D, 1D));
    this.configurations.put("sounds", frontedOptionFactory.create("block", Double.TYPE, "1.0").setRange(0D, 1D));
    this.configurations.put("sounds", frontedOptionFactory.create("player", Double.TYPE, "1.0").setRange(0D, 1D));
    this.configurations.put("sounds", frontedOptionFactory.create("neutral", Double.TYPE, "1.0").setRange(0D, 1D));
    this.configurations.put("sounds", frontedOptionFactory.create("ambient", Double.TYPE, "1.0").setRange(0D, 1D));
    this.configurations.put("sounds", frontedOptionFactory.create("hostile", Double.TYPE, "1.0").setRange(0D, 1D));
    this.configurations.put("sounds", frontedOptionFactory.create("showSubtitles", Boolean.TYPE, "false"));

    this.configurations.put("resources", frontedOptionFactory.create("resourcePacks", List.class, "[]"));
    this.configurations.put("resources", frontedOptionFactory.create("incompatibleResourcePacks", List.class, "[]"));

    this.configurations.put("other", frontedOptionFactory.create("useNativeTransport", Boolean.TYPE, "true"));
    this.configurations.put("other", frontedOptionFactory.create("hideServerAddress", Boolean.TYPE, "false"));
    this.configurations.put("other", frontedOptionFactory.create("pauseOnLostFocus", Boolean.TYPE, "false"));
    this.configurations.put("other", frontedOptionFactory.create("lastServer", String.class, ""));
    this.configurations.put("other", frontedOptionFactory.create("lang", String.class, "en_US"));
    this.configurations.put("other", frontedOptionFactory.create("tutorialStep", TutorialSteps.class, "movement"));
    this.configurations.put("other", frontedOptionFactory.create("snooperEnabled", Boolean.TYPE, "false"));
    // 1.16
    this.configurations.put("other", frontedOptionFactory.create("syncChunkWrites", Boolean.TYPE, "true"));
  }

}
