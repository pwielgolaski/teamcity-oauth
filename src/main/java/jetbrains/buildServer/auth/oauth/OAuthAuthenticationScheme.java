package jetbrains.buildServer.auth.oauth;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationResult;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationSchemeAdapter;
import jetbrains.buildServer.controllers.interceptors.auth.util.HttpAuthUtil;
import jetbrains.buildServer.serverSide.auth.AuthModuleUtil;
import jetbrains.buildServer.serverSide.auth.ServerPrincipal;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class OAuthAuthenticationScheme extends HttpAuthenticationSchemeAdapter {

    private static final boolean DEFAULT_ALLOW_CREATING_NEW_USERS_BY_LOGIN = true;
    public static final String CODE = "code";
    public static final String STATE = "state";

    private final PluginDescriptor pluginDescriptor;
    private final ServerPrincipalFactory principalFactory;
    private final OAuthClient authClient;
    private final AuthenticationSchemeProperties properties;

    public OAuthAuthenticationScheme(@NotNull final PluginDescriptor pluginDescriptor,
                                     @NotNull final ServerPrincipalFactory principalFactory,
                                     @NotNull final OAuthClient authClient,
                                     @NotNull final AuthenticationSchemeProperties properties) {
        this.pluginDescriptor = pluginDescriptor;
        this.principalFactory = principalFactory;
        this.authClient = authClient;
        this.properties = properties;
    }

    @NotNull
    @Override
    protected String doGetName() {
        return PluginConstants.OAUTH_AUTH_SCHEME_NAME;
    }

    @NotNull
    @Override
    public String getDescription() {
        return PluginConstants.OAUTH_AUTH_SCHEME_DESCRIPTION;
    }

    @Nullable
    @Override
    public String getEditPropertiesJspFilePath() {
        return pluginDescriptor.getPluginResourcesPath(PluginConstants.Web.EDIT_SCHEME_PAGE);
    }

    @Nullable
    @Override
    public Collection<String> validate(@NotNull Map<String, String> properties) {
        final Collection<String> errors = new ConfigurationValidator().validate(properties);
        return errors.isEmpty() ? super.validate(properties) : errors;
    }

    @NotNull
    @Override
    public HttpAuthenticationResult processAuthenticationRequest(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Map<String, String> schemeProperties) throws IOException {
        String code = request.getParameter(CODE);
        String state = request.getParameter(STATE);

        if (StringUtil.isEmpty(code) || StringUtil.isEmpty(state))
            return HttpAuthenticationResult.notApplicable();

        if (!state.equals(SessionUtil.getSessionId(request)))
            return sendUnauthorizedRequest(request, response, "Unauthenticated since retrieved 'state' doesn't correspond to current TeamCity session.");

        String token = authClient.getAccessToken(code);
        if (token == null) {
            return sendUnauthorizedRequest(request, response, String.format("Unauthenticated since failed to fetch token for code '%s' and state '%s'.", code, state));
        }

        OAuthUser user = authClient.getUserData(token);
        try {
            user.validate(properties);
        } catch (Exception ex) {
            return sendUnauthorizedRequest(request, response, ex.getMessage());
        }

        boolean allowCreatingNewUsersByLogin = AuthModuleUtil.allowCreatingNewUsersByLogin(schemeProperties, DEFAULT_ALLOW_CREATING_NEW_USERS_BY_LOGIN);
        final Optional<ServerPrincipal> principal = principalFactory.getServerPrincipal(user, allowCreatingNewUsersByLogin);

        if (principal.isPresent()) {
            return HttpAuthenticationResult.authenticated(principal.get(), true)
                    .withRedirect("/");
        } else {
            return sendUnauthorizedRequest(request, response, "Unauthenticated since user could not be found or created.");
        }
    }

    private HttpAuthenticationResult sendUnauthorizedRequest(HttpServletRequest request, HttpServletResponse response, String reason) throws IOException {
        HttpAuthUtil.setUnauthenticatedReason(request, reason);
        response.sendError(HttpStatus.UNAUTHORIZED.value(), reason);
        return HttpAuthenticationResult.unauthenticated();
    }
}
