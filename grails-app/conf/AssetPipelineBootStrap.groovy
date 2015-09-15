import asset.pipeline.AssetPipelineConfigHolder
import com.grailsrocks.jqueryui.JqueryUiTagLib
import java.util.Map.Entry
import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest
import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.plugin.resource.ResourceTagLib
import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource

import static asset.pipeline.grails.utils.net.HttpServletRequests.getBaseUrlWithScheme
import static org.codehaus.groovy.grails.web.util.WebUtils.retrieveGrailsWebRequest


class AssetPipelineBootStrap {

	private static final int SLASH = (int) '/' as char


	def assetProcessorService
	def grailsApplication
	def pluginManager


	def init = {final ServletContext servletContext ->
		wrapLinkWriters((Map<String, Closure<String>>) ApplicationTagLib.LINK_WRITERS, '/plugins/')
		wrapLinkWriters((Map<String, Closure<String>>)    JqueryUiTagLib.LINK_WRITERS, '/plugins/')
		wrapLinkWriters((Map<String, Closure<String>>)    ResourceTagLib.LINK_WRITERS, "/${pluginManager.getGrailsPlugin('resources').instance.getUriPrefix(grailsApplication)}/plugins/")

		final ConfigObject conf = grailsApplication.config.grails.assets

		final def storagePath = conf.storagePath
		if (! storagePath) {
			return
		}

		final Properties manifest = AssetPipelineConfigHolder.manifest

		if (manifest) {
			final boolean enableDigests  = assetProcessorService.isEnableDigests(conf)
			final boolean skipNonDigests = assetProcessorService.isSkipNonDigests(conf)

			if (enableDigests || ! skipNonDigests) {
				final File storageDir = new File((String) storagePath)
				storageDir.mkdirs()

				final ApplicationContext parentContext = grailsApplication.parentContext

				manifest.stringPropertyNames().each {final String propertyName ->
					final File outputFile = new File(storageDir, propertyName)

					new File(outputFile.parent).mkdirs()

					final String propertyValue = manifest.getProperty(propertyName)

					final String assetPath = "assets/${enableDigests ? propertyValue : propertyName}"

					final byte[] fileBytes = parentContext.getResource(assetPath).inputStream.bytes

					if (! skipNonDigests) {
						outputFile.bytes = fileBytes
					}

					if (enableDigests) {
						new File(storageDir, propertyValue).bytes = fileBytes
					}

					final Resource gzRes = parentContext.getResource("${assetPath}.gz")
					if (gzRes.exists()) {
						final byte[] gzBytes = gzRes.inputStream.bytes

						if (! skipNonDigests) {
							new File(storageDir, "${propertyName}.gz" ).bytes = gzBytes
						}

						if (enableDigests) {
							new File(storageDir, "${propertyValue}.gz").bytes = gzBytes
						}
					}
				}

				manifest.store(new File(storageDir, 'manifest.properties').newWriter(), '')
			}
		}
	}


	private static void wrapLinkWriters(final Map<String, Closure<String>> linkWriterByName, final String removeUriPrefix) {
		for (final Entry<String, Closure<String>> linkWriterForName : linkWriterByName.entrySet()) {
			linkWriterForName.value = wrapLinkWriter(linkWriterForName.value, removeUriPrefix)
		}
	}

	private static Closure<String> wrapLinkWriter(final Closure<String> linkWriter, final String removeUriPrefix) {
		{final String url, final Map<String, String> constants, final Map<String, ?> attrs ->
			final int indexSlash3
			final int indexAfterSlash3
			final int indexSlash4

			final String assetUrl =
				url.startsWith(removeUriPrefix) &&
				(indexSlash3      = url.indexOf(SLASH, removeUriPrefix.length())) != -1 &&
				(indexAfterSlash3 = indexSlash3 + 1)                              != url.length() &&
				(indexSlash4      = url.indexOf(SLASH, indexAfterSlash3))         != -1 \
					? url.substring(indexSlash4 + 1)
					: url

			final HttpServletRequest req = retrieveGrailsWebRequest().currentRequest

			final String assetPipelineBaseUrl
			linkWriter(
				(
					assetProcessorService.isAssetPath(assetUrl) &&
					(assetPipelineBaseUrl = assetProcessorService.getConfigBaseUrl(req)) != null
						? assetPipelineBaseUrl + assetUrl
						: getBaseUrlWithScheme(req).append(url).toString()
				),
				constants,
				attrs
			)
		}
	}
}
