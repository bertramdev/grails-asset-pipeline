package asset.pipeline.grails


import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.web.mapping.DefaultLinkGenerator


@Slf4j
class LinkGenerator extends DefaultLinkGenerator {

	def assetProcessorService
	def grailsApplication


	LinkGenerator(final String serverUrl) {
		super(serverUrl)
	}


	@Override
	String resource(final Map attrs) {
		if (! grailsApplication.config.grails.assets.useGrailsResourceMethod) {
			final String url = asset(attrs)
			if (url) {
				return url
			}
		}

		super.resource(attrs)
	}

	/**
	 * Finds an Asset from the asset-pipeline based on the file attribute.
	 * @param attrs [file]
	 */
	String asset(final Map attrs) {
		assetProcessorService.asset(attrs, this)
	}

	@Override
	String makeServerURL() {
		assetProcessorService.makeServerURL(this)
	}
}
