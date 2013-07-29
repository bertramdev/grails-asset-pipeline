package asset.pipeline

class CssAssetFile {
	static final String contentType = 'text/css'
	static extensions = ['css']
	static processors = []

	File file

	CssAssetFile(file) {
		this.file = file
	}

	def processedStream() {
		def fileText = file?.text
		for(processor in processors) {
			fileText = processor.process(fileText)
			// TODO Iterate Over Processors
		}
		return file.text
		// Return File Stream
	}

	def directiveForLine(line) {
		line.find(/\*=(.*)/) { fullMatch, directive -> return directive }
		// line.find(/\/\/=(.*)/) { fullMatch, directive -> return directive }
	}
}
