package asset.pipeline

import grails.util.Environment

class AssetMethodTagLib {

	static namespace = "g"
	static returnObjectForTags = ['assetPath']

	def grailsApplication
	def assetProcessorService

	def assetPath = { attrs ->
		def src
        //unused
		def ignorePrefix = false
        def absoluteUrl = false
		if (attrs instanceof Map) {
			src = attrs.src
            //unused
			ignorePrefix = attrs.containsKey('ignorePrefix')? attrs.ignorePrefix : false
            absoluteUrl = attrs.containsKey('absoluteUrl') ? attrs.absoluteUrl : false
		} else {
			src = attrs
		}

		def conf = grailsApplication.config.grails.assets

		def assetUrl = assetUriRootPath(grailsApplication, request, absoluteUrl)

		if(conf.precompiled) {
			def realPath = conf.manifest.getProperty(src)
			if(realPath) {
				return "${assetUrl}${realPath}"
			}
		}
		return "${assetUrl}${src}"
	}


	private assetUriRootPath(grailsApplication, request, absoluteUrl=false) {
		def context = grailsApplication.mainContext //unused
		def conf    = grailsApplication.config.grails.assets
		def mapping = assetProcessorService.assetMapping
		if(conf.url && conf.url instanceof Closure) {
			return conf.url.call(request)
		} else {
            if(absoluteUrl){
                return grailsApplication.config.grails.serverURL + "${request.contextPath?.endsWith('/') ? '' : '/'}$mapping/"
            }
            String relativePathToResource = (request.contextPath + "${request.contextPath?.endsWith('/') ? '' : '/'}$mapping/" )
            return conf.url ?: relativePathToResource
		}

	}
}
