package jetbrains.buildServer.auth.oauth;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigurationValidator {

    private static final Map<ConfigKey, String> EMPTY_KEY_VALUE_MESSAGES;

    static {
        EMPTY_KEY_VALUE_MESSAGES = new HashMap<>();
        EMPTY_KEY_VALUE_MESSAGES.put(ConfigKey.authorizeEndpoint, "Authorization endpoint should be specified.");
        EMPTY_KEY_VALUE_MESSAGES.put(ConfigKey.tokenEndpoint, "Token endpoint should be specified.");
        EMPTY_KEY_VALUE_MESSAGES.put(ConfigKey.userEndpoint, "User endpoint should be specified.");
        EMPTY_KEY_VALUE_MESSAGES.put(ConfigKey.clientId, "Client ID should be specified.");
        EMPTY_KEY_VALUE_MESSAGES.put(ConfigKey.clientSecret, "Client secret should be specified.");
    }

    private final ConfigPresets presets;

    public ConfigurationValidator() {
        presets = new ConfigPresets();
    }

    public Collection<String> validate(@NotNull Map<String, String> properties) {
        String preset = properties.get(ConfigKey.preset.toString());
        presets.getPreset(preset).forEach( (k, v) -> properties.put(k.toString(), v));
        final Collection<String> errors = EMPTY_KEY_VALUE_MESSAGES.entrySet().stream()
                .filter(mapping -> StringUtil.isEmptyOrSpaces(properties.get(mapping.getKey().toString())))
                .map(Map.Entry::getValue)
                .collect(Collectors.toCollection(ArrayList::new));
        return errors;
    }

}
