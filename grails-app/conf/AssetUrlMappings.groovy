import org.springframework.context.ApplicationContext

class AssetUrlMappings {

	static mappings = {ApplicationContext context ->

        def path = context.assetProcessorService.assetPath

        "/$path/$id**" (
			controller: 'assets',
			action: 'index'
		)
	}
}
