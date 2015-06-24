package asset.pipeline.grails

import grails.util.Environment
import org.apache.commons.lang.StringUtils

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
        def configUrl = conf.url
        if(conf.url && conf.url instanceof Closure) {
            configUrl = conf.url.call(request)
            if(configUrl){
                return configUrl
            }
        } 
        if(absolute && !configUrl){
            return [grailsLinkGenerator.serverBaseURL, "$mapping/"].join('/')
        }
        def contextPath = StringUtils.trimToEmpty(grailsLinkGenerator?.contextPath)
        String relativePathToResource = (contextPath + "${contextPath?.endsWith('/') ? '' : '/'}$mapping/" )
        return configUrl ?: relativePathToResource

    }
}
