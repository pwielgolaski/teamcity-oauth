package jetbrains.buildServer.auth.oauth;

import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;

public class LoginViaOAuthLoginPageExtension extends SimplePageExtension {


    public LoginViaOAuthLoginPageExtension(@NotNull final PagePlaces pagePlaces,
                                           @NotNull final PluginDescriptor pluginDescriptor) {
        super(pagePlaces,
                PlaceId.LOGIN_PAGE,
                LoginViaOAuthLoginPageExtension.class.getName(),
                pluginDescriptor.getPluginResourcesPath(PluginConstants.Web.LOGIN_EXTENSION_PAGE));
        register();
    }
}