package jetbrains.buildServer.auth.oauth

import jetbrains.buildServer.controllers.interceptors.auth.HttpAuthenticationResult
import jetbrains.buildServer.serverSide.auth.ServerPrincipal
import jetbrains.buildServer.web.openapi.PluginDescriptor
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class OAuthAuthenticationSchemeTest extends Specification {

    OAuthClient client = Mock()
    HttpServletResponse res = Mock()
    AuthenticationSchemeProperties properties
    OAuthAuthenticationScheme scheme

    def setup() {
        PluginDescriptor pluginDescriptor = Mock()
        ServerPrincipalFactory principalFactory = Mock() {
            getServerPrincipal(_, _) >> { OAuthUser user, boolean allow ->
                if (allow)
                    Optional.of(new ServerPrincipal(PluginConstants.OAUTH_AUTH_SCHEME_NAME, user.id))
                else Optional.empty()
            }
        }
        properties = Mock()
        scheme = new OAuthAuthenticationScheme(pluginDescriptor, principalFactory, client, properties)
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
        1 * res.sendError(401, "Unauthenticated since retrieved 'state' doesn't correspond to current TeamCity session.")
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
        1 * res.sendError(401, "Unauthenticated since failed to fetch token for code 'code' and state 'state'.")
    }

    def "handle as unauthenticated if user data are not valid"() {
        given:
        HttpServletRequest req = Mock() {
            getParameter(OAuthAuthenticationScheme.CODE) >> "code"
            getParameter(OAuthAuthenticationScheme.STATE) >> "state"
            getRequestedSessionId() >> "state"
        }
        client.getAccessToken("code") >> "token"
        client.getUserData("token") >> new OAuthUser(null)
        when:
        HttpAuthenticationResult result = scheme.processAuthenticationRequest(req, res, [:])
        then:
        result.type == HttpAuthenticationResult.Type.UNAUTHENTICATED
        1 * res.sendError(401, "Unauthenticated since user endpoint does not return any login id")
    }

    def "authenticate user"() {
        given:
        HttpServletRequest req = Mock() {
            getParameter(OAuthAuthenticationScheme.CODE) >> "code"
            getParameter(OAuthAuthenticationScheme.STATE) >> "state"
            getRequestedSessionId() >> "state"
        }
        client.getAccessToken("code") >> "token"
        client.getUserData("token") >> new OAuthUser("testUser")
        when:
        HttpAuthenticationResult result = scheme.processAuthenticationRequest(req, res, [:])
        then:
        result.type == HttpAuthenticationResult.Type.AUTHENTICATED
        result.principal.name == "testUser"
    }

    def "don't authenticate user if allow to create new account is disabled"() {
        given:
        HttpServletRequest req = Mock() {
            getParameter(OAuthAuthenticationScheme.CODE) >> "code"
            getParameter(OAuthAuthenticationScheme.STATE) >> "state"
            getRequestedSessionId() >> "state"
        }
        client.getAccessToken("code") >> "token"
        client.getUserData("token") >> new OAuthUser("testUser")
        when:
        HttpAuthenticationResult result = scheme.processAuthenticationRequest(req, res, [allowCreatingNewUsersByLogin: false])
        then:
        result.type == HttpAuthenticationResult.Type.UNAUTHENTICATED
        1 * res.sendError(401, 'Unauthenticated since user could not be found or created.')
    }

    def "authenticate user with email matching domain"() {
        given:
        HttpServletRequest req = Mock() {
            getParameter(OAuthAuthenticationScheme.CODE) >> "code"
            getParameter(OAuthAuthenticationScheme.STATE) >> "state"
            getRequestedSessionId() >> "state"
        }
        client.getAccessToken("code") >> "token"
        client.getUserData("token") >> new OAuthUser("testUser", "Test User", "test@example.com", Arrays.asList("dev-"))
        properties.getEmailDomains() >> Arrays.asList("example.com")
        when:
        HttpAuthenticationResult result = scheme.processAuthenticationRequest(req, res, [:])
        then:
        result.type == HttpAuthenticationResult.Type.AUTHENTICATED
        result.principal.name == "testUser"
    }

    def "authenticate user with email matching domain list"() {
        given:
        HttpServletRequest req = Mock() {
            getParameter(OAuthAuthenticationScheme.CODE) >> "code"
            getParameter(OAuthAuthenticationScheme.STATE) >> "state"
            getRequestedSessionId() >> "state"
        }
        client.getAccessToken("code") >> "token"
        client.getUserData("token") >> new OAuthUser("testUser", "Test User", "test@example.com", Arrays.asList("dev-"))
        properties.getEmailDomains() >> Arrays.asList("example.com", "@example1.com")
        when:
        HttpAuthenticationResult result = scheme.processAuthenticationRequest(req, res, [:])
        then:
        result.type == HttpAuthenticationResult.Type.AUTHENTICATED
        result.principal.name == "testUser"
    }

    def "don't authenticate user with email that doesn't match domain"() {
        given:
        HttpServletRequest req = Mock() {
            getParameter(OAuthAuthenticationScheme.CODE) >> "code"
            getParameter(OAuthAuthenticationScheme.STATE) >> "state"
            getRequestedSessionId() >> "state"
        }
        client.getAccessToken("code") >> "token"
        client.getUserData("token") >> new OAuthUser("testUser", "Test User", "test@acme.com", Arrays.asList("dev-"))
        properties.getEmailDomains() >> Arrays.asList("example.com")
        when:
        HttpAuthenticationResult result = scheme.processAuthenticationRequest(req, res, [:])
        then:
        result.type == HttpAuthenticationResult.Type.UNAUTHENTICATED
        1 * res.sendError(401, 'Unauthenticated since user email is not in [example.com]')
    }

    def "don't authenticate user with email that doesn't match domain list"() {
        given:
        HttpServletRequest req = Mock() {
            getParameter(OAuthAuthenticationScheme.CODE) >> "code"
            getParameter(OAuthAuthenticationScheme.STATE) >> "state"
            getRequestedSessionId() >> "state"
        }
        client.getAccessToken("code") >> "token"
        client.getUserData("token") >> new OAuthUser("testUser", "Test User", "test@acme.com", Arrays.asList("dev-"))
        properties.getEmailDomains() >> Arrays.asList("example.com", "@example1.com")
        when:
        HttpAuthenticationResult result = scheme.processAuthenticationRequest(req, res, [:])
        then:
        result.type == HttpAuthenticationResult.Type.UNAUTHENTICATED
        1 * res.sendError(401, 'Unauthenticated since user email is not in [example.com, @example1.com]')
    }

    def "don't authenticate user without email when domain specified"() {
        given:
        HttpServletRequest req = Mock() {
            getParameter(OAuthAuthenticationScheme.CODE) >> "code"
            getParameter(OAuthAuthenticationScheme.STATE) >> "state"
            getRequestedSessionId() >> "state"
        }
        client.getAccessToken("code") >> "token"
        client.getUserData("token") >> new OAuthUser("testUser")
        properties.getEmailDomains() >> Arrays.asList("example.com")
        when:
        HttpAuthenticationResult result = scheme.processAuthenticationRequest(req, res, [:])
        then:
        result.type == HttpAuthenticationResult.Type.UNAUTHENTICATED
        1 * res.sendError(401, 'Unauthenticated since user email is not in [example.com]')
    }

    def "authenticate user when user's organization match the specified organization"() {
        given:
        HttpServletRequest req = Mock() {
            getParameter(OAuthAuthenticationScheme.CODE) >> "code"
            getParameter(OAuthAuthenticationScheme.STATE) >> "state"
            getRequestedSessionId() >> "state"
        }
        GithubUser githubUser = new GithubUser([id: "sample-test"], { '[ {"login": "test-org"} ]' })

        client.getUserData("token") >> githubUser
        client.getAccessToken("code") >> "token"
        properties.getOrganizations() >> "test-org,foo-org"

        when:
        HttpAuthenticationResult result = scheme.processAuthenticationRequest(req, res, [:])
        then:
        result.type == HttpAuthenticationResult.Type.AUTHENTICATED
    }

    def "authenticate user when no organization is specified in the settings"() {
        given:
        HttpServletRequest req = Mock() {
            getParameter(OAuthAuthenticationScheme.CODE) >> "code"
            getParameter(OAuthAuthenticationScheme.STATE) >> "state"
            getRequestedSessionId() >> "state"
        }
        GithubUser githubUser = new GithubUser([id: "sample-test"], { '[ {"login": "test-org"} ]' })

        client.getUserData("token") >> githubUser
        client.getAccessToken("code") >> "token"

        when:
        HttpAuthenticationResult result = scheme.processAuthenticationRequest(req, res, [:])
        then:
        result.type == HttpAuthenticationResult.Type.AUTHENTICATED
    }


    def "dont authenticate user when user's organization does not match the specified organization"() {
        given:
        HttpServletRequest req = Mock() {
            getParameter(OAuthAuthenticationScheme.CODE) >> "code"
            getParameter(OAuthAuthenticationScheme.STATE) >> "state"
            getRequestedSessionId() >> "state"
        }
        GithubUser githubUser = new GithubUser([id: "sample-test"], { '[ {"login": "test-org"} ]' })

        client.getUserData("token") >> githubUser
        client.getAccessToken("code") >> "token"
        properties.getOrganizations() >> "foo-org, bar-org"

        when:
        HttpAuthenticationResult result = scheme.processAuthenticationRequest(req, res, [:])
        then:
        result.type == HttpAuthenticationResult.Type.UNAUTHENTICATED
        1 * res.sendError(401, "User's organization does not match with the ones specified in the oAuth settings")
    }

}
