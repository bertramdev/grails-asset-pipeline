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

                def file = grailsApplication.parentContext.getResource("assets${fileUri}")
                if (!file.exists()) {
                    return
                }

                // Check for GZip
                def acceptsEncoding = request.getHeader("Accept-Encoding")
                if(acceptsEncoding?.split(",")?.contains("gzip")) {
                    def gzipFile = grailsApplication.parentContext.getResource("assets${fileUri}.gz")
                    if(gzipFile.exists()) {
                        file = gzipFile
                        response.setHeader('Content-Encoding','gzip')
                    }
                }

                def format = servletContext.getMimeType(request.forwardURI)
                response.setContentType(format)
                response.setHeader('Cache-Control','public, max-age=31536000')
                response.outputStream << file.inputStream.getBytes()
                return false
            }
        }
    }
}
