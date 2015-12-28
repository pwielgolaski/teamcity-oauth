package jetbrains.buildServer.auth.oauth;

import com.google.common.collect.ImmutableMap;
import jetbrains.buildServer.serverSide.auth.AuthModuleUtil;
import jetbrains.buildServer.serverSide.auth.ServerPrincipal;
import jetbrains.buildServer.users.InvalidUsernameException;
import jetbrains.buildServer.users.PropertyKey;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;


public class ServerPrincipalFactory {

    private static final Logger LOG = Logger.getLogger(ServerPrincipalFactory.class);

    private static final boolean DEFAULT_ALLOW_CREATING_NEW_USERS_BY_LOGIN = true;

    @NotNull
    private final UserModel userModel;

    public ServerPrincipalFactory(@NotNull UserModel userModel) {
        this.userModel = userModel;
    }

    @NotNull
    public ServerPrincipal getServerPrincipal(@NotNull final String userName, @NotNull Map<String, String> schemeProperties) {
        ServerPrincipal existingPrincipal = findExistingPrincipal(userName);
        if (existingPrincipal != null) {
            LOG.info("Use existing user: " + userName);
            return existingPrincipal;
        }

        boolean allowCreatingNewUsersByLogin = AuthModuleUtil.allowCreatingNewUsersByLogin(schemeProperties, DEFAULT_ALLOW_CREATING_NEW_USERS_BY_LOGIN);
        Map<PropertyKey, String> userProperties = ImmutableMap.<PropertyKey, String>of(PluginConstants.ID_USER_PROPERTY_KEY, userName);
        return new ServerPrincipal(PluginConstants.OAUTH_AUTH_SCHEME_NAME, userName, null, allowCreatingNewUsersByLogin, userProperties);
    }

    @Nullable
    private ServerPrincipal findExistingPrincipal(@NotNull final String userName) {
        try {
            final SUser user = userModel.findUserByUsername(userName, PluginConstants.ID_USER_PROPERTY_KEY);
            if (user != null) {
                return new ServerPrincipal(PluginConstants.OAUTH_AUTH_SCHEME_NAME, user.getUsername());
            }
        } catch (InvalidUsernameException e) {
            // ignore it
        }
        return null;
    }
}
