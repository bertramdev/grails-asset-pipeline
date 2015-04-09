package asset.pipeline.grails

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware
import org.codehaus.groovy.grails.web.mapping.DefaultLinkGenerator
import asset.pipeline.AssetHelper
import groovy.util.logging.Slf4j

@Slf4j
class LinkGenerator extends DefaultLinkGenerator implements GrailsApplicationAware {
	GrailsApplication grailsApplication
	def assetProcessorService

	LinkGenerator(String serverUrl) {
		super(serverUrl)
	}

	String resource(Map attrs) {
		return asset(attrs) ?: super.resource(attrs)
	}

	/**
	 * Finds an Asset from the asset-pipeline based on the file attribute.
	 * @param attrs [file]
	 */
	String asset(Map attrs) {
		def absolutePath = handleAbsolute(attrs)

		def absolute = attrs[DefaultLinkGenerator.ATTRIBUTE_ABSOLUTE]
		def conf = grailsApplication.config.grails.assets
		def url  = attrs.file ?: attrs.src
		def assetFound = false

		if(url) {
			if(conf.precompiled) {
				def realPath = conf.manifest.getProperty(url)
				if(realPath) {
					url = assetUriRootPath() + realPath
					assetFound = true
				}
			} else {
				def assetFile = AssetHelper.fileForFullName(url)
				if(assetFile != null) {
					url = assetUriRootPath() + url
					assetFound = true
				}
			}
		}

		if(!assetFound) {
			return null
		} else {
			if(!url?.startsWith('http')) {
				final contextPathAttribute = attrs.contextPath?.toString()
				if(absolutePath == null) {
					final cp = contextPathAttribute == null ? getContextPath() : contextPathAttribute
					if(cp == null) {
						absolutePath = handleAbsolute(absolute:true)
					} else {
						absolutePath = cp
					}
				}
				url = (absolutePath?:'') + (url ?: '')
			}
			return url
		}
	}

	private assetUriRootPath() {
		def context = grailsApplication.mainContext
		def conf    = grailsApplication.config.grails.assets
		def mapping = assetProcessorService.assetMapping
		if(conf.url && conf.url instanceof Closure) {
			return conf.url.call(null)
		} else {
			return conf.url ?: "/$mapping/"
		}
	}

}
