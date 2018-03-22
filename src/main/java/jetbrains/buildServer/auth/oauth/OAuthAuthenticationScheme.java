package jetbrains.buildServer.auth.oauth;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationResult;
import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationSchemeAdapter;
import jetbrains.buildServer.controllers.interceptors.auth.util.HttpAuthUtil;
import jetbrains.buildServer.serverSide.auth.AuthModuleUtil;
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
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;


public class OAuthAuthenticationScheme extends HttpAuthenticationSchemeAdapter {

    private static final Logger LOG = Logger.getLogger(OAuthAuthenticationScheme.class);
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

        LOG.debug(String.format("oAuth response with code '%s' & state '%s'", code, state));

        if (!state.equals(SessionUtil.getSessionId(request)))
            return sendUnauthorizedRequest(request, response, "Unauthenticated since retrieved 'state' doesn't correspond to current TeamCity session.");

        String token = authClient.getAccessToken(code);
        if (token == null) {
            return sendUnauthorizedRequest(request, response, String.format("Unauthenticated since failed to fetch token for code '%s' and state '%s'.", code, state));
        }

        OAuthUser user = authClient.getUserData(token);
        if (user.getId() == null) {
            return sendUnauthorizedRequest(request, response, "Unauthenticated since user endpoint does not return any login id");
        }
        String emailDomain = properties.getEmailDomain();
        if (!(emailDomain == null || emailDomain.isEmpty())) {
            if (!emailDomain.startsWith("@")) {
                emailDomain = "@" + emailDomain;
            }
            if (user.getEmail() == null || !user.getEmail().endsWith(emailDomain)) {
                return sendUnauthorizedRequest(request, response, "Unauthenticated since user email is not " + emailDomain);
            }
        }
        // Check the organizations that the user belongs to for Github Oauth
        if (GithubUser.class.isInstance(user) ) {
          String orgs = properties.getOrganizations();
          if(!(orgs == null || orgs.isEmpty())) {
            String[] configuredOrganizations = orgs.split(",");
            Arrays.parallelSetAll(configuredOrganizations, (i) -> configuredOrganizations[i].trim());
            String[] userOrganizations = ((GithubUser) user).getOrganizations();

            Set<String> userOrganizationsSet = new HashSet<String>(Arrays.asList(userOrganizations));
            Set<String> configuredOrganizationsSet = new HashSet<String>(Arrays.asList(configuredOrganizations));
            Set<String> matchedOrganizationsSet = new HashSet<String>();

            for (String organization:configuredOrganizationsSet) {
              if (userOrganizationsSet.contains(organization)) {
                matchedOrganizationsSet.add(organization);
              }
            }
            if(matchedOrganizationsSet == null || matchedOrganizationsSet.size() == 0) {
              return sendUnauthorizedRequest(request, response, "User's organization does not match with the ones specified in the Oauth settings");
            }
          }
        }
        boolean allowCreatingNewUsersByLogin = AuthModuleUtil.allowCreatingNewUsersByLogin(schemeProperties, DEFAULT_ALLOW_CREATING_NEW_USERS_BY_LOGIN);
        final Optional<ServerPrincipal> principal = principalFactory.getServerPrincipal(user, allowCreatingNewUsersByLogin);

        if (principal.isPresent()) {
            LOG.debug("Request authenticated. Determined user " + principal.get().getName());
            return HttpAuthenticationResult.authenticated(principal.get(), true)
                    .withRedirect("/");
        } else {
            return sendUnauthorizedRequest(request, response, "Unauthenticated since user could not be found or created.");
        }
    }

    private HttpAuthenticationResult sendUnauthorizedRequest(HttpServletRequest request, HttpServletResponse response, String reason) throws IOException {
        LOG.warn(reason);
        HttpAuthUtil.setUnauthenticatedReason(request, reason);
        response.sendError(HttpStatus.UNAUTHORIZED.value(), reason);
        return HttpAuthenticationResult.unauthenticated();
    }
}
