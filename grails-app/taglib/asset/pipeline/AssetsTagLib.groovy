package asset.pipeline

class AssetsTagLib {
	static namespace = "asset"
	def grailsApplication

	def javascript = { attrs ->
		def src = attrs['src']
		out << "<script src=\"${assetPath(src)}\" type=\"text/javascript\"></script>"

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
