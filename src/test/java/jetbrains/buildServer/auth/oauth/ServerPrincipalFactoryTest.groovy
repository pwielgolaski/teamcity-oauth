package jetbrains.buildServer.auth.oauth

import jetbrains.buildServer.serverSide.auth.ServerPrincipal
import jetbrains.buildServer.users.InvalidUsernameException
import jetbrains.buildServer.users.SUser
import jetbrains.buildServer.users.UserModel
import spock.lang.Specification

class ServerPrincipalFactoryTest extends Specification {

    UserModel userModel = Mock()
    SUser user = Mock()
    ServerPrincipalFactory principalFactory;

    def setup() {
        principalFactory = new ServerPrincipalFactory(userModel)
    }

    def "read user from model"() {
        given:
        def userName = "testUser"
        user.getUsername() >> userName
        userModel.findUserByUsername(_,_) >> user
        when:
        ServerPrincipal principal = principalFactory.getServerPrincipal(userName, [:])
        then:
        principal != null
        principal.name == userName
        principal.realm == PluginConstants.OAUTH_AUTH_SCHEME_NAME
        !principal.creatingNewUserAllowed
    }


    def "create user if model reports null"() {
        given:
        def userName = "testUser"
        userModel.findUserByUsername(_,_) >> null
        when:
        ServerPrincipal principal = principalFactory.getServerPrincipal(userName, [:])
        then:
        principal != null
        principal.name == userName
        principal.realm == PluginConstants.OAUTH_AUTH_SCHEME_NAME
        principal.creatingNewUserAllowed
    }

    def "create user if model reports exception"() {
        given:
        def userName = "testUser"
        userModel.findUserByUsername(_,_) >> { throw new InvalidUsernameException("mocked reason") }
        when:
        ServerPrincipal principal = principalFactory.getServerPrincipal(userName, [:])
        then:
        principal != null
        principal.name == userName
        principal.realm == PluginConstants.OAUTH_AUTH_SCHEME_NAME
        principal.creatingNewUserAllowed
    }
}
