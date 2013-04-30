class AssetUrlMappings {

	static mappings = {

		"/assets/$id**" (
			controller: 'assets',
			action: 'index'
		)
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
