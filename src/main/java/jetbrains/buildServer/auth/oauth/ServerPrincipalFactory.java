package jetbrains.buildServer.auth.oauth;

import jetbrains.buildServer.serverSide.auth.ServerPrincipal;
import jetbrains.buildServer.users.InvalidUsernameException;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class ServerPrincipalFactory {

    private static final Logger LOG = Logger.getLogger(ServerPrincipalFactory.class);


    @NotNull
    private final UserModel userModel;

    public ServerPrincipalFactory(@NotNull UserModel userModel) {
        this.userModel = userModel;
    }

    @NotNull
    public ServerPrincipal getServerPrincipal(@NotNull final OAuthUser user) {
        ServerPrincipal existingPrincipal = findExistingPrincipal(user.getId());
        if (existingPrincipal != null) {
            LOG.info("Use existing user: " + user.getId());
            return existingPrincipal;
        }
        LOG.info("Creating user: " + user);
        SUser created = userModel.createUserAccount(PluginConstants.OAUTH_AUTH_SCHEME_NAME, user.getId());
        created.setUserProperty(PluginConstants.ID_USER_PROPERTY_KEY, user.getId());
        created.updateUserAccount(user.getId(), user.getName(), user.getEmail());
        return new ServerPrincipal(PluginConstants.OAUTH_AUTH_SCHEME_NAME, user.getId());
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
