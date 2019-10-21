package jetbrains.buildServer.auth.oauth

import org.assertj.core.util.Files
import org.assertj.core.util.Lists
import org.json.simple.JSONValue
import org.springframework.core.io.DefaultResourceLoader
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class OAuthUserTest extends Specification {

    @Unroll("should read user details #location")
    def "should read user details"() {
        setup:
        def file = new DefaultResourceLoader().getResource(location).file
        def content = Files.contentOf(file, StandardCharsets.UTF_8)
        when:
        def user = new OAuthUser((Map) JSONValue.parse(content))
        then:
        user.id == expectedId
        user.name == expectedName
        user.groups == expectedGroups
        where:
        location                        || expectedId             | expectedName        | expectedGroups
        "classpath:user/github.json"    || 'pwielgolaski'         | 'Piotr Wielgolaski' | ["dev"]
        "classpath:user/bitbucket.json" || 'pwielgolaski'         | 'Piotr Wielgolaski' | ["dev"]
        "classpath:user/google.json"    || 'superemail@gmail.com' | 'Piotr Wielgołaski' | ["dev"]
        "classpath:user/azure.json"     || 'any-guid'             | 'Piotr Wielgołaski' | ["dev"]
    }

    def "should return name if id is not given"() {
        expect:
        new OAuthUser(null, 'name', 'email@domain', Lists.newArrayList("dev")).id == 'email@domain'
    }
}
