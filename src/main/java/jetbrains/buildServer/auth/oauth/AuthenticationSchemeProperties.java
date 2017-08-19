package jetbrains.buildServer.auth.oauth;

import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.serverSide.auth.AuthModule;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AuthenticationSchemeProperties {

    private final ConfigPresets presets;
    private final ServerSettings serverSettings;
    private final LoginConfiguration loginConfiguration;

    public AuthenticationSchemeProperties(@NotNull final ServerSettings serverSettings, @NotNull final LoginConfiguration loginConfiguration) {
        this.serverSettings = serverSettings;
        this.loginConfiguration = loginConfiguration;
        this.presets = new ConfigPresets();
    }

    @Nullable
    public String getRootUrl() {
        return serverSettings.getRootUrl();
    }

    @Nullable
    private String getPreset() {
        return getProperty(ConfigKey.preset);
    }

    @Nullable
    public String getAuthorizeEndpoint() {
        return getPresetProperty(ConfigKey.authorizeEndpoint);
    }

    @Nullable
    public String getTokenEndpoint() {
        return getPresetProperty(ConfigKey.tokenEndpoint);
    }

    @Nullable
    public String getUserEndpoint() {
        return getPresetProperty(ConfigKey.userEndpoint);
    }

    @Nullable
    public String getClientId() {
        return getProperty(ConfigKey.clientId);
    }

    @Nullable
    public String getClientSecret() {
        return getProperty(ConfigKey.clientSecret);
    }

    @Nullable
    public String getScope() {
        return getProperty(ConfigKey.scope);
    }

    public boolean isHideLoginForm() {
        return Boolean.valueOf(getProperty(ConfigKey.hideLoginForm));
    }

    public boolean isSchemeConfigured() {
        return !loginConfiguration.getConfiguredAuthModules(OAuthAuthenticationScheme.class).isEmpty();
    }

    private String getPresetProperty(ConfigKey key) {
        return presets.getPresetKey(getPreset(), key).orElseGet(() -> getProperty(key));
    }

    private String getProperty(ConfigKey key) {
        Map<String, String> properties = getSchemeProperties();
        return properties == null ? null : properties.get(key.toString());

    }

    @Nullable
    private Map<String, String> getSchemeProperties() {
        List<AuthModule<OAuthAuthenticationScheme>> aadAuthModules = loginConfiguration.getConfiguredAuthModules(OAuthAuthenticationScheme.class);
        return aadAuthModules.isEmpty() ? null : aadAuthModules.get(0).getProperties();
    }

    public boolean isAllowInsecureHttps() {
        return Optional.ofNullable(getProperty(ConfigKey.allowInsecureHttps))
                .map(Boolean::valueOf)
                .orElse(true);
    }
}
