package jetbrains.buildServer.auth.oauth

import org.json.simple.JSONValue
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.web.util.UriComponentsBuilder
import spock.lang.Specification

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

class OAuthClientTest extends Specification {

    public static final String ROOT_URL = "http://localhost"
    public static final String AUTHORIZE_URL = "http://oauth/auth"
    public static final String TOKEN_URL = "http://oauth/token"
    public static final String USER_URL = "http://oauth/user"
    public static final String CLIENT_ID = "myclient"
    public static final String CLIENT_SECRET = "mysecret"
    public static final String SCOPE = "myscope"

    OAuthClient client;

    def setup() {
        def schemeProperties = Stub(AuthenticationSchemeProperties) {
            getRootUrl() >> ROOT_URL
            getAuthorizeEndpoint() >> AUTHORIZE_URL
            getTokenEndpoint() >> TOKEN_URL
            getUserEndpoint() >> USER_URL
            getClientId() >> CLIENT_ID
            getClientSecret() >> CLIENT_SECRET
            getScope() >> SCOPE
        }
        client = new OAuthClient(schemeProperties)
    }


    def "should generate redirect url"() {
        setup:
        def state = "state"
        when:
        def redirectUrl = client.getRedirectUrl(state);
        def uri = UriComponentsBuilder.fromHttpUrl(redirectUrl).build();
        then:
        uri.host == 'oauth'
        uri.path == '/auth'
        uri.queryParams.toSingleValueMap() == [response_type: 'code', client_id: CLIENT_ID, scope: SCOPE, state: state, redirect_uri: ROOT_URL]
    }


    def "should fetch token data"() {
        setup:
        def code = 'my_code'
        def token = 'my_token'
        def mockServer = MockRestServiceServer.createServer(client.restTemplate);
        mockServer
                .expect(method(HttpMethod.POST))
                .andExpect(requestTo(TOKEN_URL))
                .andExpect(header("Accept", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string("grant_type=authorization_code&code=my_code&redirect_uri=http%3A%2F%2Flocalhost&client_id=myclient&client_secret=mysecret")) // TODO make it nicer
                .andRespond(withSuccess(JSONValue.toJSONString([access_token: token]), MediaType.APPLICATION_JSON))
        expect:
        client.getAccessToken(code) == token
    }

    def "should fetch user data"() {
        setup:
        def token = 'test_token'
        def mockServer = MockRestServiceServer.createServer(client.restTemplate);
        def expectedURI = UriComponentsBuilder.fromHttpUrl(USER_URL).queryParam('access_token', token).build().toUri()
        mockServer
                .expect(method(HttpMethod.GET))
                .andExpect(requestTo(expectedURI))
                .andRespond(withSuccess('{"id" : "user", "name": "userName"}', MediaType.APPLICATION_JSON))
        expect:
        client.getUserData(token) == [id: 'user', name: 'userName']
    }
}
