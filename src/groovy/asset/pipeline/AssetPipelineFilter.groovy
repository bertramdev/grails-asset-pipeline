package asset.pipeline

import javax.servlet.*
import org.springframework.web.context.support.WebApplicationContextUtils
import grails.util.Environment
import groovy.util.logging.Log4j

@Log4j
class AssetPipelineFilter implements Filter {
  def applicationContext 
  def servletContext
  void init(FilterConfig config) throws ServletException {
      applicationContext = WebApplicationContextUtils.getWebApplicationContext(config.servletContext)
      servletContext = config.servletContext
      // permalinkService = applicationContext['spudPermalinkService']
    }

  void destroy() {
  }

  void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    def mapping = applicationContext.assetProcessorService.assetMapping

    // Prefer whats in web-app/assets instead of the other
    def fileUri = request.requestURI
    def baseAssetUrl = request.contextPath == "/" ? "/$mapping" : "${request.contextPath}/${mapping}"
    if(fileUri.startsWith(baseAssetUrl)) {
        fileUri = fileUri.substring(baseAssetUrl.length())
    }

    def file = applicationContext.getResource("assets${fileUri}")
    if (file.exists()) {
    // Check for GZip
      def acceptsEncoding = request.getHeader("Accept-Encoding")
      if(acceptsEncoding?.split(",")?.contains("gzip")) {
        def gzipFile = applicationContext.getResource("assets${fileUri}.gz")
        if(gzipFile.exists()) {
          file = gzipFile
          response.setHeader('Content-Encoding','gzip')
        }
      }
      def format = servletContext.getMimeType(request.forwardURI)
      def encoding = request.getCharacterEncoding()
      if(encoding) {
        response.setCharacterEncoding(encoding)
      }
      response.setContentType(format)
      response.setHeader('Cache-Control','public, max-age=31536000')

      try {
        response.outputStream << file.inputStream.getBytes()
        response.flushBuffer()  
      } catch(e) {
        log.debug("File Transfer Aborted (Probably by the user)",e)
      }
      
    }

    if (!response.committed) {
      chain.doFilter(request, response)
    }
  }

}