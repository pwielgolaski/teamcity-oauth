package jetbrains.buildServer.auth.oauth

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.json.simple.JSONValue
import org.springframework.http.MediaType
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Shared
import spock.lang.Specification

class OAuthClientTest extends Specification {

    public static String AUTHORIZE_URL = ""
    public static String TOKEN_URL = ""
    public static String USER_URL = ""
    public static final String ROOT_URL = "http://localhost"
    public static final String CLIENT_ID = "myclient"
    public static final String CLIENT_SECRET = "mysecret"
    @Shared
    MockWebServer server = new MockWebServer()

    OAuthClient client;
    def schemeProperties;

    def setupSpec() {
        server.start()
        AUTHORIZE_URL = server.url("/auth")
        TOKEN_URL = server.url("/token")
        USER_URL = server.url("/user")
    }

    def cleanupSpec() {
        server.shutdown()
    }

    def setup() {
        schemeProperties = Stub(AuthenticationSchemeProperties) {
            getRootUrl() >> ROOT_URL
            getAuthorizeEndpoint() >> AUTHORIZE_URL
            getTokenEndpoint() >> TOKEN_URL
            getUserEndpoint() >> USER_URL
            getClientId() >> CLIENT_ID
            getClientSecret() >> CLIENT_SECRET
            isAllowInsecureHttps() >> true
        }
        client = new OAuthClient(schemeProperties)
    }


    def "should generate redirect url"() {
        setup:
        schemeProperties.getScope() >> scope
        def state = "state"
        when:
        def redirectUrl = client.getRedirectUrl(state);
        def uri = UriComponentsBuilder.fromHttpUrl(redirectUrl).build();
        then:
        uri.host == 'localhost'
        uri.path == '/auth'
        def query = uri.queryParams.toSingleValueMap()
        query['response_type'] == 'code'
        query['client_id'] == CLIENT_ID
        query['state'] == state
        query['scope'] == expectedScope
        query['redirect_uri'] == ROOT_URL
        where:
        scope           || expectedScope
        ""              || null
        "scope1"        || "scope1"
        "scope1 scope2" || "scope1%20scope2"
    }

    def "should fetch token data"() {
        setup:
        def code = 'my_code'
        def token = 'my_token'
        server.enqueue(new MockResponse().setBody(JSONValue.toJSONString([access_token: token])))
        expect:
        client.getAccessToken(code) == token
        def req = server.takeRequest()
        req.method == 'POST'
        req.path == '/token'
        req.getHeader('Content-Type') == MediaType.APPLICATION_FORM_URLENCODED_VALUE
        req.getHeader('Accept') == MediaType.APPLICATION_JSON_VALUE
        req.getBody().readUtf8() == "grant_type=authorization_code&code=my_code&redirect_uri=http%3A%2F%2Flocalhost&client_id=myclient&client_secret=mysecret"
    }

    def "should fetch user data"() {
        setup:
        def token = 'test_token'
        server.enqueue(new MockResponse().setBody(JSONValue.toJSONString([id: 'user', name: 'userName', email: 'email', groups: ['dev-']])))
        expect:
        client.getUserData(token) == new OAuthUser('user', 'userName', 'email', Arrays.asList("dev-"))
        def req = server.takeRequest()
        req.method == 'GET'
        req.path == '/user'
        req.headers.get('Authorization') == 'Bearer ' + token
    }
}
