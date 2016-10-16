package jetbrains.buildServer.auth.oauth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ConfigPresets {
    private Map<String, Map<ConfigKey, String>> presets;

    public ConfigPresets() {
        presets = new HashMap<>();
        installPreset("github", "https://github.com/login/oauth/authorize", "https://github.com/login/oauth/access_token", "https://api.github.com/user");
    }

    private void installPreset(String name, String authorizeEndpoint, String tokenEndpoint, String userEndpoint) {
        Map<ConfigKey, String> preset = new HashMap<>();
        preset.put(ConfigKey.authorizeEndpoint, authorizeEndpoint);
        preset.put(ConfigKey.tokenEndpoint, tokenEndpoint);
        preset.put(ConfigKey.userEndpoint, userEndpoint);
        presets.put(name, preset);
    }

    Optional<String> getPresetKey(String name, ConfigKey key) {
        Map<ConfigKey, String> preset = presets.get(name);
        return preset == null ? Optional.empty() : Optional.ofNullable(preset.get(key));
    }

    Map<ConfigKey, String> getPreset(String name) {
        return presets.getOrDefault(name, Collections.emptyMap());
    }

}
