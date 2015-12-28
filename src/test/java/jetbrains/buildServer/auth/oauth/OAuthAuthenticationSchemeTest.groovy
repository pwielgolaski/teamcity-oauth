package jetbrains.buildServer.auth.oauth

import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationResult
import jetbrains.buildServer.serverSide.auth.LoginConfiguration
import jetbrains.buildServer.serverSide.auth.ServerPrincipal
import jetbrains.buildServer.web.openapi.PluginDescriptor
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class OAuthAuthenticationSchemeTest extends Specification {

    OAuthClient client = Mock()
    HttpServletResponse res = Mock()
    OAuthAuthenticationScheme scheme

    def setup() {
        LoginConfiguration loginConfiguration = Mock()
        PluginDescriptor pluginDescriptor = Mock()
        ServerPrincipalFactory principalFactory = Mock() {
            getServerPrincipal(_, _) >> { String userName, _ -> new ServerPrincipal(PluginConstants.OAUTH_AUTH_SCHEME_NAME, userName) }
        }
        scheme = new OAuthAuthenticationScheme(loginConfiguration, pluginDescriptor, principalFactory, client)
    }


    @Unroll
    def "handle as not applicable if state or code is missing"() {
        given:
        HttpServletRequest req = Mock() {
            getParameter(OAuthAuthenticationScheme.CODE) >> code
            getParameter(OAuthAuthenticationScheme.STATE) >> state
        }
        when:
        HttpAuthenticationResult result = scheme.processAuthenticationRequest(req, res, [:])
        then:
        result.type == type

        where:
        code   | state   || type
        null   | null    || HttpAuthenticationResult.Type.NOT_APPLICABLE
        "code" | null    || HttpAuthenticationResult.Type.NOT_APPLICABLE
        null   | "state" || HttpAuthenticationResult.Type.NOT_APPLICABLE
        ""     | ""      || HttpAuthenticationResult.Type.NOT_APPLICABLE
    }

    def "handle as unauthenticated if state does not match"() {
        given:
        HttpServletRequest req = Mock() {
            getParameter(OAuthAuthenticationScheme.CODE) >> "code"
            getParameter(OAuthAuthenticationScheme.STATE) >> "state"
            getRequestedSessionId() >> "id"
        }
        when:
        HttpAuthenticationResult result = scheme.processAuthenticationRequest(req, res, [:])
        then:
        result.type == HttpAuthenticationResult.Type.UNAUTHENTICATED
        1 * res.sendError(400, "Marked request as unauthenticated since retrieved 'state' doesn't correspond to current TeamCity session.")
    }


    def "handle as unauthenticated if token is not acquired"() {
        given:
        HttpServletRequest req = Mock() {
            getParameter(OAuthAuthenticationScheme.CODE) >> "code"
            getParameter(OAuthAuthenticationScheme.STATE) >> "state"
            getRequestedSessionId() >> "state"
        }
        when:
        HttpAuthenticationResult result = scheme.processAuthenticationRequest(req, res, [:])
        then:
        result.type == HttpAuthenticationResult.Type.UNAUTHENTICATED
        1 * res.sendError(400, "Marked request as unauthenticated since failed to fetch token for code 'code' and state 'state'.")
    }

    def "handle as unauthenticated if user data are not valid"() {
        given:
        HttpServletRequest req = Mock() {
            getParameter(OAuthAuthenticationScheme.CODE) >> "code"
            getParameter(OAuthAuthenticationScheme.STATE) >> "state"
            getRequestedSessionId() >> "state"
        }
        client.getAccessToken("code") >> "token"
        when:
        HttpAuthenticationResult result = scheme.processAuthenticationRequest(req, res, [:])
        then:
        result.type == HttpAuthenticationResult.Type.UNAUTHENTICATED
        1 * res.sendError(400, "Marked request as unauthenticated since user endpoint does not return 'name' field")
    }

    def "authenticate user"() {
        given:
        HttpServletRequest req = Mock() {
            getParameter(OAuthAuthenticationScheme.CODE) >> "code"
            getParameter(OAuthAuthenticationScheme.STATE) >> "state"
            getRequestedSessionId() >> "state"
        }
        client.getAccessToken("code") >> "token"
        client.getUserData("token") >> [name: "testUser"]
        when:
        HttpAuthenticationResult result = scheme.processAuthenticationRequest(req, res, [:])
        then:
        result.type == HttpAuthenticationResult.Type.AUTHENTICATED
        result.principal.name == "testUser"
    }

}
