package asset.pipeline
import asset.pipeline.processors.CssProcessor
class CssAssetFile{
	static final String contentType = 'text/css'
	static extensions = ['css']
	static compiledExtension = 'css'
	static processors = [CssProcessor]

	File file

	CssAssetFile(file) {
		this.file = file
	}

	def processedStream() {
		def fileText = file?.text
		for(processor in processors) {
			def processInstance = processor.newInstance()
			fileText = processInstance.process(fileText, this)
		}
		return fileText
		// Return File Stream
	}

	def directiveForLine(line) {
		line.find(/\*=(.*)/) { fullMatch, directive -> return directive }
	}
}
