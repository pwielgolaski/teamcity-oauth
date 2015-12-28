package jetbrains.buildServer.auth.oauth;

import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.serverSide.auth.AuthModule;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class AuthenticationSchemeProperties {

    private ServerSettings serverSettings;
    private LoginConfiguration loginConfiguration;

    public AuthenticationSchemeProperties(@NotNull final ServerSettings serverSettings, @NotNull final LoginConfiguration loginConfiguration) {
        this.serverSettings = serverSettings;
        this.loginConfiguration = loginConfiguration;
    }

    @Nullable
    public String getRootUrl() {
        return serverSettings.getRootUrl();
    }

    @Nullable
    public String getAuthorizeEndpoint() {
        return getProperty(ConfigKey.authorizeEndpoint);
    }

    @Nullable
    public String getTokenEndpoint() {
        return getProperty(ConfigKey.tokenEndpoint);
    }

    @Nullable
    public String getUserEndpoint() {
        return getProperty(ConfigKey.userEndpoint);
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

    public boolean isSchemeConfigured() {
        return !loginConfiguration.getConfiguredAuthModules(OAuthAuthenticationScheme.class).isEmpty();
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
}
