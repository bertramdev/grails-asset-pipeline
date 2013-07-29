import grails.util.Environment

import org.codehaus.groovy.grails.plugins.PluginManagerHolder

import asset.pipeline.AssetFileArtefactHandler

class AssetPipelineGrailsPlugin {
    def version = "0.1.0"
    def grailsVersion = "2.0 > *"
    def title = "Asset Pipeline Plugin"
    def author = "David Estes"
    def authorEmail = "destes@bcap.com"
    def description = 'The asset-pipeline plugin is a port from the rails asset-pipeline into the Grails world. It allows similar require directives within the grails-app/assets folder.'

    def documentation = "http://grails.org/plugin/asset-pipeline"

    def artefacts = [AssetFileArtefactHandler]

    def license = "APACHE"
    def organization = [ name: "Bertram Capital", url: "http://www.bertramcapital.com/" ]
    def issueManagement = [ system: "GITHUB", url: "http://github.com/bertramdev/asset-pipeline/issues" ]
    def scm = [ url: "http://github.com/bertramdev/asset-pipeline" ]

    def doWithSpring = {

        def pluginManager = PluginManagerHolder.pluginManager
        def plugins = pluginManager.getAllPlugins()
        def manifestProps = new Properties()
        def manifestFile
        try {
            application.getParentContext().getResource("assets/manifest.properties").getFile()
        } catch(e) {
            //Silent fail
        }

        if(manifestFile?.exists()) {
            try {
                manifestProps.load(manifestFile.newDataInputStream())
                application.config.grails.assets.manifest = manifestProps

            } catch(e) {
                println "Failed to load Manifest"
            }
        }

        if(!application.config.grails.assets.containsKey("precompiled")) {
            application.config.grails.assets.precompiled = !Environment.isDevelopmentMode()
        }

        // grails.config.assetPipeline.preProcessors

        // println getAssetPaths()
    }
}
