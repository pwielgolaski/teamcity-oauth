package jetbrains.buildServer.auth.oauth;

import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class LoginViaOAuthController extends BaseController {

    private final AuthenticationSchemeProperties schemeProperties;
    private final OAuthClient authClient;

    public LoginViaOAuthController(@NotNull final WebControllerManager webManager,
                                   @NotNull final AuthorizationInterceptor authInterceptor,
                                   @NotNull final AuthenticationSchemeProperties schemeProperties,
                                   @NotNull final OAuthClient authClient) {
        this.schemeProperties = schemeProperties;
        this.authClient = authClient;
        webManager.registerController(PluginConstants.Web.LOGIN_PATH, this);
        authInterceptor.addPathNotRequiringAuth(PluginConstants.Web.LOGIN_PATH);
    }

    @Override
    protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
        if (!schemeProperties.isSchemeConfigured()) {
            return null;
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String sessionId = SessionUtil.getSessionId(request);
        String sessionIdHash = new String(digest.digest(sessionId.getBytes(StandardCharsets.UTF_8)));
        return new ModelAndView(new RedirectView(authClient.getRedirectUrl(sessionIdHash)));
    }
}
