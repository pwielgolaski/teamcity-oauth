plugins {
    java
    groovy
    id("io.github.rodm.teamcity-server") version "1.5"
    id("pl.allegro.tech.build.axion-release") version "1.14.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.googlecode.json-simple:json-simple:1.1.1") {
        exclude("junit", "junit")
    }
    // mandatory dependencies for using Spock
    testImplementation("org.codehaus.groovy:groovy-all:3.0.12")
    testImplementation("org.spockframework:spock-core:1.0-groovy-2.4")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
}
repositories {
    mavenCentral()
}

teamcity {
    version = "2022.04.3"
    server {
        descriptor {
            name = "teamcity-oauth2"
            displayName = "teamcity-oauth2"
            version = "0.0.1"
            vendorName = "sre@jetbrains.com"
            description = "oAuth2 authentication plugin"
            downloadUrl = "https://github.com/jetbrains-infra/teamcity-oauth/"
            vendorUrl = "https://github.com/jetbrains-infra/teamcity-oauth/"
            email = "sre@jetbrains.com"
        }
    }
}