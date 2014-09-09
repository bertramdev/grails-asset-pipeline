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

class HelperGetsJavascriptAssetSpec extends IntegrationSpec{

    def "gets a javascript file from a uri"() {
        given: "A uri"
            def uri = "asset-pipeline/test/test.js"

        when:
            def file = AssetHelper.fileForUri(uri)

        then:
            file instanceof GenericAssetFile
    }

    def "gets a javascript file given a uri and content type application/javascript"() {
        given: "A uri and contentType"
            def uri = "asset-pipeline/test/test.js"
            def contentType = "application/javascript"

        when:
            def file = AssetHelper.fileForUri(uri, contentType)

        then:
            file instanceof JsAssetFile
    }

    def "gets a javascript file given a uri and content type application/x-javascript"() {
        given: "A uri and contentType"
            def uri = "asset-pipeline/test/test.js"
            def contentType = "application/x-javascript"

        when:
            def file = AssetHelper.fileForUri(uri, contentType)

        then:
            file instanceof JsAssetFile
    }

    def "gets a javascript file given a uri and content type text/javascript"() {
        given: "A uri and contentType"
            def uri = "asset-pipeline/test/test.js"
            def contentType = "text/javascript"

        when:
            def file = AssetHelper.fileForUri(uri, contentType)

        then:
            file instanceof JsAssetFile
    }

    def "gets a javascript file given a uri and extension()"() {
        given: "A uri and file extension"
            def uri = "asset-pipeline/test/test.js"
            def fileExtension = "js"
            def contentType = "application/javascript"

        when:
            def file = AssetHelper.fileForUri(uri, contentType, fileExtension)

        then:
            file instanceof JsAssetFile
    }

    def "gets a javascript file of different extension given a uri and content type"() {
        given: "A uri and file extension"
            JsAssetFile.extensions << "javascript"
            def uri = "asset-pipeline/test/test_ext.js"
            def fileExtension = "js"
            def contentType = "application/javascript"

        when:
            def file = AssetHelper.fileForUri(uri, contentType, fileExtension)

        then:
            file instanceof JsAssetFile
    }

    def "returns null if javascript file doesn't exist"() {
        given: "A uri for a non-existent javascript file"
            def uri = "invalid.js"

        when:
            def file = AssetHelper.fileForUri(uri)

        then:
            !file
    }
}
