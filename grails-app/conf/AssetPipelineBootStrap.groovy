import asset.pipeline.AssetPipelineConfigHolder
import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource


class AssetPipelineBootStrap {

	def grailsApplication


	def init = {final servletContext ->
		final def storagePath = grailsApplication.config.grails.assets.storagePath
		if (! storagePath) {
			return
		}

		final Properties manifest = AssetPipelineConfigHolder.manifest

		if (manifest) {
			final File storageDir = new File((String) storagePath)
			storageDir.mkdirs()

			final ApplicationContext parentContext = grailsApplication.parentContext

			manifest.stringPropertyNames().each {final String propertyName ->
				final File outputFile = new File(storageDir, propertyName)

				new File(outputFile.parent).mkdirs()

				final String propertyValue = manifest.getProperty(propertyName)

				final String assetPath = "assets/${propertyValue}"

				final byte[] fileBytes = parentContext.getResource(assetPath).inputStream.bytes

				outputFile.bytes = fileBytes

				new File(storageDir, propertyValue).bytes = fileBytes

				final Resource gzRes = parentContext.getResource("${assetPath}.gz")
				if (gzRes.exists()) {
					final byte[] gzBytes = gzRes.inputStream.bytes

					new File(storageDir, "${propertyName}.gz" ).bytes = gzBytes
					new File(storageDir, "${propertyValue}.gz").bytes = gzBytes
				}
			}

			manifest.store(new File(storageDir, 'manifest.properties').newWriter(), '')
		}
	}
}
