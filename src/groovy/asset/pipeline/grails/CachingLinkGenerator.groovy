package asset.pipeline.grails


import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.support.aware.GrailsApplicationAware


@Slf4j
class CachingLinkGenerator extends org.codehaus.groovy.grails.web.mapping.CachingLinkGenerator implements GrailsApplicationAware {

	GrailsApplication grailsApplication
	def assetProcessorService


	CachingLinkGenerator(String serverUrl) {
		super(serverUrl)
	}


	String resource(Map attrs) {
		asset(attrs) ?: super.resource(attrs)
	}

	/**
	 * Finds an Asset from the asset-pipeline based on the file attribute.
	 * @param attrs [file]
	 */
	String asset(Map attrs) {
		assetProcessorService.asset(attrs, this)
	}
}
