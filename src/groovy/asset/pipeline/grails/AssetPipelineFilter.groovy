package asset.pipeline.grails

import asset.pipeline.AssetPipeline
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.AssetPipelineResponseBuilder
import grails.util.Environment
import groovy.util.logging.Slf4j
import org.springframework.web.context.support.WebApplicationContextUtils

import javax.servlet.*
import java.text.SimpleDateFormat

@Slf4j
class AssetPipelineFilter implements Filter {

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz"
    private final SimpleDateFormat sdf = new SimpleDateFormat(HTTP_DATE_FORMAT);

    def applicationContext
    def servletContext
    def warDeployed

    void init(FilterConfig config) throws ServletException {
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        applicationContext = WebApplicationContextUtils.getWebApplicationContext(config.servletContext)
        servletContext = config.servletContext
        warDeployed = Environment.isWarDeployed()
        // permalinkService = applicationContext['spudPermalinkService']
    }

    void destroy() {
    }

    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        def mapping = applicationContext.assetProcessorService.assetMapping

        def fileUri = new URI(request.requestURI).path
        def baseAssetUrl = request.contextPath == "/" ? "/$mapping" : "${request.contextPath}/${mapping}"
        def format = servletContext.getMimeType(fileUri)
        def encoding = request.getParameter('encoding') ?: request.getCharacterEncoding()
        if (fileUri.startsWith(baseAssetUrl)) {
            fileUri = fileUri.substring(baseAssetUrl.length())
        }
        if (warDeployed) {
            def manifest = AssetPipelineConfigHolder.manifest
            def manifestPath = fileUri
            if (fileUri.startsWith('/')) {
                manifestPath = fileUri.substring(1) //Omit forward slash
            }
            fileUri = manifest?.getProperty(manifestPath) ?: manifestPath
            def file = applicationContext.getResource("assets/${fileUri}")
            if (file.exists()) {
                def responseBuilder = new AssetPipelineResponseBuilder(fileUri, request.getHeader('If-None-Match'), request.getHeader('If-Modified-Since'))
                response.setHeader('Last-Modified', getLastModifiedDate(file))

                if (hasNotChanged(responseBuilder.ifModifiedSinceHeader, file)) {
                    responseBuilder.statusCode = 304
                }

                responseBuilder.headers.each { header ->
                    response.setHeader(header.key, header.value)
                }
                if (responseBuilder.statusCode) {
                    response.status = responseBuilder.statusCode
                }

                if (responseBuilder.statusCode != 304) {
                    response.setHeader('Last-Modified', getLastModifiedDate(file))

                    // Check for GZip
                    def acceptsEncoding = request.getHeader("Accept-Encoding")
                    if (acceptsEncoding?.split(",")?.contains("gzip")) {
                        def gzipFile = applicationContext.getResource("assets/${fileUri}.gz")
                        if (gzipFile.exists()) {
                            file = gzipFile
                            response.setHeader('Content-Encoding', 'gzip')
                        }
                    }

                    if (encoding) {
                        response.setCharacterEncoding(encoding)
                    }
                    response.setContentType(format)
                    response.setHeader('Content-Length', file.contentLength().toString())

                    try {
                        byte[] buffer = new byte[102400];
                        int len;
                        def inputStream = file.inputStream
                        def out = response.outputStream
                        while ((len = inputStream.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                        response.flushBuffer()
                    } catch (e) {
                        log.debug("File Transfer Aborted (Probably by the user)", e)
                    }
                } else {
                    response.flushBuffer()
                }

            }
        } else {
            def fileContents
            if (request.getParameter('compile') == 'false') {
                fileContents = AssetPipeline.serveUncompiledAsset(fileUri, format, null, encoding)
            } else {
                fileContents = AssetPipeline.serveAsset(fileUri, format, null, encoding)
            }

            if (fileContents != null) {

                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate") // HTTP 1.1.
                response.setHeader("Pragma", "no-cache") // HTTP 1.0.
                response.setDateHeader("Expires", 0) // Proxies.
                response.setHeader('Content-Length', fileContents.size().toString())

                response.setContentType(format)
                try {
                    response.outputStream << fileContents
                    response.flushBuffer()
                } catch (e) {
                    log.debug("File Transfer Aborted (Probably by the user)", e)
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

    boolean hasNotChanged(String ifModifiedSince, file) {
        boolean hasNotChanged = false
        if (ifModifiedSince) {
            try {
                hasNotChanged = new Date(file?.lastModified()) <= sdf.parse(ifModifiedSince)
            } catch (Exception e) {
                log.debug("Could not parse date time or file modified date", e)
            }
        }
        return hasNotChanged
    }
    private String getLastModifiedDate(file) {
        String lastModifiedDateTimeString = sdf.format(new Date())
        try {
            lastModifiedDateTimeString = sdf.format(new Date(file?.lastModified()))
        } catch (Exception e) {
            log.debug("Could not get last modified date time for file", e)
        }

        return lastModifiedDateTimeString
    }

}
