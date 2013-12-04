import org.springframework.context.ApplicationContext

class AssetUrlMappings {

	static mappings = {ApplicationContext context ->
		def grailsApplication
		def path = context?.assetProcessorService?.assetMapping ?: 'assets'

		"/$path/$id**" (
			controller: 'assets',
			plugin: 'assetPipeline',
			action: 'index'
		)
	}
}
