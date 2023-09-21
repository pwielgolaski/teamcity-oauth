package jetbrains.buildServer.auth.oauth;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.AuthModule;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AuthenticationSchemeProperties {

    private final ConfigPresets presets;
    private final SBuildServer sBuildServer;
    private final LoginConfiguration loginConfiguration;

    public AuthenticationSchemeProperties(@NotNull final SBuildServer sBuildServer, @NotNull final LoginConfiguration loginConfiguration) {
        this.sBuildServer = sBuildServer;
        this.loginConfiguration = loginConfiguration;
        this.presets = new ConfigPresets();
    }

    @Nullable
    public String getRootUrl() {
        return sBuildServer.getRootUrl();
    }

    @Nullable
    public String getPreset() {
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
    public String getCustomName() {
        String customName = getProperty(ConfigKey.customName);
        if (StringUtil.isEmpty(customName)) {
            customName = "OAuth";
        }
        return customName;
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

    @Nullable
    public List<String> getEmailDomains() {
        return StringUtil.split(getProperty(ConfigKey.emailDomain), " ");
    }

    @Nullable
    public Set<String> getWhitelistedGroups() {
        String groupsProperty = getProperty(ConfigKey.groups);
        Set<String> groups = null;

        if (StringUtil.isNotEmpty(groupsProperty)) {
            groups = Stream.of(groupsProperty.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
        }
        return groups;
    }

    public boolean isSyncGroups() {
        return Optional.ofNullable(getProperty(ConfigKey.syncGroups))
                .map(Boolean::valueOf)
                .orElse(true);
    }

    @Nullable
    public String getOrganizations() {
        return getProperty(ConfigKey.organizations);
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
    // used by JSP loginViaOAuth.jsp
    public boolean isGuestLoginAllowed() {
        return loginConfiguration.isGuestLoginAllowed();
    }
}
