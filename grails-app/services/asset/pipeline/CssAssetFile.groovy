package asset.pipeline
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

class CssAssetFile {
	static contentType = 'text/css'
	static extensions = ['css']
	static processors = []

	public File file

	def CssAssetFile(file) {
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
