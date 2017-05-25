package jetbrains.buildServer.auth.oauth;

import com.google.common.base.Strings;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationResult;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationSchemeAdapter;
import jetbrains.buildServer.serverSide.auth.LoginConfiguration;
import jetbrains.buildServer.serverSide.auth.ServerPrincipal;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;


public class OAuthAuthenticationScheme extends HttpAuthenticationSchemeAdapter {

    private static final Logger LOG = Logger.getLogger(OAuthAuthenticationScheme.class);
    public static final String CODE = "code";
    public static final String STATE = "state";
    public static final String[] IDS_LIST = new String[]{"id", "sub", "email"};
    public static final String[] NAME_LIST = new String[]{"name"};
    public static final String[] EMAIL_LIST = new String[]{"email"};


    private final PluginDescriptor pluginDescriptor;
    private final ServerPrincipalFactory principalFactory;
    private final OAuthClient authClient;

    public OAuthAuthenticationScheme(@NotNull final LoginConfiguration loginConfiguration,
                                     @NotNull final PluginDescriptor pluginDescriptor,
                                     @NotNull final ServerPrincipalFactory principalFactory,
                                     @NotNull final OAuthClient authClient) {
        this.pluginDescriptor = pluginDescriptor;
        this.principalFactory = principalFactory;
        this.authClient = authClient;
        loginConfiguration.registerAuthModuleType(this);
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

        if (Strings.isNullOrEmpty(code) || Strings.isNullOrEmpty(state))
            return HttpAuthenticationResult.notApplicable();

        LOG.debug(String.format("oAuth response with code '%s' & state '%s'", code, state));

        if (!state.equals(SessionUtil.getSessionId(request)))
            return sendBadRequest(response, "Marked request as unauthenticated since retrieved 'state' doesn't correspond to current TeamCity session.");

        String token = authClient.getAccessToken(code);
        if (token == null) {
            return sendBadRequest(response, String.format("Marked request as unauthenticated since failed to fetch token for code '%s' and state '%s'.", code, state));
        }

        Map userData = authClient.getUserData(token);
        String userId = getId(userData);
        String userName = getName(userData);
        String userEmail = getEmail(userData);

        if ( userId == null || userName == null || userEmail == null) {
            return sendBadRequest(response, "Marked request as unauthenticated since user endpoint does not return any login id");
        }

        final ServerPrincipal principal = principalFactory.getServerPrincipal(userId, userName, userEmail, schemeProperties);
        if( principal == null ) {
            return sendBadRequest(response, "Marked request as unauthenticated since a user does not exist or failed to be created.");
        }

        LOG.debug("Request authenticated. Determined user " + principal.getName());
        return HttpAuthenticationResult.authenticated(principal, true);
    }

    @Nullable
    private String getId(Map userData) {
        if (userData == null) {
            return null;
        }

        for (String key : IDS_LIST) {
            String userId = (String) userData.get(key);
            if (userId != null) {
                return userId;
            }
        }
        return null;
    }

    @Nullable
    private String getEmail(Map userData) {
        if (userData == null) {
            return null;
        }

        for (String key : EMAIL_LIST) {
            String userEmail = (String) userData.get(key);
            if (userEmail != null) {
                return userEmail;
            }
        }
        return null;
    }

    @Nullable
    private String getName(Map userData) {
        if (userData == null) {
            return null;
        }

        for (String key : NAME_LIST) {
            String userName = (String) userData.get(key);
            if (userName != null) {
                return userName;
            }
        }
        return null;
    }

    private HttpAuthenticationResult sendBadRequest(HttpServletResponse response, String reason) throws IOException {
        LOG.warn(reason);
        response.sendError(HttpStatus.BAD_REQUEST.value(), reason);
        return HttpAuthenticationResult.unauthenticated();
    }
}
