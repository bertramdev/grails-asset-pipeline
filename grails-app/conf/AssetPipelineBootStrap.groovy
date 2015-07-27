import asset.pipeline.AssetPipelineConfigHolder
import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource


class AssetPipelineBootStrap {

	def grailsApplication


	def init = {final servletContext ->
		final String storagePath = grailsApplication.config.grails.assets.storagePath
		if (!storagePath) {
			return
		}

		final Properties manifest = AssetPipelineConfigHolder.manifest

		if (manifest) {
			new File(storagePath).mkdirs()

			final ApplicationContext parentContext = grailsApplication.parentContext

			manifest.stringPropertyNames().each {final String propertyName ->
				final File outputFile = new File(storagePath, propertyName)

				new File(outputFile.parent).mkdirs()

				final String propertyValue = manifest.getProperty(propertyName)

				final String assetPath = "assets/${propertyValue}"

				final byte[] fileBytes = parentContext.getResource(assetPath).inputStream.bytes

				outputFile.bytes = fileBytes

				new File(storagePath, propertyValue).bytes = fileBytes

				final Resource gzRes = parentContext.getResource("${assetPath}.gz")
				if (gzRes.exists()) {
					final byte[] gzBytes = gzRes.inputStream.bytes

					new File(storagePath, "${propertyName}.gz" ).bytes = gzBytes
					new File(storagePath, "${propertyValue}.gz").bytes = gzBytes
				}
			}

			manifest.store(new File(storagePath, 'manifest.properties').newWriter(), '')
		}
	}
}
