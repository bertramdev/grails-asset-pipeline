package asset.pipeline
import org.codehaus.groovy.grails.plugins.PluginManagerHolder

class AssetsController {
	def assetProcessorService
    def index() {
            // println "REQUEST: "
        def uri = params.id
        def extension = AssetHelper.extensionFromURI(request.forwardURI)
        def format = request.getHeader('Content-Type')
        if(uri.lastIndexOf(".") >= 0) {
            uri = params.id.substring(0,uri.lastIndexOf("."))
            extension = params.id.substring(params.id.lastIndexOf("."))
        }
        if(extension) {
            format = servletContext.getMimeType(request.forwardURI)
        }

        // println "Extension: ${extension} - ${format}"

    	// def uri = pathWithExtension(params.id)
    	def assetFile = assetProcessorService.serveAsset(uri,format, extension)
        // println "Preparing to render: ${assetFile}"
		if(assetFile) {

            // if(format == 'application/javascript') {
                //TESTING METHOD, NOT FOR FINAL
            //     def ps = new asset.pipeline.processors.UglifyJsProcessor()
            //     assetFile = ps.process(assetFile)
            // }
            response.setContentType(format)
            response.outputStream << assetFile
        }
        else {
            render status: 404
        }

    }

}
