package asset.pipeline.grails

import grails.core.support.GrailsApplicationAware
import grails.core.GrailsApplication
import asset.pipeline.AssetHelper
import asset.pipeline.AssetPipelineConfigHolder
import groovy.util.logging.Commons

@Commons
class CachingLinkGenerator extends org.grails.web.mapping.CachingLinkGenerator implements GrailsApplicationAware {
	GrailsApplication grailsApplication
	def assetProcessorService

	CachingLinkGenerator(final String serverUrl) {
		super(serverUrl)
	}

	String resource(final Map attrs) {
		asset(attrs) ?: super.resource(attrs)
	}

	/**
	 * Finds an Asset from the asset-pipeline based on the file attribute.
	 * @param attrs [file]
	 */
	String asset(final Map attrs) {
		assetProcessorService.asset(attrs, this)
	}
}
