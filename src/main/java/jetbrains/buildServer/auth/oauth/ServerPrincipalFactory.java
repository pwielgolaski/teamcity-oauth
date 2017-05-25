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
    public ServerPrincipal getServerPrincipal(@NotNull final String userId, @NotNull final String userName, @NotNull final String userEmail, @NotNull Map<String, String> schemeProperties) {
        boolean createUser = AuthModuleUtil.allowCreatingNewUsersByLogin(schemeProperties, DEFAULT_ALLOW_CREATING_NEW_USERS_BY_LOGIN);

        ServerPrincipal existingPrincipal = findExistingPrincipal(userId);
        if (existingPrincipal != null) {
            LOG.info("Use existing user: " + userName);
            return existingPrincipal;
        }

        if (createUser) {
            existingPrincipal = createUser(userId, userName, userEmail);
        }


        return existingPrincipal;
    }

    @Nullable
    private ServerPrincipal findExistingPrincipal(@NotNull final String userId) {
        try {
            final SUser user = userModel.findUserByUsername(userId, PluginConstants.ID_USER_PROPERTY_KEY);
            if (user != null) {
                return new ServerPrincipal(PluginConstants.OAUTH_AUTH_SCHEME_NAME, user.getUsername());
            }
        } catch (InvalidUsernameException e) {
            // ignore it
        }
        return null;
    }

    @Nullable
    private ServerPrincipal createUser(@NotNull final String userId, @NotNull final String userName, final String userEmail) {
        final SUser user = userModel.createUserAccount(null, userEmail);
        user.updateUserAccount(userEmail, userName, userEmail);
        Map<PropertyKey, String> userProperties = ImmutableMap.<PropertyKey, String>of(PluginConstants.ID_USER_PROPERTY_KEY, userId);
        user.setUserProperties(userProperties);

        return new ServerPrincipal(PluginConstants.OAUTH_AUTH_SCHEME_NAME, userName, PluginConstants.ID_USER_PROPERTY_KEY, false, userProperties);
    }

}
