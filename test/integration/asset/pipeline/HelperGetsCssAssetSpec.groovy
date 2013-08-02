package asset.pipeline

import grails.plugin.spock.IntegrationSpec

class HelperGetsCssAssetSpec extends IntegrationSpec {

    def "gets a css asset from a uri"() {
        given: "A uri"
            def uri = "asset-pipeline/test/test.css"

        when:
            def file = AssetHelper.fileForUri(uri)

        then:
            file instanceof File
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
