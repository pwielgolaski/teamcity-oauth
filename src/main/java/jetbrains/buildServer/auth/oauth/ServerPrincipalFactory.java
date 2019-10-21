package jetbrains.buildServer.auth.oauth;

import jetbrains.buildServer.groups.SUserGroup;
import jetbrains.buildServer.groups.UserGroupManager;
import jetbrains.buildServer.serverSide.auth.ServerPrincipal;
import jetbrains.buildServer.users.InvalidUsernameException;
import jetbrains.buildServer.users.SUser;
import jetbrains.buildServer.users.UserModel;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ServerPrincipalFactory {

    private static final Logger LOG = Logger.getLogger(ServerPrincipalFactory.class);

    @NotNull
    private final UserModel userModel;

    @NotNull
    private final UserGroupManager userGroupManager;

    @NotNull
    private final AuthenticationSchemeProperties properties;

    public ServerPrincipalFactory(@NotNull UserModel userModel, @NotNull UserGroupManager userGroupManager,
            @NotNull AuthenticationSchemeProperties properties) {
        this.userModel = userModel;
        this.userGroupManager = userGroupManager;
        this.properties = properties;
    }

    @NotNull
    public Optional<ServerPrincipal> getServerPrincipal(@NotNull final OAuthUser user,
            boolean allowCreatingNewUsersByLogin) {
        Optional<SUser> existingUserOptional = findExistingUser(user.getId());
        boolean syncGroups = properties.isSyncGroups();
        List<String> groupsInToken = user.getGroups();
        if (existingUserOptional.isPresent()) {
            LOG.info("Use existing user: " + user.getId());
            SUser existingUser = existingUserOptional.get();
            if (syncGroups) {
                //user group or all user groups?
                List<String> existingUserGroups =
                        existingUser.getUserGroups().stream().map(userGroup -> userGroup.getName())
                                .collect(Collectors.toList());
                List<String> additionalGroupsInToken = obtainGroupsDelta(groupsInToken, existingUserGroups);
                addUserToGroups(existingUser, applyGroupWhitelist(additionalGroupsInToken));

                List<String> groupsNotPresentInToken = obtainGroupsDelta(existingUserGroups, groupsInToken);
                removeUserFromGroups(existingUser, applyGroupWhitelist(groupsNotPresentInToken));
            }
            return Optional.of(new ServerPrincipal(PluginConstants.OAUTH_AUTH_SCHEME_NAME, existingUser.getUsername()));
        } else if (allowCreatingNewUsersByLogin) {
            LOG.info("Creating user: " + user);
            SUser created = userModel.createUserAccount(PluginConstants.OAUTH_AUTH_SCHEME_NAME, user.getId());
            if (syncGroups) {
                addUserToGroups(created, applyGroupWhitelist(groupsInToken));
            }
            created.setUserProperty(PluginConstants.ID_USER_PROPERTY_KEY, user.getId());
            created.updateUserAccount(user.getId(), user.getName(), user.getEmail());
            return Optional.of(new ServerPrincipal(PluginConstants.OAUTH_AUTH_SCHEME_NAME, user.getId()));
        } else {
            LOG.info("User: " + user + " could not be found and allowCreatingNewUsersByLogin is disabled");
            return Optional.empty();
        }
    }

    private void removeUserFromGroups(SUser existingUser, List<String> groupsToDelete) {
        for (String group : groupsToDelete) {
            SUserGroup userGroup = userGroupManager.findUserGroupByName(group);
            if (userGroup != null) {
                userGroup.removeUser(existingUser);
            }
        }
    }

    private List<String> applyGroupWhitelist(List<String> groups) {
        List<String> finalGroupList = new ArrayList<>();
        Set<String> whitelistedGroups = properties.getWhitelistedGroups();
        if (whitelistedGroups != null) {
            for (String group : groups) {
                for (String whitelistedGroup : whitelistedGroups) {
                    if (group.startsWith(whitelistedGroup)) {
                        finalGroupList.add(group);
                        break;
                    }
                }
            }
        }
        return finalGroupList;
    }

    private void addUserToGroups(SUser created, List<String> finalGroupList) {
        for (String group : finalGroupList) {
            SUserGroup userGroup = userGroupManager.findUserGroupByName(group);
            if (userGroup != null) {
                userGroup.addUser(created);
            }
        }
    }

    @NotNull
    private Optional<SUser> findExistingUser(@NotNull final String userName) {
        try {
            final SUser user = userModel.findUserByUsername(userName, PluginConstants.ID_USER_PROPERTY_KEY);
            return Optional.ofNullable(user);
        } catch (InvalidUsernameException e) {
            // ignore it
            return Optional.empty();
        }
    }

    private List<String> obtainGroupsDelta(List<String> minuend, List<String> subtrahend) {
        return minuend.stream()
                .filter(filterOutEqualValuesCaseInsensitive(subtrahend))
                .collect(Collectors.toList());
    }

    private Predicate<String> filterOutEqualValuesCaseInsensitive(Collection<String> valuesToExclude) {
        return valueUnderTest -> valuesToExclude.stream().map(String::toLowerCase)
                .noneMatch(s -> s.equals(valueUnderTest.toLowerCase()));
    }
}
