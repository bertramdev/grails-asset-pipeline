package asset.pipeline


import org.springframework.context.*
import javax.servlet.*
import javax.servlet.http.*
import org.springframework.web.context.support.WebApplicationContextUtils
import org.springframework.web.filter.*
import groovy.util.logging.Commons
import groovy.transform.*
import asset.pipeline.grails.AssetProcessorService
import asset.pipeline.AssetPipelineConfigHolder

@Commons
@CompileStatic
class AssetPipelineFilter extends OncePerRequestFilter {
    ApplicationContext applicationContext
    ServletContext servletContext

    @Override
    void initFilterBean() throws ServletException {
        def config = filterConfig
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(config.servletContext)
        servletContext = config.servletContext
    }

    void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        boolean warDeployed = AssetPipelineConfigHolder.manifest ? true : false

        String mapping = ((AssetProcessorService)(applicationContext.getBean('assetProcessorService', AssetProcessorService))).assetMapping

        def fileUri = new java.net.URI(request.requestURI).path
        String baseAssetUrl = request.contextPath == "/" ? "/$mapping" : "${request.contextPath}/${mapping}"
        String format = servletContext.getMimeType(fileUri)
        String encoding = request.getParameter('encoding') ?: request.getCharacterEncoding()
        if(fileUri.startsWith(baseAssetUrl)) {
            fileUri = fileUri.substring(baseAssetUrl.length())
        }
        if(warDeployed) {
            def file = applicationContext.getResource("classpath:assets${fileUri}")
            if (file.exists()) {
                def responseBuilder = new AssetPipelineResponseBuilder(fileUri,request.getHeader('If-None-Match'))
                responseBuilder.headers.each { Map.Entry header ->
                    response.setHeader(header.key.toString(), header.value.toString())
                }
                if(responseBuilder.statusCode) {
                    response.status = responseBuilder.statusCode
                }

                if(responseBuilder.statusCode != 304) {
                    // Check for GZip
                    def acceptsEncoding = request.getHeader("Accept-Encoding")
                    if(acceptsEncoding?.split(",")?.contains("gzip")) {
                        def gzipFile = applicationContext.getResource("classpath:assets${fileUri}.gz")
                        if(gzipFile.exists()) {
                            file = gzipFile
                            response.setHeader('Content-Encoding','gzip')
                        }
                    }
                    
                    if(encoding) {
                        response.setCharacterEncoding(encoding)
                    }
                    response.setContentType(format)
                    response.setHeader('Content-Length', file.contentLength().toString())

                    try {
                        response.outputStream << file.inputStream.getBytes()
                        response.flushBuffer()
                    } catch(e) {
                        log.debug("File Transfer Aborted (Probably by the user)",e)
                    }
                 } else {
                    response.flushBuffer()
                 }

            }
        } else {
            def fileContents
            if(request.getParameter('compile') == 'false') {
                fileContents = AssetPipeline.serveUncompiledAsset(fileUri,format, null, encoding)
            } else {
                fileContents = AssetPipeline.serveAsset(fileUri,format, null, encoding)
            }

            if (fileContents != null) {

                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
                response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
                response.setDateHeader("Expires", 0); // Proxies.
                response.setHeader('Content-Length', fileContents.size().toString())
                
                response.setContentType(format)
                try {
                    response.outputStream << fileContents
                    response.flushBuffer()
                } catch(e) {
                    log.debug("File Transfer Aborted (Probably by the user)",e)
                }
            } else {
                response.status = 404
                response.flushBuffer()
            }
        }


        if (!response.committed) {
            chain.doFilter(request, response)
        }
    }

}
