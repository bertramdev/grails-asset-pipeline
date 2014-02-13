import org.apache.tools.ant.DirectoryScanner
import org.codehaus.groovy.grails.commons.ApplicationHolder

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

  def assetHelper             = classLoader.loadClass('asset.pipeline.AssetHelper')
  def assetCompilerClass      = classLoader.loadClass('asset.pipeline.AssetCompiler')
  def directiveProcessorClass = classLoader.loadClass('asset.pipeline.DirectiveProcessor')
  def assetConfig             = [specs:[]] //Additional Asset Specs (Asset File formats) that we want to process.
  def grailsApplication       = ApplicationHolder.getApplication()
  event("AssetPrecompileStart", [assetConfig])

  assetConfig.minifyJs = grailsApplication.config.grails.assets.containsKey('minifyJs') ? grailsApplication.config.grails.assets.minifyJs : (argsMap.containsKey('minifyJs') ? argsMap.minifyJs == 'true' : true)
  assetConfig.minifyOptions = grailsApplication.config.grails.assets.minifyOptions
  assetConfig.compileDir = "target/assets"
  assetConfig.excludesGzip = grailsApplication.config.grails.assets.excludesGzip



  event("StatusUpdate",["Precompiling Assets!"])

  def assetCompiler = assetCompilerClass.newInstance(assetConfig, eventListener)

  assetCompiler.assetPaths = assetHelper.getAssetPathsByPlugin()
  assetCompiler.excludeRules.default = grailsApplication.config.grails.assets.excludes
  assetCompiler.includeRules.default = grailsApplication.config.grails.assets.includes

	// Initialize Exclude/Include Rules
  grailsApplication.config.grails.assets.plugin.each { pluginName, value ->

  	if(value.excludes) {
  		assetCompiler.excludeRules[pluginName] = value.excludes
  	}
  	if(value.includes) {
  		assetCompiler.includeRules[pluginName] = value.includes
  	}
  }
  assetCompiler.compile()
}

