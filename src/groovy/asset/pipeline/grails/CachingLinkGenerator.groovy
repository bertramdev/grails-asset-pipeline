package asset.pipeline.grails


import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest

import static asset.pipeline.grails.utils.net.HttpServletRequests.getBaseUrlWithScheme
import static org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest.lookup


@Slf4j
class CachingLinkGenerator extends org.codehaus.groovy.grails.web.mapping.CachingLinkGenerator {

	def assetProcessorService


	CachingLinkGenerator(final String serverUrl) {
		super(serverUrl)
	}


	@Override
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

	@Override
	String makeServerURL() {
		assetProcessorService.makeServerURL(this)
	}

	@Override
	protected String makeKey(final String prefix, final Map attrs) {
		final StringBuilder sb = new StringBuilder()
		sb.append(prefix)
		if (configuredServerBaseURL == null && isAbsolute(attrs)) {
			final Object base = attrs.base
			if (base != null) {
				sb.append(base)
			} else {
				final GrailsWebRequest req = lookup()
				if (req != null) {
					sb.append(getBaseUrlWithScheme(req.currentRequest).toString())
				}
			}
		}
		appendMapKey(sb, attrs)
		sb.toString()
	}
}
