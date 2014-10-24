target(assetPrecompile: "Precompiles assets in the application as specified by the precompile glob!") {
	if(argsMap.target) {
		event("StatusError",["This script is no longer necessary! Simply run grails war to generate your assets into your war file!"])
	} else {
		includeTargets << grailsScript("_GrailsBootstrap")
		includeTargets << new File(assetPipelinePluginDir, "scripts/_AssetCompile.groovy")

		depends(configureProxy, compile, packageApp)

		assetCompile()
	}
}

setDefaultTarget(assetPrecompile)
