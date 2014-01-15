package asset.pipeline

class AssetsController {

	def assetProcessorService

    def index() {
        def uri = params.id
        def extension = AssetHelper.extensionFromURI(request.forwardURI)
        def format = servletContext.getMimeType(request.forwardURI)
        def uriComponents = uri.split("/")
        def lastUriComponent = uriComponents[uriComponents.length - 1]
        //TODO: Only track extension from last /

        if(format != "application/javascript" && format != "text/css" && lastUriComponent.lastIndexOf(".") >= 0 && uri.lastIndexOf(".") >= 0) {
            uri = params.id.substring(0,uri.lastIndexOf("."))
            extension = params.id.substring(params.id.lastIndexOf(".") + 1)
        }

        if(extension) {
            format = servletContext.getMimeType(request.forwardURI)

        }
        if(!format) {
            def contentTypes = AssetHelper.assetMimeTypeForURI(request.forwardURI)
            format = contentTypes ? contentTypes[0] : null
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
