package asset.pipeline

class HtmlAssetFile{
	static final String contentType = 'text/html'
	static extensions = ['html']
	static compiledExtension = 'html'
	static processors = []

	File file
	def baseFile
	def encoding

	HtmlAssetFile(file, baseFile=null) {
		this.file = file
		this.baseFile = baseFile
	}

	def processedStream(precompiler=false) {
		def fileText
		if(baseFile?.encoding || encoding) {
			fileText = file?.text(baseFile?.encoding ? baseFile.encoding : encoding)
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
