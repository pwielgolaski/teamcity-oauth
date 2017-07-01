package jetbrains.buildServer.auth.oauth;

import com.intellij.openapi.util.text.StringUtil;
import okhttp3.*;
import org.apache.log4j.Logger;
import org.json.simple.JSONValue;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Map;

public class OAuthClient {

    private AuthenticationSchemeProperties properties;
    private static final Logger log = Logger.getLogger(OAuthClient.class);
    private final OkHttpClient httpClient;

    public OAuthClient(AuthenticationSchemeProperties properties) {
        this.properties = properties;
        this.httpClient = HttpClientFactory.createClient(properties.isAllowInsecureHttps());
    }

    public String getRedirectUrl(String state) {
        HttpUrl.Builder builder = HttpUrl.parse(properties.getAuthorizeEndpoint())
                .newBuilder()
                .addQueryParameter("response_type", "code")
                .addQueryParameter("client_id", properties.getClientId())
                .addQueryParameter("state", state)
                .addQueryParameter("redirect_uri", properties.getRootUrl());
        if (StringUtil.isNotEmpty(properties.getScope())) {
            builder.addQueryParameter("scope", properties.getScope());
        }
        return builder.build().toString();

    }

    public String getAccessToken(String code) throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("grant_type", "authorization_code")
                .add("code", code)
                .add("redirect_uri", properties.getRootUrl())
                .add("client_id", properties.getClientId())
                .add("client_secret", properties.getClientSecret())
                .build();

        Request request = new Request.Builder()
                .url(properties.getTokenEndpoint())
                .addHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .post(formBody)
                .build();
        Response response = httpClient.newCall(request).execute();
        Map jsonResponse = (Map) JSONValue.parse(response.body().string());
        return (String) jsonResponse.get("access_token");
    }

    public Map getUserData(String token) throws IOException {
        Request request = new Request.Builder()
                .url(properties.getUserEndpoint())
                .addHeader("Authorization","Bearer " + token)
                .build();
        String response = httpClient.newCall(request).execute().body().string();
        log.debug("Fetched user data: " + response);
        return (Map) JSONValue.parse(response);
    }
}
