package jetbrains.buildServer.auth.oauth

import jetbrains.buildServer.groups.SUserGroup
import jetbrains.buildServer.groups.UserGroup
import jetbrains.buildServer.groups.UserGroupManager
import jetbrains.buildServer.serverSide.auth.ServerPrincipal
import jetbrains.buildServer.users.InvalidUsernameException
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.users.UserModel
import spock.lang.Specification

class ServerPrincipalFactoryTest extends Specification {

    UserModel userModel = Mock()
    UserGroupManager userGroupManager = Mock()
    SUserGroup sUserGroup = Mock()
    UserGroup userGroup = Mock()
    SUser teamcityUser = Mock()
    AuthenticationSchemeProperties properties = Mock()
    ServerPrincipalFactory principalFactory

    def setup() {
        principalFactory = new ServerPrincipalFactory(userModel, userGroupManager, properties)
    }

    def "read user from model"() {
        given:
        def user = new OAuthUser("testUser")
        userModel.findUserByUsername(_, _) >> this.teamcityUser
        this.teamcityUser.getUsername() >> user.id
        this.teamcityUser.getUserGroups() >> Arrays.asList(userGroup)
        userGroup.getName() >> "dev-team1"
        when:
        ServerPrincipal principal = principalFactory.getServerPrincipal(user, true).get()
        then:
        principal != null
        principal.name == user.id
        principal.realm == PluginConstants.OAUTH_AUTH_SCHEME_NAME
    }

    def "read user from model with existing groups add user group membership"() {
        given:
        def user = new OAuthUser("testUser", null, null, Arrays.asList("dev-team1", "dev-team2"))
        userModel.findUserByUsername(_, _) >> this.teamcityUser
        this.teamcityUser.getUsername() >> user.id
        this.teamcityUser.getUserGroups() >> Arrays.asList(userGroup)
        properties.isSyncGroups() >> true
        userGroup.getName() >> "dev-team2"
        properties.getWhitelistedGroups() >> Arrays.asList("dev")
        userGroupManager.findUserGroupByName("dev-team1") >> sUserGroup
        when:
        ServerPrincipal principal = principalFactory.getServerPrincipal(user, true).get()
        then:
        1 * sUserGroup.addUser(teamcityUser)
        principal != null
        principal.name == user.id
        principal.realm == PluginConstants.OAUTH_AUTH_SCHEME_NAME
    }

    def "read user from model with existing groups remove user membership"() {
        given:
        def user = new OAuthUser("testUser", null, null, Arrays.asList())
        userModel.findUserByUsername(_, _) >> this.teamcityUser
        this.teamcityUser.getUsername() >> user.id
        this.teamcityUser.getUserGroups() >> Arrays.asList(userGroup)
        properties.isSyncGroups() >> true
        userGroup.getName() >> "dev-team1"
        properties.getWhitelistedGroups() >> Arrays.asList("dev")
        userGroupManager.findUserGroupByName("dev-team1") >> sUserGroup
        when:
        ServerPrincipal principal = principalFactory.getServerPrincipal(user, true).get()
        then:
        1 * sUserGroup.removeUser(teamcityUser)
        principal != null
        principal.name == user.id
        principal.realm == PluginConstants.OAUTH_AUTH_SCHEME_NAME
    }

    /*
    before login, user in TC: dev-team1, dev-team2, dev-team3, SuperTCAdmin
    whitelist: dev-team1, dev-team2, dev-team3, dev-team5
    token: dev-team2, dev-team3, dev-team4, dev-team5

    after login, user in TC: dev-team2, dev-team3, dev-team5, SuperTCAdmin
    */

