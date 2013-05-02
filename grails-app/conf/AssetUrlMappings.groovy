class AssetUrlMappings {

	static mappings = {
		"/assets/$id**" (
			controller: 'assets',
			action: 'index'
		)
	}
}
