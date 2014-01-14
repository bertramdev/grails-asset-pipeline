/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
            path = service.assetMapping

        then:
            "assets" == path
            noExceptionThrown()

        when: "mapping set to 'foo'"
            service.grailsApplication.config = goodConfig
            path = service.assetMapping

        then:
            "foo" == path
            noExceptionThrown()

        when: "mapping set to 'foo/bar'"
            service.grailsApplication.config = badConfig
            service.assetMapping

        then: "error is thrown since only one level is supported"
            thrown(IllegalArgumentException)
    }
}
