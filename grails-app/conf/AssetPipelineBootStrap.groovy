import org.apache.commons.io.FileUtils

class AssetPipelineBootStrap {
	def grailsApplication
    // def grailsAttributes
    def init = { servletContext ->
    	if(grailsApplication.config.grails.assets.storagePath) {
            def manifestFile = grailsApplication.getParentContext().getResource("assets/manifest.properties").getFile()
            // println("Checking For Parent ${manifestFile.parent}")
            def webAppAssetsDir = new File(manifestFile.parent)
            // def webAppAssetsDir = new File("web-app/assets")
            if(webAppAssetsDir.exists()) {
                // println "Path Found, Copying Assets"
                def storageFile = new File(grailsApplication.config.grails.assets.storagePath)
                storageFile.mkdirs()
                FileUtils.copyDirectory(webAppAssetsDir, storageFile)
            }
        }
    }
    def destroy = {
    }
}
