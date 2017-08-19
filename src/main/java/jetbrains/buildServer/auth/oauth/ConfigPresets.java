package jetbrains.buildServer.auth.oauth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class ConfigPresets {
    private final Map<String, Map<ConfigKey, String>> presets;

    ConfigPresets() {
        presets = new HashMap<>();
        installPreset("github",
                "https://github.com/login/oauth/authorize",
                "https://github.com/login/oauth/access_token",
                "https://api.github.com/user");
        installPreset("bitbucket",
                "https://bitbucket.org/site/oauth2/authorize",
                "https://bitbucket.org/site/oauth2/access_token",
                "https://api.bitbucket.org/2.0/user");
        installPreset("google",
                "https://accounts.google.com/o/oauth2/v2/auth",
                "https://www.googleapis.com/oauth2/v4/token",
                "https://www.googleapis.com/oauth2/v3/userinfo");
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
