package asset.pipeline.grails

import grails.util.Environment

class AssetMethodTagLib {

    static namespace = "g"
    static returnObjectForTags = ['assetPath']

    def grailsApplication
    def assetProcessorService
    def grailsLinkGenerator

    def assetPath = { attrs ->
        def src
        //unused
        def ignorePrefix = false
        def absolute = false
        if (attrs instanceof Map) {
            
            src = attrs.src
            //unused
            ignorePrefix = attrs.containsKey('ignorePrefix')? attrs.ignorePrefix : false
            absolute = attrs.containsKey('absolute') ? attrs.absolute : false
        } else {
            
            src = attrs
        }
       
        def conf = grailsApplication.config.grails.assets

        def assetUrl = assetUriRootPath(grailsApplication, request, absolute)

        if(conf.precompiled && src) {
            def realPath = conf.manifest.getProperty(src)
            if(realPath) {
                return "${assetUrl}${realPath}"
            }
        }
        return "${assetUrl}${src}"
    }


    private assetUriRootPath(grailsApplication, request, absolute=false) {
        def context = grailsApplication.mainContext //unused
        def conf    = grailsApplication.config.grails.assets
        def mapping = assetProcessorService.assetMapping
        if(conf.url && conf.url instanceof Closure) {
            return conf.url.call(request)
        } else {
            if(absolute && !conf.url){
                return [grailsLinkGenerator.serverBaseURL, "$mapping/"].join('/')
            }
            String relativePathToResource = (request.contextPath + "${request.contextPath?.endsWith('/') ? '' : '/'}$mapping/" )
            return conf.url ?: relativePathToResource
        }

    }
}
