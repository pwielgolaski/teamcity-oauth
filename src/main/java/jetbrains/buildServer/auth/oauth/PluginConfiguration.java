package jetbrains.buildServer.auth.oauth;

import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.groups.UserGroupManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.users.UserModel;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PluginConfiguration {

    @Bean
    public AuthenticationSchemeProperties authenticationSchemeProperties(SBuildServer sBuildServer, LoginConfiguration loginConfiguration) {
        return new AuthenticationSchemeProperties(sBuildServer, loginConfiguration);
    }

    @Bean
    public OAuthClient oAuthClient(AuthenticationSchemeProperties properties) {
        return new OAuthClient(properties);
    }

    @Bean
    public ServerPrincipalFactory serverPrincipalFactory(UserModel userModel, UserGroupManager userGroupManager, AuthenticationSchemeProperties properties) {
        return new ServerPrincipalFactory(userModel, userGroupManager, properties);
    }

    @Bean
    public LoginViaOAuthController loginController(WebControllerManager webManager,
                                            AuthorizationInterceptor authInterceptor,
                                            AuthenticationSchemeProperties schemeProperties,
                                            OAuthClient authClient) {
        return new LoginViaOAuthController(webManager, authInterceptor, schemeProperties, authClient);
    }

    @Bean
    public LoginViaOAuthLoginPageExtension loginPageExtension(PagePlaces pagePlaces,
                                                       PluginDescriptor pluginDescriptor,
                                                       AuthenticationSchemeProperties schemeProperties) {
        return new LoginViaOAuthLoginPageExtension(pagePlaces, pluginDescriptor, schemeProperties);
    }

    @Bean
    public OAuthAuthenticationScheme oAuthAuthenticationScheme(LoginConfiguration loginConfiguration,
                                                        PluginDescriptor pluginDescriptor,
                                                        ServerPrincipalFactory principalFactory,
                                                        OAuthClient authClient,
                                                        AuthenticationSchemeProperties schemeProperties) {
        OAuthAuthenticationScheme authenticationScheme = new OAuthAuthenticationScheme(pluginDescriptor, principalFactory, authClient, schemeProperties);
        loginConfiguration.registerAuthModuleType(authenticationScheme);
        return authenticationScheme;
    }
}
