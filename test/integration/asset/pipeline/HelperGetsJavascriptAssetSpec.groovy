package asset.pipeline

import grails.plugin.spock.IntegrationSpec

class HelperGetsJavascriptAssetSpec extends IntegrationSpec{

    def "gets a javascript file from a uri"() {
        given: "A uri"
            def uri = "test.js"

        when:
            def file = AssetHelper.fileForUri(uri)

        then:
            file instanceof File
    }

    def "gets a javascript file given a uri and content type"() {
        given: "A uri and contentType"
            def uri = "test.js"
            def contentType = "application/javascript"

        when:
            def file = AssetHelper.fileForUri(uri, contentType)

        then:
            file instanceof JsAssetFile

    }

    def "gets a javascript file given a uri and extension()"() {
        given: "A uri and file extension"
            def uri = "test.js"
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
