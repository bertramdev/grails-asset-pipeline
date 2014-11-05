package asset.pipeline.fs

import org.springframework.core.io.*


class SpringResourceAssetResolverSpec extends spock.lang.Specification {


    def "Test that the getAsset method resolves an AssetFile instance"() {
        given:"A resourceLoader instancer with an asset resolver"
            def resourceLoader = new FileSystemResourceLoader()
            def assetResolver = new SpringResourceAssetResolver("spring", resourceLoader, "grails-app/assets/javascripts")

        when:"A resource is loaded"
            def assetFile = assetResolver.getAsset("asset-pipeline/test/test", "application/javascript", "js")


        then:"It resolves the asset correctly"
            assetFile != null
            assetFile.path == 'asset-pipeline/test/test.js'
            assetFile.inputStream != null

    }

    def "Test that the getAssets method resolves all AssetFile instances"() {
        given:"A resourceLoader instancer with an asset resolver"
            def resourceLoader = new FileSystemResourceLoader()
            def assetResolver = new SpringResourceAssetResolver("spring", resourceLoader, "grails-app/assets/javascripts")

        when:"A resource is loaded"
            def assetFiles = assetResolver.getAssets("asset-pipeline/test", "application/javascript")


        then:"It resolves the asset correctly"
            assetFiles.size() == 14

    }

    def "Test that the scanForResources method scans and locates AssetFile instances"() {
        given:"A resourceLoader instancer with an asset resolver"
            def resourceLoader = new FileSystemResourceLoader()
            def assetResolver = new SpringResourceAssetResolver("spring", resourceLoader, "grails-app/assets/javascripts")

        when:"A resource is loaded"
            def assetFiles = assetResolver.scanForFiles([], [ 'asset-pipeline/test/libs/*'])


        then:"It resolves the asset correctly"
            assetFiles.size() ==4

    }    
}