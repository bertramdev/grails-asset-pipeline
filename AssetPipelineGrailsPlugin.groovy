import org.codehaus.groovy.grails.plugins.PluginManagerHolder
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import grails.util.GrailsUtil
class AssetPipelineGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.2 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Asset Pipeline Plugin" // Headline display name of the plugin
    def author = "David Estes"
    def authorEmail = "destes@bcap.com"
    def description = '''\
The Grails asset-pipeline is a port from the rails asset-pipeline into the grails world. It allows similar require directives within the grails-app/assets folder.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/asset-pipeline"

    def artefacts = [ asset.pipeline.AssetFileArtefactHandler ]

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
   def license = "APACHE"

    // Details of company behind the plugin (if there is one)
   def organization = [ name: "Bertram Capital", url: "http://www.bertramcapital.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
   def scm = [ url: "http://github.com/bertramdev/asset-pipeline" ]

    def doWithWebDescriptor = { xml ->

        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {

        // TODO Implement runtime spring config (optional)
        def pluginManager = PluginManagerHolder.pluginManager
        def plugins = pluginManager.getAllPlugins()
        def manifestProps = new Properties()
        def manifestFile = null
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
            if(GrailsUtil.environment == "development") {
                application.config.grails.assets.precompiled = false
            } else {
                application.config.grails.assets.precompiled = true
            }
        }

        // grails.config.assetPipeline.preProcessors

        // println getAssetPaths()
    }




    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
