import org.codehaus.groovy.grails.plugins.GrailsPluginUtils


includeTargets << grailsScript("_PackagePlugins")
includeTargets << grailsScript("_GrailsBootstrap")

target(assetClean: "Cleans Compiled Assets Directory") {
	// Clear compiled assets folder
	println "Asset Precompiler Args ${argsMap}"
	final def assetDir = new File(argsMap.target ?: "target/assets")
	if (assetDir.exists()) {
		assetDir.deleteDir()
	}
}

target(assetCompile: "Precompiles assets in the application as specified by the precompile glob!") {
	depends(configureProxy,compile)

	final def assetPipelineConfigHolder = classLoader.loadClass('asset.pipeline.AssetPipelineConfigHolder')
	final def defaultResourceLoader     = classLoader.loadClass('org.springframework.core.io.DefaultResourceLoader').newInstance(classLoader)
	final def fileSystemAssetResolver   = classLoader.loadClass('asset.pipeline.fs.FileSystemAssetResolver')
	final def jarAssetResolver          = classLoader.loadClass('asset.pipeline.fs.JarAssetResolver')
	final def assetHelper               = classLoader.loadClass('asset.pipeline.AssetHelper')
	final def assetCompilerClass        = classLoader.loadClass('asset.pipeline.AssetCompiler')
	final def directiveProcessorClass   = classLoader.loadClass('asset.pipeline.DirectiveProcessor')

	final def assetConfig               = [specs:[]] //Additional Asset Specs (Asset File formats) that we want to process.
	assetPipelineConfigHolder.config = config.grails.assets
	assetPipelineConfigHolder.config.cacheLocation = "target/.asscache"
	event("AssetPrecompileStart", [assetConfig])

	assetConfig.minifyJs         = config.grails.assets.containsKey('minifyJs')         ? config.grails.assets.minifyJs  : (argsMap.containsKey('minifyJs')  ? argsMap.minifyJs  == 'true' : true)
	assetConfig.minifyCss        = config.grails.assets.containsKey('minifyCss')        ? config.grails.assets.minifyCss : (argsMap.containsKey('minifyCss') ? argsMap.minifyCss == 'true' : true)
	assetConfig.minifyOptions    = config.grails.assets.minifyOptions
	assetConfig.compileDir       = "${basedir}/target/assets"
	assetConfig.enableGzip       = config.grails.assets.enableGzip
	assetConfig.excludesGzip     = config.grails.assets.excludesGzip
	assetConfig.enableSourceMaps = config.grails.assets.containsKey('enableSourceMaps') ? config.grails.assets.enableSourceMaps : true
	assetConfig.skipNonDigests   = config.grails.assets.containsKey('skipNonDigests')   ? config.grails.assets.skipNonDigests   : true
	assetConfig.enableDigests    = config.grails.assets.containsKey('enableDigests')    ? config.grails.assets.enableDigests    : true

	//Add Resolvers for Grails
	assetPipelineConfigHolder.registerResolver(fileSystemAssetResolver.newInstance('application', "${basedir}/grails-app/assets"))

	for (final plugin in GrailsPluginUtils.pluginInfos) {
		def assetPath    = [plugin.pluginDir.getPath(), "grails-app", "assets"].join(File.separator)
		def fallbackPath = [plugin.pluginDir.getPath(), "web-app"].join(File.separator)
		assetPipelineConfigHolder.registerResolver(fileSystemAssetResolver.newInstance(plugin.name, assetPath))
		assetPipelineConfigHolder.registerResolver(fileSystemAssetResolver.newInstance(plugin.name, fallbackPath, true))
	}

	grailsSettings.runtimeDependencies.each { final dep ->
		if (dep.name.endsWith('.jar')) {
			assetPipelineConfigHolder.registerResolver(jarAssetResolver.newInstance(dep.name, dep.path, 'META-INF/assets'))
			assetPipelineConfigHolder.registerResolver(jarAssetResolver.newInstance(dep.name, dep.path, 'META-INF/static'))
			assetPipelineConfigHolder.registerResolver(jarAssetResolver.newInstance(dep.name, dep.path, 'META-INF/resources'))
		}
	}

	grailsSettings.providedDependencies.each { final dep ->
		if (dep.name.endsWith('.jar')) {
			assetPipelineConfigHolder.registerResolver(jarAssetResolver.newInstance(dep.name, dep.path, 'META-INF/assets'))
			assetPipelineConfigHolder.registerResolver(jarAssetResolver.newInstance(dep.name, dep.path, 'META-INF/static'))
			assetPipelineConfigHolder.registerResolver(jarAssetResolver.newInstance(dep.name, dep.path, 'META-INF/resources'))
		}
	}

	event("StatusUpdate", ["Precompiling Assets!"])

	final def assetCompiler = assetCompilerClass.newInstance(assetConfig + [compileDir: "${basedir}/target/assets", classLoader: classLoader],  eventListener)

	assetCompiler.excludeRules.default = config.grails.assets.excludes
	assetCompiler.includeRules.default = config.grails.assets.includes

	// Initialize Exclude/Include Rules
	config.grails.assets.plugin.each { final pluginName, final value ->
		if (value.excludes) {
			assetCompiler.excludeRules[pluginName] = value.excludes
		}
		if (value.includes) {
			assetCompiler.includeRules[pluginName] = value.includes
		}
	}
	assetCompiler.compile()
	event("AssetPrecompileComplete", [assetConfig])
}
