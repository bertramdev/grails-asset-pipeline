
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.apache.tools.ant.DirectoryScanner
import java.security.MessageDigest
import java.util.Properties;

// import asset.pipeline.*;

includeTargets << grailsScript("_GrailsBootstrap")

target(assetCompile: "Precompiles assets in the application as specified by the precompile glob!") {
    depends(configureProxy,compile, packageApp, bootstrap)
    // def manifestMap = [:]
    Properties manifestProperties = new Properties()
    def assetHelper             = classLoader.loadClass('asset.pipeline.AssetHelper')
    def directiveProcessorClass = classLoader.loadClass('asset.pipeline.DirectiveProcessor')
    def uglifyJsProcessor 		= classLoader.loadClass('asset.pipeline.processors.UglifyJsProcessor').newInstance()
	def grailsApplication       = ApplicationHolder.getApplication()
    event("StatusUpdate",["Precompiling Assets!"]);

    // Clear compiled assets folder
    def assetDir = new File("web-app/assets")
    if(assetDir.exists()) {
    	assetDir.deleteDir()
    }


	//Find all files we want to process
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
		fileName  = assetHelper.nameWithoutExtension(fileName)


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
						event("StatusUpdate",["Uglifying File ${counter+1} of ${filesToProcess.size()} - ${fileName}"]);
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

			// Generate Checksum
			if(extension) {
				try {
					MessageDigest md = MessageDigest.getInstance("MD5");
					md.update(outputFile.bytes)
					def checksum = md.digest()
					def digestedFile = new File("web-app/assets/${fileName}-${checksum.encodeHex()}.${extension}");
					digestedFile.createNewFile()
					digestedFile.bytes = outputFile.bytes
					manifestProperties.setProperty("${fileName}.${extension}", "${fileName}-${checksum.encodeHex()}.${extension}")
					// println "Generated digest ${checksum.encodeHex().toString()}"
					// Zip it Good!
					def targetStream = new java.io.ByteArrayOutputStream()
					def zipStream = new java.util.zip.GZIPOutputStream(targetStream)
					event("StatusUpdate",["Compressing File ${counter+1} of ${filesToProcess.size()} - ${fileName}"]);
					zipStream.write(outputFile.bytes)
					def zipFile = new File("${outputFile.getAbsolutePath()}.gz")
					def zipFileDigest = new File("${digestedFile.getAbsolutePath()}.gz")
					zipFile.createNewFile()
					zipFileDigest.createNewFile()
					zipStream.finish()

					zipFile.bytes = targetStream.toByteArray()
					zipFileDigest.bytes = targetStream.toByteArray()
					targetStream.close()
				} catch(ex) {
					println("Error Compiling File ${fileName}.${extension}")
				}
			}


		}
		else {
			println("Asset File not found! ${fileName}")
		}
	}


	// Update Manifest
	manifestProperties.store(new File( 'web-app/assets/manifest.properties' ).newWriter(),"")


}
