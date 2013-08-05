import org.springframework.context.ApplicationContext

class AssetUrlMappings {

	static mappings = {ApplicationContext context ->
		def grailsApplication
		def path = context.assetProcessorService.assetMapping

		"/$path/$id**" (
			controller: 'assets',
			action: 'index'
		)
	}
}
