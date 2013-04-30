import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.apache.tools.ant.DirectoryScanner
// import asset.pipeline.*;

includeTargets << grailsScript("_GrailsBootstrap")

target(main: "Precompiles assets in the application as specified by the precompile glob!") {
    depends(configureProxy,compile, packageApp, bootstrap)

    def assetHelper             = classLoader.loadClass('asset.pipeline.AssetHelper')
    def directiveProcessorClass = classLoader.loadClass('asset.pipeline.DirectiveProcessor')
	def grailsApplication       = ApplicationHolder.getApplication()
    event("StatusUpdate",["Precompiling Assets!"]);

	// TODO: Find all Files we want to process
	DirectoryScanner scanner = new DirectoryScanner()
	scanner.setExcludes(["**/.*","**/.DS_Store"] as String[])
	scanner.setIncludes(["**/*"] as String[])

	def assetPaths = assetHelper.getAssetPaths()
	def filesToProcess = []
	for(path in assetPaths) {
	    event("StatusUpdate",["Scanning ${path}"]);
	    scanner.setBasedir(path)
	    scanner.setCaseSensitive(false)
	    scanner.scan()
	    filesToProcess += scanner.getIncludedFiles().flatten()

	}
	filesToProcess.unique()
	def directiveProcessor = directiveProcessorClass.newInstance()
	for(counter = 0 ; counter < filesToProcess.size(); counter++) {
		def fileName = filesToProcess[counter]
		event("StatusUpdate",["Processing File ${counter+1} of ${filesToProcess.size()} - ${fileName}"]);
		def extension = assetHelper.extensionFromURI(fileName)
		fileName = fileName.substring(0,fileName.lastIndexOf("."))

		def assetFile = assetHelper.artefactForFile(assetHelper.fileForUri(fileName,null,extension))
		if(assetFile) {
			def fileData = directiveProcessor.compile(assetFile)
			if(assetFile.class.name == 'java.io.File') {
				println "Non Asset File"
			}
			else {
				if(assetFile.contentType == 'application/javascript') {
					def ps = classLoader.loadClass('asset.pipeline.processors.UglifyJsProcessor').newInstance()
		            fileData = ps.process(fileData)
				}
			}


		}
		else {
			println("Asset File not found! ${fileName}")
		}
	}

	// Save File
	// Save File Digest
	// Gzip File
	// Update Manifest

  def possibleFileSpecs = grailsApplication.assetFileClasses

	for(fileSpec in possibleFileSpecs) {
	  println "Filespec Extensions: ${fileSpec.getPropertyValue('extensions')}"
	}
}

setDefaultTarget(main)
