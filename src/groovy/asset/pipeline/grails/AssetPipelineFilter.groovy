package asset.pipeline.grails

import asset.pipeline.AssetPipeline
import asset.pipeline.AssetPipelineConfigHolder
import asset.pipeline.AssetPipelineResponseBuilder
import grails.util.Environment
import groovy.util.logging.Slf4j
import org.springframework.core.io.Resource
import org.springframework.web.context.support.WebApplicationContextUtils

import javax.servlet.*

@Slf4j
class AssetPipelineFilter implements Filter {
	
	public static final ProductionAssetCache fileCache = new ProductionAssetCache();

	def applicationContext
	def servletContext
	def warDeployed
	void init(FilterConfig config) throws ServletException {

		applicationContext = WebApplicationContextUtils.getWebApplicationContext(config.servletContext)
		servletContext = config.servletContext
		warDeployed = Environment.isWarDeployed()
	}

	void destroy() {
	}

	void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		def mapping = applicationContext.assetProcessorService.assetMapping

		def fileUri = new URI(request.requestURI).path
		def baseAssetUrl = request.contextPath == "/" ? "/$mapping" : "${request.contextPath}/${mapping}"
		def format = servletContext.getMimeType(fileUri)
		def encoding = request.getParameter('encoding') ?: request.getCharacterEncoding()
		if(fileUri.startsWith(baseAssetUrl)) {
			fileUri = fileUri.substring(baseAssetUrl.length())
		}
		if(warDeployed) {
			def manifest = AssetPipelineConfigHolder.manifest
			def manifestPath = fileUri
			if(fileUri.startsWith('/')) {
			  manifestPath = fileUri.substring(1) //Omit forward slash
			}
			fileUri = manifest?.getProperty(manifestPath, manifestPath)

			AssetAttributes attributeCache = fileCache.get(fileUri)

			if(attributeCache) {
				if(attributeCache.exists()) {
					def file = attributeCache.resource
					def responseBuilder = new AssetPipelineResponseBuilder(fileUri,request.getHeader('If-None-Match'), request.getHeader('If-Modified-Since'), attributeCache.lastModified)
					responseBuilder.headers.each { header ->
						response.setHeader(header.key,header.value)
					}
					if(responseBuilder.statusCode) {
						response.status = responseBuilder.statusCode
					}
					
					if(responseBuilder.statusCode != 304) {
						def acceptsEncoding = request.getHeader("Accept-Encoding")
						if(acceptsEncoding?.split(",")?.contains("gzip") && attributeCache.gzipExists()) {
							file = attributeCache.getGzipResource()
							response.setHeader('Content-Encoding','gzip')
							response.setHeader('Content-Length', attributeCache.getGzipFileSize().toString())
						} else {
							response.setHeader('Content-Length', attributeCache.getFileSize().toString())
						}
						if(encoding) {
							response.setCharacterEncoding(encoding)
						}

						response.setContentType(format)
						def inputStream
						try {
							byte[] buffer = new byte[102400];
							int len;
							inputStream = file.inputStream
							def out = response.outputStream
							while ((len = inputStream.read(buffer)) != -1) {
								out.write(buffer, 0, len);
							}
							response.flushBuffer()
						} catch(e) {
							log.debug("File Transfer Aborted (Probably by the user)",e)
						}finally {
							try { inputStream?.close()} catch(ie){/*silent close fail*/}
						}
					} else {
						response.flushBuffer()
					}
				} else {
					response.status = 404
					response.flushBuffer()
				}
				
			} else {
				Resource file = applicationContext.getResource("assets/${fileUri}")
				if(!file.exists()) {
					file = applicationContext.getResource("classpath:assets/${fileUri}")
				}

				if(file.exists()) {
					responseBuilder.headers.each { header ->
						response.setHeader(header.key,header.value)
					}
					if(responseBuilder.statusCode) {
						response.status = responseBuilder.statusCode
					}
					

					def gzipFile = applicationContext.getResource("assets/${fileUri}.gz")
					if(!gzipFile.exists()) {
						gzipFile = applicationContext.getResource("classpath:assets/${fileUri}.gz")
					}

					AssetAttributes newCache = new AssetAttributes(true, gzipFile.exists(), false, file.contentLength(), gzipFile.exists() ? gzipFile.contentLength() : null, fileLastModified, file, gzipFile)
					fileCache.put(fileUri, newCache)

					if(responseBuilder.statusCode != 304) {
						// Check for GZip
						def acceptsEncoding = request.getHeader("Accept-Encoding")
						if(acceptsEncoding?.split(",")?.contains("gzip")) {
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
						def inputStream
						try {
							byte[] buffer = new byte[102400];
							int len;
							inputStream = file.inputStream
							def out = response.outputStream
							while ((len = inputStream.read(buffer)) != -1) {
								out.write(buffer, 0, len);
							}
							response.flushBuffer()
						} catch(e) {
							log.debug("File Transfer Aborted (Probably by the user)",e)
						} finally {
							try { inputStream?.close()} catch(ie){/*silent close fail*/}
						}
					} else {
						response.flushBuffer()
					}
				} else {
					AssetAttributes newCache = new AssetAttributes(false, false, false, null, null, null, null, null)
					fileCache.put(fileUri, newCache)
					response.status = 404
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

				response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate") // HTTP 1.1.
				response.setHeader("Pragma", "no-cache") // HTTP 1.0.
				response.setDateHeader("Expires", 0) // Proxies.
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
