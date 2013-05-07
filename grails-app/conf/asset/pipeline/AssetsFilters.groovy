package asset.pipeline
import org.codehaus.groovy.grails.commons.ApplicationHolder
class AssetsFilters {

    def filters = {
        all(controller:'assets', action:'*') {
            before = {
                def file = ApplicationHolder.getApplication().getParentContext().getResource(request.forwardURI).getFile();
                if(file.exists()) {
                    def format = servletContext.getMimeType(request.forwardURI)
                    response.setContentType(format)
                    response.outputStream << file.getBytes()
                    return false
                }


            }

        }
    }
}
