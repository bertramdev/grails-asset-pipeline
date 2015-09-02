package asset.pipeline.grails


import asset.pipeline.AssetHelper
import asset.pipeline.AssetPipeline


class AssetsTagLib {

	static namespace = 'asset'
	static returnObjectForTags = ['assetPath']

	private static final LINE_BREAK = System.getProperty('line.separator') ?: '\n'


	def assetProcessorService
	def grailsApplication


	/**
	 * @attr src REQUIRED
	 */
	def javascript = {attrs ->
		def src = attrs.remove('src')
		def attrBundle = attrs.remove('bundle')
		attrs.remove('href')
		src = "${AssetHelper.nameWithoutExtension(src)}.js"
		def uri
		def extension

		def conf = grailsApplication.config.grails.assets

		def nonBundledMode = (!grailsApplication.warDeployed && conf.bundle != true && attrBundle != 'true')

		if(!nonBundledMode) {
			out << "<script src=\"${assetPath(src:src)}\" type=\"text/javascript\" ${paramsToHtmlAttr(attrs)}></script>"
		}
		else {
			if (src.lastIndexOf('.') >= 0) {
				uri = src.substring(0, src.lastIndexOf('.'))
				extension = src.substring(src.lastIndexOf('.') + 1)
			}
			else {
				uri = src
				extension = 'js'
			}
			// def startTime = new Date().time
			def list = AssetPipeline.getDependencyList(uri, 'application/javascript', extension)
			def modifierParams = ['compile=false']
			if (attrs.charset) {
				modifierParams << "encoding=${attrs.charset}"
			}
			list.each {dep ->
				def depAssetPath = assetPath([src: "${dep.path}", ignorePrefix:true])
				out << "<script src=\"${depAssetPath}?${modifierParams.join('&')}\" type=\"text/javascript\" ${paramsToHtmlAttr(attrs)}></script>${LINE_BREAK}"
			}
			// println "Fetching Dev Mode Dependency List Time ${new Date().time - startTime}"
		}
	}

	/**
	 * @attr href OPTIONAL alternative to src
	 * @attr src OPTIONAL alternative to href
	 */
	def stylesheet = {attrs ->
		def src  = attrs.remove('src')
		def attrBundle = attrs.remove('bundle')
		def href = attrs.remove('href')
		if (href) {
			src = href
		}
		src = "${AssetHelper.nameWithoutExtension(src)}.css"
		def conf = grailsApplication.config.grails.assets
		def uri
		def extension
		def nonBundledMode = (!grailsApplication.warDeployed && conf.bundle != true && attrBundle != 'true')

		if(!nonBundledMode) {
			out << link([rel: 'stylesheet', href:src] + attrs)
		}
		else {
			if (src.lastIndexOf('.') >= 0) {
				uri = src.substring(0, src.lastIndexOf('.'))
				extension = src.substring(src.lastIndexOf('.') + 1)
			}
			else {
				uri = src
				extension = 'css'
			}
			def list = AssetPipeline.getDependencyList(uri, 'text/css', extension)
			def modifierParams = ['compile=false']
			if (attrs.charset) {
				modifierParams << "encoding=${attrs.charset}"
			}
			list.each {dep ->
				def depAssetPath = assetPath([src: "${dep.path}", ignorePrefix:true])
				out << "<link rel=\"stylesheet\" href=\"${depAssetPath}?${modifierParams.join('&')}\" ${paramsToHtmlAttr(attrs)}/>${LINE_BREAK}"
			}
		}
	}

	def image = {attrs ->
		def src = attrs.remove('src')
		def absolute = attrs.remove('absolute')
		out << "<img src=\"${assetPath(src:src, absolute: absolute)}\" ${paramsToHtmlAttr(attrs)}/>"
	}


	/**
	 * @attr href REQUIRED
	 * @attr rel REQUIRED
	 * @attr type OPTIONAL
	 */
	def link = {attrs ->
		def href = attrs.remove('href')
		out << "<link ${paramsToHtmlAttr(attrs)} href=\"${assetPath(src:href)}\"/>"
	}


	def script = {attrs, body ->
		def assetBlocks = request.getAttribute('assetScriptBlocks')
		if (!assetBlocks) {
			assetBlocks = []
		}
		assetBlocks << [attrs: attrs, body: body()]
		request.setAttribute('assetScriptBlocks', assetBlocks)
	}

	def deferredScripts = {attrs ->
		def assetBlocks = request.getAttribute('assetScriptBlocks')
		if (!assetBlocks) {
			return
		}
		assetBlocks.each {assetBlock ->
			out << "<script ${paramsToHtmlAttr(assetBlock.attrs)}>${assetBlock.body}</script>"
		}
	}


	def assetPath = {attrs ->
		g.assetPath(attrs)
	}

	def assetPathExists = {attrs, body ->
		if (isAssetPath(attrs.remove('src'))) {
			out << (body() ?: true)
		}
		else {
			out << ''
		}
	}

	boolean isAssetPath(src) {
		assetProcessorService.isAssetPath(src)
	}

	private paramsToHtmlAttr(attrs) {
		attrs.collect {key, value -> "${key}=\"${value.toString().replace('"', '\\"')}\""}?.join(' ')
	}
}
