package asset.pipeline

class JsAssetFile{
	static final String contentType = 'application/javascript'
	static extensions = ['js']
	static compiledExtension = 'js'
	static processors = []

	File file
	def baseFile
	def encoding


	JsAssetFile(file, baseFile=null) {
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
		line.find(/\/\/=(.*)/) { fullMatch, directive -> return directive }
	}
}
