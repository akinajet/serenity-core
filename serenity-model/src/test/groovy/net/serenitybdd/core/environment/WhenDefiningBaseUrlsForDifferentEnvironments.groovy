package net.serenitybdd.core.environment

import net.thucydides.core.util.EnvironmentVariables
import net.thucydides.core.util.MockEnvironmentVariables
import spock.lang.Specification

class WhenDefiningBaseUrlsForDifferentEnvironments extends Specification {

    EnvironmentVariables environmentVariables = new MockEnvironmentVariables()

    def "if no environments are defined, the normal base url will be used"() {
        given:
            environmentVariables.setProperty("webdriver.base.url","http://foo.com")
        when:
            def baseUrl = EnvironmentSpecificConfiguration.from(environmentVariables).getProperty("webdriver.base.url")
        then:
            baseUrl == "http://foo.com"
    }

    def """different environment configurations can be defined using the "environment" system property.
             environments {
                dev {
                    webdriver.base.url = http://dev.myapp.myorg.com
                }
        """() {

        given:
            environmentVariables.setProperties([
                    "environment":"dev",
                    "environments.dev.webdriver.base.url":"http://dev.foo.com"
            ])
        when:
            def baseUrl = EnvironmentSpecificConfiguration.from(environmentVariables).getProperty("webdriver.base.url")
        then:
            baseUrl == "http://dev.foo.com"

    }


    def """If a default environment is defined, it will be used if no environment is set
             environments {
                default {
                    webdriver.base.url = http://default.myapp.myorg.com
                }
                dev {
                    webdriver.base.url = http://dev.myapp.myorg.com
                }
        """() {

        given:
            environmentVariables.setProperties([
                    "environments.default.webdriver.base.url":"http://default.foo.com",
                    "environments.dev.webdriver.base.url":"http://dev.foo.com"
            ])
        when:
            def baseUrl = EnvironmentSpecificConfiguration.from(environmentVariables).getProperty("webdriver.base.url")
        then:
            baseUrl == "http://default.foo.com"

    }


    def """If a environment property is not defined in an environment, we fall back on the default
             environments {
                default {
                    webdriver.base.url = http://default.myapp.myorg.com
                }
                dev {
                    webdriver.base.url = http://dev.myapp.myorg.com
                }
        """() {

        given:
            environmentVariables.setProperties([
                    "environments.default.webdriver.base.url":"http://default.foo.com",
                    "environments.dev.some.other.property":"some.other.value",
                    "environment":"dev",
            ])
        when:
            def baseUrl = EnvironmentSpecificConfiguration.from(environmentVariables).getProperty("webdriver.base.url")
        then:
            baseUrl == "http://default.foo.com"
    }

    def "If a environment property is not defined anywhere, a UndefinedEnvironmentVariableException should be thrown"() {

        given:
            environmentVariables.setProperties([
                    "environments.dev.webdriver.base.url":"http://default.foo.com",
                    "environment":"dev",
            ])
        when:
            EnvironmentSpecificConfiguration.from(environmentVariables).getProperty("unknown.property")
        then:
            thrown(UndefinedEnvironmentVariableException)
    }


    def """The special 'all' environment will work for any environment, and can be used with variable substitution from previous sections.
        For environment configuration variable substitution, we sue the # symbol to avoid conflicts with the TypeSafe variable substitution.
        environments {
            default {
                webdriver.base.url = http://localhost:8080/myapp
                accounts.service.url = http://localhost:8081
            }
            dev {
                webdriver.base.url = http://dev.myapp.myorg.com
                accounts.service.url = http://dev.accounts.myorg.com
            }
            staging {
                webdriver.base.url = http://staging.myapp.myorg.com
                accounts.service.url = http://staging.accounts.myorg.com
            }
            all {
                home.url = #{webdriver.base.url}/home
                config.url = #{webdriver.base.url}/config
                accounts.url = #{accounts.service.url}/accounts
            }
        }
        """() {

        given:
            environmentVariables.setProperties([
                    "environments.default.webdriver.base.url":"http://default.foo.com",
                    "environments.dev.webdriver.base.url":"http://dev.foo.com",
                    "environment":"dev",
                    "environments.all.home.url":"#{webdriver.base.url}/home",
            ])
        when:
            def baseUrl = EnvironmentSpecificConfiguration.from(environmentVariables).getProperty("home.url")
        then:
            baseUrl == "http://dev.foo.com/home"

    }

    def """If no configured environment can be found, an exception is thrown
             environments {
                default {
                    webdriver.base.url = http://default.myapp.myorg.com
                }
                dev {
                    webdriver.base.url = http://dev.myapp.myorg.com
                }
        """() {

        given:
            environmentVariables.setProperties([
                    "environments.default.webdriver.base.url":"http://default.foo.com",
                    "environment":"prod",
            ])
        when:
            EnvironmentSpecificConfiguration.from(environmentVariables).getProperty("webdriver.base.url")
        then:
            thrown(UnknownEnvironmentException)

    }

}
