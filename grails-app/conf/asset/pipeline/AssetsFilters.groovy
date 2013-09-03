package asset.pipeline

class AssetsFilters {

    def grailsApplication

    def filters = {
        all(controller:'assets', action:'*') {
            before = {
                def context = grailsApplication.mainContext
                def mapping = context.assetProcessorService.assetMapping
                def config  = grailsApplication.config.grails.assets
                def debugParameter = params."_debugResources" == 'y' || params."_debugAssets" == "y"
                if(config.allowDebugParam && debugParameter) {
                    return
                }

                // Prefer whats in web-app/assets instead of the other
                def fileUri = request.forwardURI
                def baseAssetUrl = request.contextPath == "/" ? "/$mapping" : "${request.contextPath}/${mapping}"
                if(fileUri.startsWith(baseAssetUrl)) {
                    fileUri = fileUri.substring(baseAssetUrl.length())
                }

                def file = grailsApplication.parentContext.getResource("assets${fileUri}").getFile()
                if (!file.exists() || file.directory) {
                    return
                }

                def format = servletContext.getMimeType(request.forwardURI)
                response.setContentType(format)
                response.setHeader('Cache-Control','public, max-age=31536000')
                response.outputStream << file.getBytes()
                return false
            }
        }
    }
}
