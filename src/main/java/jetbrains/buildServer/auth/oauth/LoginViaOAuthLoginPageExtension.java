package jetbrains.buildServer.auth.oauth;

import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public class LoginViaOAuthLoginPageExtension extends SimplePageExtension {

    @NotNull
    private final AuthenticationSchemeProperties schemeProperties;

    public LoginViaOAuthLoginPageExtension(@NotNull final PagePlaces pagePlaces,
                                           @NotNull final PluginDescriptor pluginDescriptor,
                                           @NotNull final AuthenticationSchemeProperties schemeProperties) {
        super(pagePlaces,
                PlaceId.LOGIN_PAGE,
                LoginViaOAuthLoginPageExtension.class.getName(),
                pluginDescriptor.getPluginResourcesPath(PluginConstants.Web.LOGIN_EXTENSION_PAGE));
        this.schemeProperties = schemeProperties;
        register();
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
        super.fillModel(model, request);
        model.put("oauth2_settings", schemeProperties);
    }
}