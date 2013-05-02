package asset.pipeline
import org.codehaus.groovy.grails.commons.ApplicationHolder
class AssetsFilters {

    def filters = {
        all(controller:'assets', action:'*') {
            before = {
                println "Getting The Forwarded Request URI"
                println request.forwardURI
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
