import org.codehaus.groovy.grails.plugins.GrailsPluginInfo
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils


includeTargets << grailsScript('_PackagePlugins')
includeTargets << grailsScript('_GrailsBootstrap')

target(assetClean: 'Cleans Compiled Assets Directory') {
	// Clear compiled assets folder
	println "Asset Precompiler Args ${argsMap}"
	final File assetDir = new File(argsMap.target ?: 'target/assets')
	if (assetDir.exists()) {
		assetDir.deleteDir()
	}
}

target(assetCompile: 'Precompiles assets in the application as specified by the precompile glob!') {
	depends(configureProxy, compile)

	final Class<?> assetPipelineConfigHolder = classLoader.loadClass('asset.pipeline.AssetPipelineConfigHolder')
	final def      defaultResourceLoader     = classLoader.loadClass('org.springframework.core.io.DefaultResourceLoader').newInstance(classLoader)
	final Class<?> fileSystemAssetResolver   = classLoader.loadClass('asset.pipeline.fs.FileSystemAssetResolver')
	final Class<?> jarAssetResolver          = classLoader.loadClass('asset.pipeline.fs.JarAssetResolver')
	final Class<?> assetHelper               = classLoader.loadClass('asset.pipeline.AssetHelper')
	final Class<?> assetCompilerClass        = classLoader.loadClass('asset.pipeline.AssetCompiler')
	final Class<?> directiveProcessorClass   = classLoader.loadClass('asset.pipeline.DirectiveProcessor')

	final Map<String, Object> assetConfig = [specs: []] // Additional Asset Specs (Asset File formats) to process

	final ConfigObject grailsConfig = config.grails.assets

	assetPipelineConfigHolder.config = grailsConfig

	grailsConfig.cacheLocation = 'target/.asscache'

	event('AssetPrecompileStart', [assetConfig])

	assetConfig.minifyJs         = grailsConfig.containsKey('minifyJs')         ? grailsConfig.minifyJs  : (argsMap.containsKey('minifyJs')  ? argsMap.minifyJs  == 'true' : true)
	assetConfig.minifyCss        = grailsConfig.containsKey('minifyCss')        ? grailsConfig.minifyCss : (argsMap.containsKey('minifyCss') ? argsMap.minifyCss == 'true' : true)
	assetConfig.minifyOptions    = grailsConfig.minifyOptions
	assetConfig.compileDir       = "${basedir}/target/assets"
	assetConfig.enableGzip       = grailsConfig.enableGzip
	assetConfig.excludesGzip     = grailsConfig.excludesGzip
	assetConfig.enableSourceMaps = grailsConfig.containsKey('enableSourceMaps') ? grailsConfig.enableSourceMaps : true
	assetConfig.skipNonDigests   = grailsConfig.containsKey('skipNonDigests')   ? grailsConfig.skipNonDigests   : true
	assetConfig.enableDigests    = grailsConfig.containsKey('enableDigests')    ? grailsConfig.enableDigests    : true

	// Add Resolvers for Grails
	assetPipelineConfigHolder.registerResolver(fileSystemAssetResolver.newInstance('application', "${basedir}/grails-app/assets"))

	for (final GrailsPluginInfo plugin in GrailsPluginUtils.pluginInfos) {
		final String assetPath    = [plugin.pluginDir.getPath(), 'grails-app', 'assets'].join(File.separator)
		final String fallbackPath = [plugin.pluginDir.getPath(), 'web-app'].join(File.separator)
		assetPipelineConfigHolder.registerResolver(fileSystemAssetResolver.newInstance(plugin.name, assetPath))
		assetPipelineConfigHolder.registerResolver(fileSystemAssetResolver.newInstance(plugin.name, fallbackPath, true))
	}

	grailsSettings.runtimeDependencies.each { final File dep ->
		if (dep.name.endsWith('.jar')) {
			assetPipelineConfigHolder.registerResolver(jarAssetResolver.newInstance(dep.name, dep.path, 'META-INF/assets'))
			assetPipelineConfigHolder.registerResolver(jarAssetResolver.newInstance(dep.name, dep.path, 'META-INF/static'))
			assetPipelineConfigHolder.registerResolver(jarAssetResolver.newInstance(dep.name, dep.path, 'META-INF/resources'))
		}
	}

	grailsSettings.providedDependencies.each { final File dep ->
		if (dep.name.endsWith('.jar')) {
			assetPipelineConfigHolder.registerResolver(jarAssetResolver.newInstance(dep.name, dep.path, 'META-INF/assets'))
			assetPipelineConfigHolder.registerResolver(jarAssetResolver.newInstance(dep.name, dep.path, 'META-INF/static'))
			assetPipelineConfigHolder.registerResolver(jarAssetResolver.newInstance(dep.name, dep.path, 'META-INF/resources'))
		}
	}

	event('StatusUpdate', ['Precompiling Assets!'])

	final def assetCompiler = assetCompilerClass.newInstance(assetConfig + [compileDir: "${basedir}/target/assets", classLoader: classLoader], eventListener)

	assetCompiler.excludeRules.default = grailsConfig.excludes
	assetCompiler.includeRules.default = grailsConfig.includes

	// Initialize Exclude/Include Rules
	grailsConfig.plugin.each { final pluginName, final value ->
		if (value.excludes) {
			assetCompiler.excludeRules[pluginName] = value.excludes
		}
		if (value.includes) {
			assetCompiler.includeRules[pluginName] = value.includes
		}
	}
	assetCompiler.compile()
	event('AssetPrecompileComplete', [assetConfig])
}
