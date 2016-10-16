package jetbrains.buildServer.auth.oauth

import spock.lang.Specification
import spock.lang.Unroll

class ConfigurationValidatorTest extends Specification {

    Map<String, String> configuration = [:];

    def setup() {
        configuration[ConfigKey.authorizeEndpoint.toString()] = 'http://localhost:8080/oauth/authorize'
        configuration[ConfigKey.tokenEndpoint.toString()] = 'http://localhost:8080/oauth/token'
        configuration[ConfigKey.userEndpoint.toString()] = 'http://localhost:8080/oauth/user'
        configuration[ConfigKey.clientId.toString()] = 'clientID'
        configuration[ConfigKey.clientSecret.toString()] = 'clientSsss'
        configuration[ConfigKey.scope.toString()] = 'scope'
    }

    def "configuration is valid"() {
        expect:
        new ConfigurationValidator().validate(configuration).isEmpty();
    }

    def "configuration is valid if preset define values"() {
        given:
        configuration[ConfigKey.preset.toString()] = 'github';
        configuration.remove(ConfigKey.authorizeEndpoint.toString())
        expect:
        new ConfigurationValidator().validate(configuration).isEmpty();
    }

    @Unroll
    def "configuration is not valid without #key"() {
        when:
        configuration.remove(key.toString())
        List<String> errors = new ConfigurationValidator().validate(configuration)
        then:
        !errors.isEmpty()
        errors.size() == 1
        errors.get(0) == msg

        where:
        key                         || msg
        ConfigKey.authorizeEndpoint || "Authorization endpoint should be specified."
        ConfigKey.tokenEndpoint     || "Token endpoint should be specified."
        ConfigKey.userEndpoint      || "User endpoint should be specified."
        ConfigKey.clientId          || "Client ID should be specified."
        ConfigKey.clientSecret      || "Client secret should be specified."
        ConfigKey.scope             || "Scope should be specified."
    }

}
