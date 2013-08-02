import grails.util.Environment

import org.codehaus.groovy.grails.plugins.PluginManagerHolder


class AssetPipelineGrailsPlugin {
    def version = "0.2.1"
    def grailsVersion = "2.0 > *"
    def title = "Asset Pipeline Plugin"
    def author = "David Estes"
    def authorEmail = "destes@bcap.com"
    def description = 'The Grails `asset-pipeline` is a plugin used for managing/processing static assets. These include processing, and minification of both css, and javascript files. It is also capable of being extended to compile custom static assets, such as coffeescript.'
    def documentation = "http://github.com/bertramdev/asset-pipeline"


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
            manifestFile = application.getParentContext().getResource("assets/manifest.properties").getFile()
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
