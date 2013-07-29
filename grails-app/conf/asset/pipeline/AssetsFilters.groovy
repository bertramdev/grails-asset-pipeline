package asset.pipeline

class AssetsFilters {

    def grailsApplication

    def filters = {
        all(controller:'assets', action:'*') {
            before = {
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
