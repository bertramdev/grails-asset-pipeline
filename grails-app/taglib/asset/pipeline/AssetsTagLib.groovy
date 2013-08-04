package asset.pipeline

import grails.util.Environment

class AssetsTagLib {

	static namespace = "asset"

	def grailsApplication
	def assetProcessorService

	def javascript = { attrs ->
		def src       = attrs['src']
		def uri
		def extension

		def conf = grailsApplication.config.grails.assets

		if((!conf.containsKey('bundle') && Environment.current != Environment.DEVELOPMENT) || conf.bundle == true) {
			out << "<script src=\"${assetPath(src)}\" type=\"application/javascript\"></script>"
		} else {

			if(src.lastIndexOf(".") >= 0) {
        uri = src.substring(0,src.lastIndexOf("."))
        extension = src.substring(src.lastIndexOf(".") + 1)
      } else {
      	uri = src
      	extension = 'js'
      }
			def list = assetProcessorService.getDependencyList(uri, 'application/javascript', extension)
			list.each { dep ->
				def depAssetPath = assetPath("${dep}")
				out << "<script src=\"${depAssetPath}?compile=false\" type=\"application/javascript\"></script>"
			}
		}
	}

	def stylesheet = { attrs ->
		def src = attrs['src'] ?: attrs['href']
		out << "<link rel=\"stylesheet\" href=\"${assetPath(src)}\"/>"
	}

	def image = { attrs ->
		def src = attrs.remove('src')
		def htmlAttributes = attrs.collect {key, value -> "${key}='${value.replace('\'','\\\'')}'"}

		out << "<img src=\"${assetPath(src)}\" ${htmlAttributes?.join(" ")}/>"
	}

	private assetPath(src) {

		def conf = grailsApplication.config.grails.assets
        def assetPath = assetProcessorService.assetPath
		def assetUrl = conf.url ?: "$assetPath/"

		if(conf.precompiled) {
			def realPath = conf.manifest.getProperty(src)
			if(realPath) {
				return "${assetUrl}${realPath}"
			}
		}
		return "${assetUrl}${src}"
	}
}
