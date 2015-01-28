import org.apache.tools.ant.DirectoryScanner
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
// import asset.pipeline.*
includeTargets << grailsScript("_PackagePlugins")
includeTargets << grailsScript("_GrailsBootstrap")

target(assetClean: "Cleans Compiled Assets Directory") {
	// Clear compiled assets folder
	println "Asset Precompiler Args ${argsMap}"
	def assetDir = new File(argsMap.target ?: "target/assets")
	if(assetDir.exists()) {
		assetDir.deleteDir()
	}
}

target(assetCompile: "Precompiles assets in the application as specified by the precompile glob!") {
	depends(configureProxy,compile)
	def assetPipelineConfigHolder   = classLoader.loadClass('asset.pipeline.AssetPipelineConfigHolder')
	def defaultResourceLoader       = classLoader.loadClass('org.springframework.core.io.DefaultResourceLoader').newInstance(classLoader)
	def fileSystemAssetResolver     = classLoader.loadClass('asset.pipeline.fs.FileSystemAssetResolver')
	def springResourceAssetResolver = classLoader.loadClass('asset.pipeline.grails.fs.SpringResourceAssetResolver')
	def jarAssetResolver            = classLoader.loadClass('asset.pipeline.fs.JarAssetResolver')
	def assetHelper                 = classLoader.loadClass('asset.pipeline.AssetHelper')
	def assetCompilerClass          = classLoader.loadClass('asset.pipeline.AssetCompiler')
	def directiveProcessorClass     = classLoader.loadClass('asset.pipeline.DirectiveProcessor')

	def assetConfig                = [specs:[]] //Additional Asset Specs (Asset File formats) that we want to process.

	event("AssetPrecompileStart", [assetConfig])
	assetConfig.minifyJs = config.grails.assets.containsKey('minifyJs') ? config.grails.assets.minifyJs : (argsMap.containsKey('minifyJs') ? argsMap.minifyJs == 'true' : true)
	assetConfig.minifyCss = config.grails.assets.containsKey('minifyCss') ? config.grails.assets.minifyCss : (argsMap.containsKey('minifyCss') ? argsMap.minifyCss == 'true' : true)
	assetConfig.minifyOptions = config.grails.assets.minifyOptions
	assetConfig.compileDir = "${basedir}/target/assets"
	assetConfig.excludesGzip = config.grails.assets.excludesGzip

	//Add Resolvers for Grails
	assetPipelineConfigHolder.registerResolver(fileSystemAssetResolver.newInstance('application','grails-app/assets'))
	// for(plugin in pluginManager.getAllPlugins()) {
	// 	if(plugin instanceof org.codehaus.groovy.grails.plugins.BinaryGrailsPlugin) {
	// 		def descriptorURI = plugin.binaryDescriptor.resource.URI
	// 		descriptorURI = new java.net.URI( new java.net.URI(descriptorURI.getSchemeSpecificPart()).getSchemeSpecificPart()).toString().split("!")[0]

	// 		assetPipelineConfigHolder.registerResolver(jarAssetResolver.newInstance(plugin.name,descriptorURI,'META-INF/assets'))
	// 		assetPipelineConfigHolder.registerResolver(jarAssetResolver.newInstance(plugin.name,descriptorURI,'META-INF/static'))
	// 	}

	// }
	for(plugin in GrailsPluginUtils.pluginInfos) {
		def assetPath = [plugin.pluginDir.getPath(), "grails-app", "assets"].join(File.separator)
		def fallbackPath = [plugin.pluginDir.getPath(), "web-app"].join(File.separator)
		assetPipelineConfigHolder.registerResolver(fileSystemAssetResolver.newInstance(plugin.name,assetPath))
		assetPipelineConfigHolder.registerResolver(fileSystemAssetResolver.newInstance(plugin.name,fallbackPath,true))
	}

	assetPipelineConfigHolder.registerResolver(springResourceAssetResolver.newInstance('classpath',defaultResourceLoader,'META-INF/assets'))
	assetPipelineConfigHolder.registerResolver(springResourceAssetResolver.newInstance('classpath',defaultResourceLoader,'META-INF/static'))
	assetPipelineConfigHolder.registerResolver(springResourceAssetResolver.newInstance('classpath',defaultResourceLoader,'META-INF/resources'))

	assetPipelineConfigHolder.config = config.grails.assets

	event("StatusUpdate",["Precompiling Assets!"])

	def assetCompiler = assetCompilerClass.newInstance(assetConfig + [compileDir: "${basedir}/target/assets", classLoader: classLoader],  eventListener)

	assetCompiler.excludeRules.default = config.grails.assets.excludes
	assetCompiler.includeRules.default = config.grails.assets.includes

	// Initialize Exclude/Include Rules
	config.grails.assets.plugin.each { pluginName, value ->

		if(value.excludes) {
			assetCompiler.excludeRules[pluginName] = value.excludes
		}
		if(value.includes) {
			assetCompiler.includeRules[pluginName] = value.includes
		}
	}
	assetCompiler.compile()
	event("AssetPrecompileComplete", [assetConfig])

}
