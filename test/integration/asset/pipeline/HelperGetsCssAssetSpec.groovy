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

import grails.test.spock.IntegrationSpec

class HelperGetsCssAssetSpec extends IntegrationSpec {

    def "gets a css asset from a uri"() {
        given: "A uri"
            def uri = "asset-pipeline/test/test.css"

        when:
            def file = AssetHelper.fileForUri(uri)

        then:
            file instanceof CssAssetFile
    }

    def "gets a css asset given its uri and contentType"() {
        given: "A uri and contentType"
            def uri = "asset-pipeline/test/test.css"
            def contentType = "text/css"

        when:
            def file = AssetHelper.fileForUri(uri, contentType)

        then:
            file instanceof CssAssetFile
    }

    def "gets a css asset given its uri and file extension" () {
        given: "A uri"
            def uri = "asset-pipeline/test/test.css"
        and: "contentType"
            def contentType = "text/css"
        and: "file extension"
            def fileExtension = "css"

        when:
            def file = AssetHelper.fileForUri(uri, contentType, fileExtension)

        then:
            file instanceof CssAssetFile
    }
}
