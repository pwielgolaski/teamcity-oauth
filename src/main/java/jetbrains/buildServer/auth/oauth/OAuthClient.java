package jetbrains.buildServer.auth.oauth;


import org.apache.log4j.Logger;
import org.json.simple.JSONValue;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

public class OAuthClient {

    private RestTemplate restTemplate;
    private AuthenticationSchemeProperties properties;
    private static final Logger log = Logger.getLogger(OAuthClient.class);

    public OAuthClient(AuthenticationSchemeProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    public String getRedirectUrl(String state) {
        return String.format("%s?response_type=code&client_id=%s&scope=%s&state=%s&redirect_uri=%s",
                properties.getAuthorizeEndpoint(),
                properties.getClientId(),
                properties.getScope(),
                state,
                properties.getRootUrl());
    }

    public String getAccessToken(String code) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", properties.getRootUrl());
        form.add("client_id", properties.getClientId());
        form.add("client_secret", properties.getClientSecret());
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        String response = restTemplate.postForObject(properties.getTokenEndpoint(), new HttpEntity<Object>(form, headers), String.class);
        Map jsonResponse = (Map) JSONValue.parse(response);
        return (String) jsonResponse.get("access_token");
    }

    public Map getUserData(String token) {
        String url = String.format("%s?access_token=%s", properties.getUserEndpoint(), token);
        String response = restTemplate.getForObject(url, String.class);
        log.debug("Fetched user data: " + response);
        return (Map) JSONValue.parse(response);
    }
}
