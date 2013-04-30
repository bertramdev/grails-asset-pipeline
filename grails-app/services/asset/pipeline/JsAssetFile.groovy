package asset.pipeline
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

class JsAssetFile {
	static contentType = 'application/javascript'
	static extensions = ['js']
	static processors = []

	public File file

	def JsAssetFile(file) {
		this.file = file
	}


	def processedStream() {
		def fileText = file?.text
		for(processor in processors) {
			def processInstance = processor.newInstance()
			fileText = processInstance.process(fileText)
			// TODO Iterate Over Processors
		}
		return fileText
		// Return File Stream
	}



	def directiveForLine(line) {
		line.find(/\/\/=(.*)/) { fullMatch, directive -> return directive }
	}
}
