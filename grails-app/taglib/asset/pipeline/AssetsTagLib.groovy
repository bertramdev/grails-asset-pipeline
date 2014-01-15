package asset.pipeline

import grails.util.Environment

class AssetsTagLib {

	static namespace = "asset"
	static returnObjectForTags = ['assetPath']

	def grailsApplication
	def assetProcessorService

	/**
	 * @attr src REQUIRED
	 */
	def javascript = { attrs ->
		def src = attrs.remove('src')
		attrs.remove('href')
		def uri
		def extension

		def conf = grailsApplication.config.grails.assets
		def debugParameter = params."_debugResources" == 'y' || params."_debugAssets" == "y"
    def debugMode = (conf.allowDebugParam && debugParameter) ||  (Environment.current == Environment.DEVELOPMENT && conf.bundle != true)

		if(!debugMode) {
			out << "<script src=\"${assetPath(src)}\" type=\"text/javascript\" ${paramsToHtmlAttr(attrs)}></script>"
		} else {
			if (src.lastIndexOf(".") >= 0) {
				uri = src.substring(0, src.lastIndexOf("."))
				extension = src.substring(src.lastIndexOf(".") + 1)
			} else {
				uri = src
				extension = 'js'
			}
			// def startTime = new Date().time
			def list = assetProcessorService.getDependencyList(uri, 'application/javascript', extension)
			def modifierParams = ["compile=false"]
			if(attrs.charset) {
				modifierParams << "encoding=${attrs.charset}"
			}
			list.each { dep ->
				def depAssetPath = assetPath([src: "${dep.path}", ignorePrefix:true])
				out << "<script src=\"${depAssetPath}?${modifierParams.join("&")}\" type=\"text/javascript\" ${paramsToHtmlAttr(attrs)}></script>"
			}
			// println "Fetching Dev Mode Dependency List Time ${new Date().time - startTime}"
		}
	}

	/**
	 * @attr href REQUIRED
	 * @attr src OPTIONAL alternative to href
	 */
	def stylesheet = { attrs ->
		def src  = attrs.remove('src')
		def href = attrs.remove('href')
		if(href) {
			src = href
		}
		def conf = grailsApplication.config.grails.assets
		def uri
		def extension
		def debugParameter = params."_debugResources" == 'y' || params."_debugAssets" == "y"
    def debugMode      = (conf.allowDebugParam && debugParameter) ||  (Environment.current == Environment.DEVELOPMENT && conf.bundle != true)

		if(!debugMode) {
			out << "<link rel=\"stylesheet\" href=\"${assetPath(src)} ${paramsToHtmlAttr(attrs)}\"/>"
		} else {
			if (src.lastIndexOf(".") >= 0) {
				uri = src.substring(0, src.lastIndexOf("."))
				extension = src.substring(src.lastIndexOf(".") + 1)
			} else {
				uri = src
				extension = 'css'
			}
			def list = assetProcessorService.getDependencyList(uri, 'text/css', extension)
			def modifierParams = ["compile=false"]
			if(attrs.charset) {
				modifierParams << "encoding=${attrs.charset}"
			}
			list.each { dep ->
				def depAssetPath = assetPath([src: "${dep.path}", ignorePrefix:true])
				out << "<link rel=\"stylesheet\" href=\"${depAssetPath}?${modifierParams.join("&")}\" ${paramsToHtmlAttr(attrs)} />"
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


	Closure assetPath = { attrs ->
		def src
		def ignorePrefix = false
    if (attrs instanceof Map) {
    	src = attrs.src
    	ignorePrefix = attrs.containsKey('ignorePrefix')? attrs.ignorePrefix : false
    } else {
    	src = attrs
    }

		def conf = grailsApplication.config.grails.assets

		def assetRootPath = assetUriRootPath(grailsApplication, request)
		def assetUrl = (!ignorePrefix && conf.url) ? conf.url : "$assetRootPath"

		if(conf.precompiled) {
			def realPath = conf.manifest.getProperty(src)
			if(realPath) {
				return "${assetUrl}${realPath}"
			}
		}
		return "${assetUrl}${src}"
	}

	Closure assetPathExists = { attrs, body ->
		def src = attrs.remove('src')
		def conf = grailsApplication.config.grails.assets
		if(conf.precompiled) {
			def realPath = conf.manifest.getProperty(src)
			if(realPath) {
				if(body) {
					out << body
				}
				return true
			}
		} else {
			def assetFile = AssetHelper.fileForFullName(src)
			if(assetFile) {
				if(body) {
					out << body
				}
				return true
			}
		}
		return false
	}

	private paramsToHtmlAttr(attrs) {
		attrs.collect { key, value -> "${key}=\"${value.replace('\'', '\\\'')}\"" }?.join(" ")
	}




	private assetUriRootPath(grailsApplication, request) {
		def context = grailsApplication.mainContext
		def conf    = grailsApplication.config.grails.assets
		def mapping = context.assetProcessorService.assetMapping

		return conf.url ?: (request.contextPath + "${request.contextPath?.endsWith('/') ? '' : '/'}$mapping/" )
	}
}
