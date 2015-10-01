package asset.pipeline.grails


import asset.pipeline.AssetHelper
import asset.pipeline.AssetPipeline
import org.codehaus.groovy.grails.web.util.GrailsPrintWriter


class AssetsTagLib {

	static namespace = 'asset'
	static returnObjectForTags = ['assetPath']

	private static final LINE_BREAK = System.getProperty('line.separator') ?: '\n'


	def assetProcessorService
	def grailsApplication


	/**
	 * @attr src REQUIRED
	 */
	def javascript = {final attrs ->
		attrs.remove('href')
		element(attrs, 'js', 'application/javascript', null) {final String src, final outputAttrs, final String endOfLine ->
			out << '<script type="text/javascript" src="' << src << '" ' << paramsToHtmlAttr(outputAttrs) << '></script>' << endOfLine
		}
	}

	/**
	 * @attr href OPTIONAL alternative to src
	 * @attr src OPTIONAL alternative to href
	 */
	def stylesheet = {final attrs ->
		element(attrs, 'css', 'text/css', Objects.toString(attrs.remove('href'), null)) {final String src, final outputAttrs, final String endOfLine ->
			if (''.equals(endOfLine)) {
				out << link([rel: 'stylesheet', href: src] + outputAttrs)
			}
			else {
				out << '<link rel="stylesheet" href="' << src << '" ' << paramsToHtmlAttr(outputAttrs) << '/>' << endOfLine
			}
		}
	}

	private void element(final attrs, final String ext, final String contentType, final String srcOverride, final Closure<GrailsPrintWriter> output) {
		def src = attrs.remove('src')
		if (srcOverride) {
			src = srcOverride
		}
		src = "${AssetHelper.nameWithoutExtension(src)}.${ext}"

		final def nonBundledMode = (!grailsApplication.warDeployed && grailsApplication.config.grails.assets.bundle != true && attrs.remove('bundle') != 'true')
		if (! nonBundledMode) {
			output(assetPath(src: src), attrs, '')
		}
		else {
			final int lastDotIndex = src.lastIndexOf('.')
			final def uri
			final def extension
			if (lastDotIndex >= 0) {
				uri       = src.substring(0, lastDotIndex)
				extension = src.substring(lastDotIndex + 1)
			}
			else {
				uri       = src
				extension = ext
			}
			final def modifierParams = ['compile=false']
			if (attrs.charset) {
				modifierParams << "encoding=${attrs.charset}"
			}
			AssetPipeline.getDependencyList(uri, contentType, extension).each {
				output("${assetPath([src: "${it.path}", ignorePrefix: true])}?${modifierParams.join('&')}", attrs, LINE_BREAK)
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
