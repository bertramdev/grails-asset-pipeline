package asset.pipeline.grails


class AssetMethodTagLib {

	static namespace = 'g'
	static returnObjectForTags = ['assetPath']


	def assetProcessorService


	def assetPath = {final def attrs ->
		final def     src
		final boolean absolute

		if (attrs instanceof Map) {
			src = attrs.src

			final def abs = attrs.absolute
			absolute = abs != null ? abs : false
		}
		else {
			src      = attrs
			absolute = false
		}

		return assetProcessorService.assetBaseUrl(request, absolute) + assetProcessorService.getAssetPath(src)
	}
}
