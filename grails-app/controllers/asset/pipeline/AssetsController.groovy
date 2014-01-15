package asset.pipeline

class AssetsController {

	def assetProcessorService

    def index() {
        def uri = params.id
        def extension = AssetHelper.extensionFromURI(request.forwardURI)
        def contentTypes = AssetHelper.assetMimeTypeForURI(request.forwardURI)
        def format = contentTypes ? contentTypes[0] : null
        if(!format)  {
            format = servletContext.getMimeType(request.forwardURI)
        }

        if(extension && uri.endsWith(".${extension}")) {
            uri = params.id[0..(-extension.size()-2)]
        }

        def assetFile
        if(params.containsKey('compile') && params.boolean('compile') == false) {
            assetFile = assetProcessorService.serveUncompiledAsset(uri,format, extension, params.encoding)
        } else {
            assetFile = assetProcessorService.serveAsset(uri,format, extension)
        }
		if(assetFile) {
            response.setContentType(format)
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
            response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
            response.setDateHeader("Expires", 0); // Proxies.
            if(format == 'text/html') {
                render contentType: 'text/html', text: new String(assetFile)
            } else {
                response.outputStream << assetFile
                response.flushBuffer()
            }
        }
        else {
            render status: 404
        }
    }
}
