import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.apache.tools.ant.DirectoryScanner
// import asset.pipeline.*;

includeTargets << grailsScript("_GrailsBootstrap")

target(assetPrecompile: "Precompiles assets in the application as specified by the precompile glob!") {
    depends(configureProxy,compile, packageApp, bootstrap)

    def assetHelper             = classLoader.loadClass('asset.pipeline.AssetHelper')
    def directiveProcessorClass = classLoader.loadClass('asset.pipeline.DirectiveProcessor')
    def uglifyJsProcessor 		= classLoader.loadClass('asset.pipeline.processors.UglifyJsProcessor').newInstance()
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

	for(counter = 0 ; counter < filesToProcess.size(); counter++) {
		def fileName = filesToProcess[counter]
		event("StatusUpdate",["Processing File ${counter+1} of ${filesToProcess.size()} - ${fileName}"]);
		def extension = assetHelper.extensionFromURI(fileName)
		fileName = fileName.substring(0,fileName.lastIndexOf("."))

		def assetFile = assetHelper.artefactForFileWithExtension(assetHelper.fileForUri(fileName,null,extension), extension)
		if(assetFile) {
			def fileData = null

			if(assetFile.class.name == 'java.io.File') {
				// println "Non Asset File"
			}
			else {
				def directiveProcessor = directiveProcessorClass.newInstance(assetFile.contentType)
				fileData = directiveProcessor.compile(assetFile)
				if(assetFile.contentType == 'application/javascript') {
					def newFileData = fileData
					try {
						newFileData = uglifyJsProcessor.process(fileData)
					} catch(e) {
						println "Uglify JS Exception ${e}"
						newFileData = fileData
					}
					fileData = newFileData
				}
			}


			def outputFile = new File("web-app/assets/${fileName}.${extension}");
			def parentTree = new File(outputFile.parent)
			parentTree.mkdirs()
			outputFile.createNewFile();
			if(fileData) {
				outputFile.text = fileData
			} else {
				if(assetFile.class.name == 'java.io.File') {
					outputFile.bytes = assetFile.bytes
				} else {
					outputFile.bytes = assetFile.file.bytes
				}
			}
			def targetStream = new java.io.ByteArrayOutputStream()

			def zipStream = new java.util.zip.GZIPOutputStream(targetStream)
			zipStream.write(outputFile.bytes)
			def zipFile = new File("${outputFile.getAbsolutePath()}.gz")
			zipFile.createNewFile()
			zipFile.bytes = targetStream.toByteArray()
			targetStream.close()


		}
		else {
			println("Asset File not found! ${fileName}")
		}
	}

	// Save File
	// Save File Digest
	// Gzip File
	// Update Manifest
}

setDefaultTarget(assetPrecompile)
