includeTargets << grailsScript("_GrailsBootstrap")

includeTargets << new File(assetPipelinePluginDir, "scripts/_AssetCompile.groovy")

target(assetPrecompile: "Precompiles assets in the application as specified by the precompile glob!") {
   assetCompile()
}

setDefaultTarget(assetPrecompile)
