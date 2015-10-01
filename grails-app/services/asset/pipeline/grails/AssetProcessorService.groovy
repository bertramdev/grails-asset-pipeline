package asset.pipeline.grails


import javax.servlet.http.HttpServletRequest
import org.codehaus.groovy.grails.web.mapping.DefaultLinkGenerator
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest

import static asset.pipeline.AssetHelper.fileForFullName
import static asset.pipeline.AssetPipelineConfigHolder.manifest
import static asset.pipeline.grails.utils.net.HttpServletRequests.getBaseUrlWithScheme
import static asset.pipeline.grails.utils.text.StringBuilders.ensureEndsWith
import static asset.pipeline.utils.net.Urls.hasAuthority
import static grails.util.Environment.isWarDeployed
import static org.apache.commons.lang.StringUtils.trimToEmpty
import static org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest.lookup


class AssetProcessorService {

	static transactional = false


	def grailsApplication
	def grailsLinkGenerator


	/**
	 * Retrieves the asset path from the property [grails.assets.mapping] which is used by the url mapping and the
	 * taglib.  The property cannot contain <code>/</code>, and must be one level deep
	 *
	 * @return the path
	 * @throws IllegalArgumentException if the path contains <code>/</code>
	 */
	String getAssetMapping() {
		final def mapping = grailsApplication.config?.grails?.assets?.mapping ?: 'assets'
		if (mapping.contains('/')) {
			throw new IllegalArgumentException(
				'The property [grails.assets.mapping] can only be one level deep.  ' +
				"For example, 'foo' and 'bar' would be acceptable values, but 'foo/bar' is not"
			)
		}
		return mapping
	}


	boolean isEnableDigests(final ConfigObject conf = grailsApplication.config.grails.assets) {
		conf.containsKey('enableDigests') ? conf.enableDigests : true
	}

	boolean isSkipNonDigests(final ConfigObject conf = grailsApplication.config.grails.assets) {
		conf.containsKey('skipNonDigests') ? conf.skipNonDigests : true
	}


	String getAssetPath(final String path, final ConfigObject conf = grailsApplication.config.grails.assets) {
		final String relativePath = trimLeadingSlash(path)
		relativePath && manifest && isEnableDigests(conf) \
			? manifest.getProperty(relativePath) ?: relativePath
			: relativePath
	}


	String getResolvedAssetPath(final String path, final ConfigObject conf = grailsApplication.config.grails.assets) {
		final String relativePath = trimLeadingSlash(path)
		relativePath \
			? manifest \
				? isEnableDigests(conf) \
					? manifest.getProperty(relativePath)
					: manifest.getProperty(relativePath) \
						? relativePath
						: null
				: fileForFullName(relativePath) != null \
					? relativePath
					: null
			: null
	}


	boolean isAssetPath(final String path) {
		final String relativePath = trimLeadingSlash(path)
		relativePath &&
		(
			manifest \
				? manifest.getProperty(relativePath)
				: fileForFullName(relativePath) != null
		)
	}


	String asset(final Map attrs, final DefaultLinkGenerator linkGenerator) {
		String url = getResolvedAssetPath(attrs.file ?: attrs.src)

		if (! url) {
			return null
		}

		url = assetBaseUrl(null, false) + url

		if (! hasAuthority(url)) {
			def absolutePath = linkGenerator.handleAbsolute(attrs)

			if (absolutePath == null) {
				final String contextPathAttribute = attrs.contextPath?.toString()

				final String contextPath =
					contextPathAttribute == null \
						? linkGenerator.contextPath
						: contextPathAttribute

				absolutePath =
					contextPath == null \
						? linkGenerator.handleAbsolute(absolute: true) ?: ''
						: contextPath
			}

			url = absolutePath + url
		}

		return url
	}

	String getConfigBaseUrl(final HttpServletRequest req, final ConfigObject conf = grailsApplication.config.grails.assets) {
		final def url = conf.url
		url instanceof Closure \
			? url(req)
			: url \
				? url
				: null
	}

	String assetBaseUrl(final HttpServletRequest req, final boolean prependServerBaseUrlIfNoAssetBaseUrlSet, final ConfigObject conf = grailsApplication.config.grails.assets) {
		final String url = getConfigBaseUrl(req, conf)
		if (url) {
			return url
		}

		final String mapping = assetMapping

		final String baseUrl =
			prependServerBaseUrlIfNoAssetBaseUrlSet \
				? grailsLinkGenerator.serverBaseURL ?: ''
				: trimToEmpty(grailsLinkGenerator.contextPath)

		return \
			ensureEndsWith(
				new StringBuilder(baseUrl.length() + mapping.length() + 2).append(baseUrl),
				'/' as char
			)
				.append(mapping)
				.append('/' as char)
				.toString()
	}


	String makeServerURL(final DefaultLinkGenerator linkGenerator) {
		String serverUrl = linkGenerator.configuredServerBaseURL
		if (! serverUrl) {
			final GrailsWebRequest req = lookup()
			if (req) {
				serverUrl = getBaseUrlWithScheme(req.currentRequest).toString()
				if (! serverUrl && ! warDeployed) {
					serverUrl = "http://localhost:${System.getProperty('server.port') ?: '8080'}${linkGenerator.contextPath ?: ''}"
				}
			}
		}
		return serverUrl
	}


	private static String trimLeadingSlash(final String s) {
		! s || s.charAt(0) != '/' as char \
			? s
			: s.substring(1)
	}
}
