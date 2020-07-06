package jetbrains.buildServer.auth.oauth;

import org.json.simple.JSONArray;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class OAuthUser {
    private static final String[] IDS_LIST = new String[]{"login", "username", "id", "preferred_username"};
    private static final String[] NAMES_LIST = new String[]{"name", "display_name", "displayName"};
    private static final String[] EMAIL_LIST = new String[]{"email", "mail"};
    private final String GROUPS_KEY = "groups";

    private final String id;
    private final String name;
    private final String email;
    private final Set<String> groups;

    public OAuthUser(String id) {
        this(id, null, null, null);
    }

    public OAuthUser(String id, String name, String email, Set<String> groups) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.groups = groups;
    }

    public OAuthUser(Map userData) {
        this.id = getValueByKeys(userData, IDS_LIST);
        this.name = getValueByKeys(userData, NAMES_LIST);
        this.email = getValueByKeys(userData, EMAIL_LIST);
        this.groups = parseGroups(userData, GROUPS_KEY);
    }

    private String getValueByKeys(Map userData, String[] keys) {
        if (userData == null) {
            return null;
        }
        String value = null;
        for (String key : keys) {
            value = (String) userData.get(key);
            if (value != null) {
                break;
            }
        }
        return value;
    }

    private Set<String> parseGroups(Map userData, String key) {
        if (userData == null) {
            return new HashSet<>();
        }
        Object groupsObject = userData.get(key);
        if (groupsObject == null) {
            return new HashSet<>();
        }
        JSONArray groupsJsonArray = (JSONArray) groupsObject;
        int groupSize = groupsJsonArray.size();
        String[] groupsArray = new String[groupSize];
        for (int i = 0; i < groupSize; i++) {
            groupsArray[i] = (String) groupsJsonArray.get(i);
        }
        return new HashSet(Arrays.asList(groupsArray));
    }

    public String getId() {
        return Optional.ofNullable(id).orElse(email);
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void validate(AuthenticationSchemeProperties properties) throws Exception {
        if (this.getId() == null) {
            throw new Exception("Unauthenticated since user endpoint does not return any login id");
        }
        List<String> emailDomains = properties.getEmailDomains();

        if (emailDomains != null && !emailDomains.isEmpty()) {
            boolean isValid = emailDomains.stream().anyMatch(emailDomain -> {
                if (!emailDomain.startsWith("@")) {
                    emailDomain = "@" + emailDomain;
                }

                return this.getEmail() != null && this.getEmail().endsWith(emailDomain);
            });

            if (!isValid) {
                throw new Exception("Unauthenticated since user email is not in " + emailDomains.toString());
            }
        }
    }

    @Override
    public String toString() {
        String groupsText = (getGroups() != null && getGroups().size() >0) ?
                ", groups='" + getGroups().toString() + '\'' : "";
        return "OAuthUser{" + "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", email='" + getEmail() + '\'' +
                groupsText +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OAuthUser oAuthUser = (OAuthUser) o;
        return Objects.equals(id, oAuthUser.id) &&
                Objects.equals(name, oAuthUser.name) &&
                Objects.equals(email, oAuthUser.email) &&
                Objects.equals(groups, oAuthUser.groups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, groups);
    }
}
