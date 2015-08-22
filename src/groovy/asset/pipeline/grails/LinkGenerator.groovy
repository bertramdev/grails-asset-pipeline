package asset.pipeline.grails


import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.web.mapping.DefaultLinkGenerator


@Slf4j
class LinkGenerator extends DefaultLinkGenerator {

	def assetProcessorService


	LinkGenerator(final String serverUrl) {
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
