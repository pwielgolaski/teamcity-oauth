package jetbrains.buildServer.auth.oauth;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConfigurationValidator {

    private static final Map<ConfigKey, String> EMPTY_KEY_VALUE_MESSAGES;

    static {
        EMPTY_KEY_VALUE_MESSAGES = new HashMap<ConfigKey, String>();
        EMPTY_KEY_VALUE_MESSAGES.put(ConfigKey.authorizeEndpoint, "Authorization endpoint should be specified.");
        EMPTY_KEY_VALUE_MESSAGES.put(ConfigKey.tokenEndpoint, "Token endpoint should be specified.");
        EMPTY_KEY_VALUE_MESSAGES.put(ConfigKey.userEndpoint, "User endpoint should be specified.");
        EMPTY_KEY_VALUE_MESSAGES.put(ConfigKey.clientId, "Client ID should be specified.");
        EMPTY_KEY_VALUE_MESSAGES.put(ConfigKey.clientSecret, "Client secret should be specified.");
        EMPTY_KEY_VALUE_MESSAGES.put(ConfigKey.scope, "Scope should be specified.");
    }

    public Collection<String> validate(@NotNull Map<String, String> properties) {
        final Collection<String> errors = new ArrayList<String>();
        for (Map.Entry<ConfigKey, String> mapping : EMPTY_KEY_VALUE_MESSAGES.entrySet()) {
            if (StringUtil.isEmptyOrSpaces(properties.get(mapping.getKey().toString()))) {
                errors.add(mapping.getValue());
            }
        }
        return errors;
    }


}
