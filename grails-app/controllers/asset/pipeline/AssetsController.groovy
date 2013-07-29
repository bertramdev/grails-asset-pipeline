package asset.pipeline

class AssetsController {

	def assetProcessorService

    def index() {
            // println "REQUEST: "
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
        def assetFile
        if(params.containsKey('compile') && params.boolean('compile') == false) {
            assetFile = assetProcessorService.serveUncompiledAsset(uri,format, extension)
        } else {
            assetFile = assetProcessorService.serveAsset(uri,format, extension)
        }

		if(assetFile) {
            response.setContentType(format)
            response.outputStream << assetFile
        }
        else {
            render status: 404
        }
    }
}
