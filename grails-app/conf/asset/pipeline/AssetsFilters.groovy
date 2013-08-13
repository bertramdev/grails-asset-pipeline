package asset.pipeline

class AssetsFilters {

    def grailsApplication

    def filters = {
        all(controller:'assets', action:'*') {
            before = {
                def config = grailsApplication.config.grails.assets
                def debugParameter = params."_debugResources" == 'y' || params."_debugAssets" == "y"
                if(config.allowDebugParam && debugParameter) {
                    return
                }
                // Prefer whats in web-app/assets instead of the other
                def file = grailsApplication.parentContext.getResource(request.forwardURI).getFile()
                if (!file.exists()) {
                    return
                }

                def format = servletContext.getMimeType(request.forwardURI)
                response.setContentType(format)
                response.outputStream << file.getBytes()
                return false
            }
        }
    }
}
