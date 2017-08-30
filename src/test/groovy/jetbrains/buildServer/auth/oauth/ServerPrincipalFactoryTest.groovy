package jetbrains.buildServer.auth.oauth

import jetbrains.buildServer.serverSide.auth.ServerPrincipal
import jetbrains.buildServer.users.InvalidUsernameException
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.users.UserModel
import spock.lang.Specification

class ServerPrincipalFactoryTest extends Specification {

    UserModel userModel = Mock()
    SUser teamcityUser = Mock()
    ServerPrincipalFactory principalFactory;

    def setup() {
        principalFactory = new ServerPrincipalFactory(userModel)
    }

    def "read user from model"() {
        given:
        def user = new OAuthUser("testUser")
        this.teamcityUser.getUsername() >> user.id
        userModel.findUserByUsername(_, _) >> this.teamcityUser
        when:
        ServerPrincipal principal = principalFactory.getServerPrincipal(user, true).get()
        then:
        principal != null
        principal.name == user.id
        principal.realm == PluginConstants.OAUTH_AUTH_SCHEME_NAME
    }


    def "create user if model reports null"() {
        given:
        def user = new OAuthUser("testUser")
        userModel.findUserByUsername(_,_) >> null
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
        userModel.findUserByUsername(_,_) >> null
        when:
        Optional<ServerPrincipal> principal = principalFactory.getServerPrincipal(user, false)
        then:
        principal.isPresent() == false
    }

    def "create user if model reports exception"() {
        given:
        def user = new OAuthUser("testUser")
        userModel.findUserByUsername(_,_) >> { throw new InvalidUsernameException("mocked reason") }
        userModel.createUserAccount(PluginConstants.OAUTH_AUTH_SCHEME_NAME, user.id) >> teamcityUser
        when:
        ServerPrincipal principal = principalFactory.getServerPrincipal(user, true).get()
        then:
        principal != null
        principal.name == user.id
        principal.realm == PluginConstants.OAUTH_AUTH_SCHEME_NAME
    }
}
