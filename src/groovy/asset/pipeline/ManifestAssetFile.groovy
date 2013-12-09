package asset.pipeline

class ManifestAssetFile{
	static final String contentType = 'text/cache-manifest'
	static extensions = ['manifest']
	static compiledExtension = 'manifest'
	static processors = []

	File file
	def baseFile
	def encoding

	ManifestAssetFile(file, baseFile=null) {
		this.file = file
		this.baseFile = baseFile
	}

	def processedStream(precompiler=false) {
		def fileText

		if(baseFile?.encoding || encoding) {
			fileText = file?.getText(baseFile?.encoding ? baseFile.encoding : encoding)
		} else {
			fileText = file?.text
		}

		for(processor in processors) {
			def processInstance = processor.newInstance()
			fileText = processInstance.process(fileText)
		}
		return fileText
		// Return File Stream
	}

	def directiveForLine(line) {
		return null
	}
}
