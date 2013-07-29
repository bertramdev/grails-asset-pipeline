package asset.pipeline
import grails.util.GrailsUtil

class AssetsTagLib {
	static namespace = "asset"
	def grailsApplication
	def assetProcessorService

	def javascript = { attrs ->
		def src       = attrs['src']
		def uri       = null
		def extension = null

		if((!grailsApplication.config.grails.assets.containsKey('bundle') && GrailsUtil.environment != 'development') || grailsApplication.config.grails.assets.bundle == true) {
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

	}

	private def assetPath(src) {
		def assetUrl = grailsApplication.config.grails.assets.url ?: "/assets/"

		if(grailsApplication.config.grails.assets.precompiled) {
			def realPath = grailsApplication.config.grails.assets.manifest.getProperty(src)
			if(realPath) {
				return "${assetUrl}${realPath}"
			}
		}
		return "${assetUrl}${src}"
	}
}
