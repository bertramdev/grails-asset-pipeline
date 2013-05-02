import org.apache.commons.io.FileUtils

class AssetPipelineBootStrap {
	def grailsApplication
    def init = { servletContext ->
    	if(grailsApplication.config.grails.assets.storagePath) {
            def webAppAssetsDir = new File("web-app/assets")
            if(webAppAssetsDir.exists()) {
                def storageFile = new File(grailsApplication.config.grails.assets.storagePath)
                storageFile.mkdirs()
                FileUtils.copyDirectory(webAppAssetsDir, storageFile)
            }
        }
    }
    def destroy = {
    }
}