    def "read user from model with existing groups add and remove user memberships"() {
        given:
        def user = new OAuthUser("testUser", null, null,
                Arrays.asList("dev-team2", "dev-team3", "dev-team4", "dev-team5"))
        userModel.findUserByUsername(_, _) >> this.teamcityUser
        this.teamcityUser.getUsername() >> user.id
        UserGroup userGroup2 = Mock()
        UserGroup userGroup3 = Mock()
        UserGroup userGroupSuperTCAdmin = Mock()
        this.teamcityUser.getUserGroups() >> Arrays.asList(userGroup, userGroup2, userGroup3, userGroupSuperTCAdmin)
        properties.isSyncGroups() >> true
        userGroup.getName() >> "dev-team1"
        userGroup2.getName() >> "dev-team2"
        userGroup3.getName() >> "dev-team3"
        userGroupSuperTCAdmin.getName() >> "SuperTCAdmin"
        properties.getWhitelistedGroups() >> Arrays.asList("dev-team1", "dev-team2", "dev-team3", "dev-team5")
        SUserGroup sUserGroup2 = Mock()
        SUserGroup sUserGroup3 = Mock()
        SUserGroup sUserGroup5 = Mock()
        SUserGroup sUserGroupSuperTCAdmin = Mock()
        userGroupManager.findUserGroupByName("dev-team1") >> sUserGroup
        userGroupManager.findUserGroupByName("dev-team2") >> sUserGroup2
        userGroupManager.findUserGroupByName("dev-team3") >> sUserGroup3
        userGroupManager.findUserGroupByName("dev-team5") >> sUserGroup5
        userGroupManager.findUserGroupByName("SuperTCAdmin") >> sUserGroupSuperTCAdmin
        when:
        ServerPrincipal principal = principalFactory.getServerPrincipal(user, true).get()
        then:
        1 * sUserGroup.removeUser(teamcityUser)
        1 * sUserGroup5.addUser(teamcityUser)
        0 * sUserGroup2.addUser(teamcityUser)
        0 * sUserGroup2.removeUser(teamcityUser)
        0 * sUserGroup3.addUser(teamcityUser)
        0 * sUserGroup3.removeUser(teamcityUser)
        0 * sUserGroupSuperTCAdmin.addUser(teamcityUser)
        0 * sUserGroupSuperTCAdmin.removeUser(teamcityUser)
        principal != null
        principal.name == user.id
        principal.realm == PluginConstants.OAUTH_AUTH_SCHEME_NAME
    }

    def "create user if model reports null"() {
        given:
        def user = new OAuthUser("testUser")
        userModel.findUserByUsername(_, _) >> null
        userModel.createUserAccount(PluginConstants.OAUTH_AUTH_SCHEME_NAME, user.id) >> teamcityUser
        when:
        ServerPrincipal principal = principalFactory.getServerPrincipal(user, true).get()
        then:
        principal != null
        principal.name == user.id
        principal.realm == PluginConstants.OAUTH_AUTH_SCHEME_NAME
    }

    def "return empty user if model reports null"() {
        given:
        def user = new OAuthUser("testUser")
        userModel.findUserByUsername(_, _) >> null
        when:
        Optional<ServerPrincipal> principal = principalFactory.getServerPrincipal(user, false)
        then:
        principal.isPresent() == false
    }

    def "create user if model reports exception"() {
        given:
        def user = new OAuthUser("testUser")
        userModel.findUserByUsername(_, _) >> { throw new InvalidUsernameException("mocked reason") }
        userModel.createUserAccount(PluginConstants.OAUTH_AUTH_SCHEME_NAME, user.id) >> teamcityUser
        when:
        ServerPrincipal principal = principalFactory.getServerPrincipal(user, true).get()
        then:
        principal != null
        principal.name == user.id
        principal.realm == PluginConstants.OAUTH_AUTH_SCHEME_NAME
    }

