package jetbrains.buildServer.auth.oauth;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.simple.JSONValue;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

public class GithubUser extends OAuthUser {

  private static final String ORGANIZATION_ENDPOINT = "https://api.github.com/user/orgs";
  private final Map<Boolean, OkHttpClient> httpClients = new HashMap<>();
  private final String[] organizations;

  public GithubUser(Map userData, String token, Boolean allowInsecureHttps) throws IOException {
      super(userData);
      this.organizations = fetchUserOrganizations(token, allowInsecureHttps);
  }

  public String[] getOrganizations() {
      return organizations;
  }

  private String[] fetchUserOrganizations(String token, Boolean allowInsecureHttps) throws IOException { 
      Request orgRequest = new Request.Builder()
            .url(ORGANIZATION_ENDPOINT)
            .addHeader("Authorization","Bearer " + token)
            .build();
      String response = getHttpClient(allowInsecureHttps).newCall(orgRequest).execute().body().string();
      JSONArray parsedResponse = (JSONArray) JSONValue.parse(response);
      String[] orgs = new String[parsedResponse.size()];
      for(int i = 0; i < parsedResponse.size(); i++) {
        JSONObject value = (JSONObject) parsedResponse.get(i);
        orgs[i] = (String) value.get("login");
      }
      return orgs;
  }

  private OkHttpClient getHttpClient(Boolean allowInsecureHttps) {
      return httpClients.computeIfAbsent(allowInsecureHttps, HttpClientFactory::createClient);
  }
}