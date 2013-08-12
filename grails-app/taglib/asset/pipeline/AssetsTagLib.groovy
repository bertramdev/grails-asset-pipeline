package asset.pipeline

import grails.util.Environment

class AssetsTagLib {

	static namespace = "asset"

	def grailsApplication
	def assetProcessorService

	def javascript = { attrs ->
		def src = attrs['src']
		def uri
		def extension

		def conf = grailsApplication.config.grails.assets

		if((!conf.containsKey('bundle') && Environment.current != Environment.DEVELOPMENT) || conf.bundle == true) {
			out << "<script src=\"${assetPath(src)}\" type=\"text/javascript\"></script>"
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
				def depAssetPath = assetPath("${dep}")
				out << "<script src=\"${depAssetPath}?compile=false\" type=\"text/javascript\"></script>"
			}
		}
	}

	def stylesheet = { attrs ->
		def src = attrs['src'] ?: attrs['href']
		out << "<link rel=\"stylesheet\" href=\"${assetPath(src)}\"/>"
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

	private paramsToHtmlAttr(attrs) {
		attrs.collect { key, value -> "${key}=\"${value.replace('\'', '\\\'')}\"" }?.join(" ")
	}

	private assetPath(src) {

		def conf = grailsApplication.config.grails.assets

		def assetRootPath = assetUriRootPath(grailsApplication, request)
		def assetUrl = conf.url ?: "$assetRootPath/"

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

		def path = conf.url ?: (request.contextPath + "/$mapping" )


		// if (path.contains("/")) {
		// 		String message = "the property [grails.assets.mapping] can only be one level deep.  For example, 'foo' and 'bar' would be acceptable values, but 'foo/bar' is not"
		// 	throw new IllegalArgumentException(message)
		// }

		return path
	}
}
