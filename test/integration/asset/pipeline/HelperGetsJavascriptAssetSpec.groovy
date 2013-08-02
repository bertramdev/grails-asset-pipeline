package asset.pipeline

import grails.plugin.spock.IntegrationSpec

class HelperGetsJavascriptAssetSpec extends IntegrationSpec{

    def "gets a javascript file from a uri"() {
        given: "A uri"
            def uri = "test.js"

        when:
            def file = AssetHelper.fileForUri(uri)//, "application/javascript", 'js')

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
}
