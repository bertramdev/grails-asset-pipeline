package asset.pipeline

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * @author Tommy Barker
 */
@TestFor(AssetProcessorService)
class AssetProcessorServiceSpec extends Specification {

    void "asset mapping can be configured"() {
        given:
            def path
            def goodConfig = new ConfigObject()
            goodConfig.grails.assets.mapping = "foo"
            def badConfig = new ConfigObject()
            badConfig.grails.assets.mapping = "foo/bar"

        when: "retrieving mapping with no configuration"
            path = service.assetPath

        then:
            "assets" == path
            noExceptionThrown()

        when: "mapping set to 'foo'"
            service.grailsApplication.config = goodConfig
            path = service.assetPath

        then:
            "foo" == path
            noExceptionThrown()

        when: "mapping set to 'foo/bar'"
            service.grailsApplication.config = badConfig
            service.assetPath

        then: "error is thrown since only one level is supported"
            thrown(IllegalArgumentException)
    }
}
