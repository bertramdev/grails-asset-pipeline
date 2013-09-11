package asset.pipeline

import grails.util.Environment

class AssetsTagLib {

	static namespace = "asset"

	def grailsApplication
	def assetProcessorService

	/**
	 * @attr src REQUIRED
	 * @attr defer OPTIONAL add to request and output with deferred tag if needed
	 */
	def javascript = { attrs ->
		def src = attrs['src']
		def uri
		def extension
		def defer = attrs['defer'] ?: false

		def conf = grailsApplication.config.grails.assets
		def debugParameter = params."_debugResources" == 'y' || params."_debugAssets" == "y"
   		def debugMode = (conf.allowDebugParam && debugParameter) ||  (Environment.current == Environment.DEVELOPMENT && conf.bundle != true)

		if(!debugMode) {
			def output = "<script src=\"${assetPath(src)}\" type=\"text/javascript\"></script>"
			if( defer ) {
				addToDeferred( request, output )
			}
			else {
				out << output
			}
		} else {
			if (src.lastIndexOf(".") >= 0) {
				uri = src.substring(0, src.lastIndexOf("."))
				extension = src.substring(src.lastIndexOf(".") + 1)
			} else {
				uri = src
				extension = 'js'
			}
			def list = assetProcessorService.getDependencyList(uri, 'application/javascript', extension)
			list.each { dep ->
				def depAssetPath = assetPath("${dep}", true)
				def output = "<script src=\"${depAssetPath}?compile=false\" type=\"text/javascript\"></script>"
				if( defer ) {
					addToDeferred( request, output )
				}
				else {
					out << output
				}
			}
		}
	}

	/**
	 * @attr href REQUIRED
	 * @attr src OPTIONAL alternative to href
	 */
	def stylesheet = { attrs ->
		def src  = attrs['src'] ?: attrs['href']
		def conf = grailsApplication.config.grails.assets
		def uri
		def extension
		def debugParameter = params."_debugResources" == 'y' || params."_debugAssets" == "y"
		def debugMode      = (conf.allowDebugParam && debugParameter) ||  (Environment.current == Environment.DEVELOPMENT && conf.bundle != true)

		if(!debugMode) {
			out << "<link rel=\"stylesheet\" href=\"${assetPath(src)}\"/>"
		} else {
			if (src.lastIndexOf(".") >= 0) {
				uri = src.substring(0, src.lastIndexOf("."))
				extension = src.substring(src.lastIndexOf(".") + 1)
			} else {
				uri = src
				extension = 'css'
			}
			def list = assetProcessorService.getDependencyList(uri, 'text/css', extension)
			list.each { dep ->
				def depAssetPath = assetPath("${dep}", true)
				out << "<link rel=\"stylesheet\" href=\"${depAssetPath}?compile=false\"/>"
			}
		}
	}

	def image = { attrs ->
		def src = attrs.remove('src')
		out << "<img src=\"${assetPath(src)}\" ${paramsToHtmlAttr(attrs)}/>"
	}

	/**
	 * @attr href REQUIRED
	 * @attr rel REQUIRED
	 * @attr type OPTIONAL
	 */
	def link = { attrs ->
		def href = attrs.remove('href')
		out << "<link ${paramsToHtmlAttr(attrs)} href=\"${assetPath(href)}\"/>"
	}

	def deferred = { attrs ->
		request.getAttribute( 'DEFERRED_ASSETS' )?.each {
			out << it
		}
	}

	private addToDeferred( request, resource ) {
		( request.getAttribute( 'DEFERRED_ASSETS' ) ?: [] ).with { list ->
			list << resource
			request.setAttribute( 'DEFERRED_ASSETS' )
		}
	}

	private paramsToHtmlAttr(attrs) {
		attrs.collect { key, value -> "${key}=\"${value.replace('\'', '\\\'')}\"" }?.join(" ")
	}

	private assetPath(src, ignorePrefix = false) {

		def conf = grailsApplication.config.grails.assets

		def assetRootPath = assetUriRootPath(grailsApplication, request)
		def assetUrl = (!ignorePrefix && conf.url) ? conf.url : "$assetRootPath/"

		if(conf.precompiled) {
			def realPath = conf.manifest.getProperty(src)
			if(realPath) {
				return "${assetUrl}${realPath}"
			}
		}
		return "${assetUrl}${src}"
	}



	private assetUriRootPath(grailsApplication, request) {
		def context = grailsApplication.mainContext
		def conf    = grailsApplication.config.grails.assets
		def mapping = context.assetProcessorService.assetMapping

		return conf.url ?: (request.contextPath + "${request.contextPath?.endsWith('/') ? '' : '/'}$mapping" )
	}
}
