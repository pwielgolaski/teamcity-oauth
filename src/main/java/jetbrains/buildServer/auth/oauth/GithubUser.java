package jetbrains.buildServer.auth.oauth;

import com.intellij.openapi.util.text.StringUtil;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GithubUser extends OAuthUser {

    private static final Logger log = Logger.getLogger(GithubUser.class);
    public static final String ORGANIZATION_ENDPOINT = "https://api.github.com/user/orgs";
    private final Supplier<String> organizationSupplier;

    public GithubUser(Map userData, Supplier<String> organizationSupplier) {
        super(userData);
        this.organizationSupplier = organizationSupplier;
    }

    private Set<String> fetchUserOrganizations() {
        String response = organizationSupplier.get();
        log.debug("Fetched user org data: " + response);
        Object parsedResponse = JSONValue.parse(response);
        if (parsedResponse instanceof JSONArray) {
            return ((List<Object>) parsedResponse)
                    .stream()
                    .filter(item -> item instanceof JSONObject)
                    .map(item -> ((JSONObject) item).get("login"))
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toSet());
        } else {
            String message = ((JSONObject) parsedResponse).getOrDefault("message", "Incorrect response:" + response ).toString();
            throw new IllegalStateException(message);
        }
    }

    @Override
    public void validate(AuthenticationSchemeProperties properties) throws Exception {
        super.validate(properties);
        // Check the organizations that the user belongs to for Github Oauth
        String orgs = properties.getOrganizations();
        if (StringUtil.isNotEmpty(orgs)) {
            Set<String> userOrganizations = this.fetchUserOrganizations();
            Set<String> configuredOrganizations = Stream.of(orgs.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());

            configuredOrganizations.retainAll(userOrganizations);
            if (configuredOrganizations.isEmpty()) {
                throw new Exception("User's organization does not match with the ones specified in the oAuth settings");
            }
        }
    }
}