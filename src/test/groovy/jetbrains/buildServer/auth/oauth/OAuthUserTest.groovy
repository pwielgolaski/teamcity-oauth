package jetbrains.buildServer.auth.oauth

import org.assertj.core.util.Files
import org.json.simple.JSONValue
import org.springframework.core.io.DefaultResourceLoader
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.Charset

class OAuthUserTest extends Specification {

    @Unroll("should read user details #location")
    def "should read user details"() {
        setup:
        def file = new DefaultResourceLoader().getResource(location).file
        def content = Files.contentOf(file, Charset.defaultCharset())
        when:
        def user = new OAuthUser((Map) JSONValue.parse(content))
        then:
        user.id == expectedId
        user.name == expectedName
        where:
        location                        || expectedId             | expectedName
        "classpath:user/github.json"    || 'pwielgolaski'         | 'Piotr Wielgolaski'
        "classpath:user/bitbucket.json" || 'pwielgolaski'         | 'Piotr Wielgolaski'
    }

    def "should return name if id is not given"() {
        expect:
        new OAuthUser(null, 'name', 'email@domain').id == 'email@domain'
    }
}
