package asset.pipeline

class JsAssetFile{
	static final String contentType = 'application/javascript'
	static extensions = ['js']
	static compiledExtension = 'js'
	static processors = []

	File file
	def baseFile

	JsAssetFile(file, baseFile=null) {
		this.file = file
		this.baseFile = baseFile
	}

	def processedStream(precompiler=false) {
		def fileText = file?.text
		for(processor in processors) {
			def processInstance = processor.newInstance(precompiler)
			fileText = processInstance.process(fileText)
			// TODO Iterate Over Processors
		}
		return fileText
		// Return File Stream
	}

	def directiveForLine(line) {
		line.find(/^\/\/=(.*)/) { fullMatch, directive -> return directive }
	}
}