    def "create user from model without sync of groups"() {
        given:
        def user = new OAuthUser("testUser", null, null, Arrays.asList("dev-team1", "dev-team2"))
        userModel.findUserByUsername(_, _) >> null
        userModel.createUserAccount(PluginConstants.OAUTH_AUTH_SCHEME_NAME, user.id) >> teamcityUser
        properties.isSyncGroups() >> false
        when:
        ServerPrincipal principal = principalFactory.getServerPrincipal(user, true).get()
        then:
        0 * userGroupManager.findUserGroupByName("dev-team1")
        0 * userGroupManager.findUserGroupByName("dev-team2")
        principal != null
        principal.name == user.id
        principal.realm == PluginConstants.OAUTH_AUTH_SCHEME_NAME
    }

    def "create user from model with whitelisted groups"() {
        given:
        def user = new OAuthUser("testUser", null, null, Arrays.asList("dev-team1", "dev-team2"))
        userModel.findUserByUsername(_, _) >> null
        userModel.createUserAccount(PluginConstants.OAUTH_AUTH_SCHEME_NAME, user.id) >> teamcityUser
        properties.isSyncGroups() >> true
        properties.getWhitelistedGroups() >> Arrays.asList("dev")
        when:
        ServerPrincipal principal = principalFactory.getServerPrincipal(user, true).get()
        then:
        1 * userGroupManager.findUserGroupByName("dev-team1")
        1 * userGroupManager.findUserGroupByName("dev-team2")
        principal != null
        principal.name == user.id
        principal.realm == PluginConstants.OAUTH_AUTH_SCHEME_NAME
    }

    def "create user from model with whitelisted one group"() {
        given:
        def user = new OAuthUser("testUser", null, null, Arrays.asList("dev-team1", "dev-team2"))
        userModel.findUserByUsername(_, _) >> null
        userModel.createUserAccount(PluginConstants.OAUTH_AUTH_SCHEME_NAME, user.id) >> teamcityUser
        properties.isSyncGroups() >> true
        properties.getWhitelistedGroups() >> Arrays.asList("dev-team1")
        when:
        ServerPrincipal principal = principalFactory.getServerPrincipal(user, true).get()
        then:
        1 * userGroupManager.findUserGroupByName("dev-team1")
        0 * userGroupManager.findUserGroupByName("dev-team2")
        principal != null
        principal.name == user.id
        principal.realm == PluginConstants.OAUTH_AUTH_SCHEME_NAME
    }

    def "create user from model with whitelisted groups and only one is existing in TC"() {
        given:
        def user = new OAuthUser("testUser", null, null, Arrays.asList("dev-team1", "dev-team2"))
        userModel.findUserByUsername(_, _) >> null
        userModel.createUserAccount(PluginConstants.OAUTH_AUTH_SCHEME_NAME, user.id) >> teamcityUser
        properties.isSyncGroups() >> true
        properties.getWhitelistedGroups() >> Arrays.asList("dev")
        userGroupManager.findUserGroupByName("dev-team2") >> sUserGroup
        when:
        ServerPrincipal principal = principalFactory.getServerPrincipal(user, true).get()
        then:
        1 * sUserGroup.addUser(teamcityUser)
        principal != null
        principal.name == user.id
        principal.realm == PluginConstants.OAUTH_AUTH_SCHEME_NAME
    }

    def "create user from model with no whitelisted groups and one is existing in TC"() {
        given:
        def user = new OAuthUser("testUser", null, null, Arrays.asList("dev-team1", "dev-team2"))
        userModel.findUserByUsername(_, _) >> null
        userModel.createUserAccount(PluginConstants.OAUTH_AUTH_SCHEME_NAME, user.id) >> teamcityUser
        properties.isSyncGroups() >> true
        properties.getWhitelistedGroups() >> null
        userGroupManager.findUserGroupByName("dev-team2") >> sUserGroup
        when:
        ServerPrincipal principal = principalFactory.getServerPrincipal(user, true).get()
        then:
        0 * sUserGroup.addUser(teamcityUser)
        principal != null
        principal.name == user.id
        principal.realm == PluginConstants.OAUTH_AUTH_SCHEME_NAME
    }}
