package jetbrains.buildServer.auth.oauth;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class OAuthUser {
    public static final String[] IDS_LIST = new String[]{"login", "username", "id"};
    public static final String[] NAMES_LIST = new String[]{"name", "display_name"};
    public static final String[] EMAIL_LIST = new String[]{"email"};

    private String id;
    private String name;
    private String email;

    public OAuthUser(String id) {
        this(id, null, null);
    }

    public OAuthUser(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public OAuthUser(Map userData) {
        this.id = getValueByKeys(userData, IDS_LIST);
        this.name = getValueByKeys(userData, NAMES_LIST);
        this.email = getValueByKeys(userData, EMAIL_LIST);
    }

    private String getValueByKeys(Map userData, String[] keys) {
        if (userData == null)
            return null;
        String value = null;
        for (String key : keys) {
            value = (String) userData.get(key);
            if (value != null) {
                break;
            }
        }
        return value;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OAuthUser{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OAuthUser oAuthUser = (OAuthUser) o;
        return Objects.equals(id, oAuthUser.id) &&
                Objects.equals(name, oAuthUser.name) &&
                Objects.equals(email, oAuthUser.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email);
    }
}
