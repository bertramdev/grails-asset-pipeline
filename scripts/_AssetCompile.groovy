import java.security.MessageDigest
import org.apache.tools.ant.DirectoryScanner
import org.codehaus.groovy.grails.commons.ApplicationHolder

// import asset.pipeline.*
includeTargets << grailsScript("Init")
includeTargets << grailsScript("_GrailsBootstrap")

target(assetCompile: "Precompiles assets in the application as specified by the precompile glob!") {
    depends(configureProxy,compile)
    def assetHelper             = classLoader.loadClass('asset.pipeline.AssetHelper')
    def directiveProcessorClass = classLoader.loadClass('asset.pipeline.DirectiveProcessor')
    def assetSpecs = [specs:[]] //Additional Asset Specs (Asset File formats) that we want to process.
    event("AssetPrecompileStart", [assetSpecs])


    // def manifestMap = [:]
    Properties manifestProperties = new Properties()

    def grailsApplication = ApplicationHolder.getApplication()
    def uglifyJsProcessor =  classLoader.loadClass('asset.pipeline.processors.UglifyJsProcessor').newInstance()
    def minifyJs			  	= grailsApplication.config.grails.assets.containsKey('minifyJs') ? grailsApplication.config.grails.assets.minifyJs : true
    event("StatusUpdate",["Precompiling Assets!"])

    // Load in additional assetspecs
    assetHelper.assetSpecs += assetSpecs.specs

    // Clear compiled assets folder
    def assetDir = new File("target/assets")
    if(assetDir.exists()) {
    	assetDir.deleteDir()
    }


	//Find all files we want to process
	def excludes = ["**/.*","**/.DS_Store", 'WEB-INF/**/*', '**/META-INF/*']
	if(grailsApplication.config.grails.assets.excludes) {
		excludes += grailsApplication.config.grails.assets.excludes
	}
	DirectoryScanner scanner = new DirectoryScanner()
	scanner.setExcludes(excludes as String[])
	// scanner.setIncludes(["**/*"] as String[])

	def assetPaths = assetHelper.getAssetPaths()
	def filesToProcess = []
	for(path in assetPaths) {
	    scanner.setBasedir(path)
	    scanner.setCaseSensitive(false)
	    scanner.scan()
	    filesToProcess += scanner.getIncludedFiles().flatten()
	}
	filesToProcess.unique() //Make sure we have a unique set
	// println "Here is the spec 2: ${asset.pipeline.AssetHelper.assetSpecs}"
	for(counter = 0 ; counter < filesToProcess.size(); counter++) {
		def fileName = filesToProcess[counter]
		event("StatusUpdate",["Processing File ${counter+1} of ${filesToProcess.size()} - ${fileName}"])
		def extension = assetHelper.extensionFromURI(fileName)
		fileName  = assetHelper.nameWithoutExtension(fileName)

		def assetFile = assetHelper.artefactForFile(assetHelper.fileForUri(filesToProcess[counter],null,null))
		if(assetFile) {
			def fileData

			if(assetFile.class.name != 'java.io.File') {
				if(assetFile.compiledExtension) {
					extension = assetFile.compiledExtension
					fileName = assetHelper.fileNameWithoutExtensionFromArtefact(fileName,assetFile)
				}
				def directiveProcessor = directiveProcessorClass.newInstance(assetFile.contentType)
				fileData = directiveProcessor.compile(assetFile)
				if(fileName.indexOf(".min") == -1 && assetFile.contentType == 'application/javascript' && minifyJs) {
					def newFileData = fileData
					try {
						event("StatusUpdate",["Uglifying File ${counter+1} of ${filesToProcess.size()} - ${fileName}"])

						newFileData = uglifyJsProcessor.process(fileData)

					} catch(e) {
						println "Uglify JS Exception ${e}"
						newFileData = fileData
					}
					fileData = newFileData
				}
				fileData = fileData.getBytes('utf-8')
			}

			def outputFileName = fileName
			if(extension) {
				outputFileName = "${fileName}.${extension}"
			}
			def outputFile = new File("target/assets/${outputFileName}")

			def parentTree = new File(outputFile.parent)
			parentTree.mkdirs()
			outputFile.createNewFile()

			if(fileData) {
				def outputStream = outputFile.newOutputStream()
				outputStream.write(fileData, 0 , fileData.length)
				outputStream.flush()
				outputStream.close()
			} else {
				if(assetFile.class.name == 'java.io.File') {
					assetHelper.copyFile(assetFile, outputFile)
				} else {
					assetHelper.copyFile(assetFile.file, outputFile)
				}
			}

			if(extension) {
				try {
					// Generate Checksum
					MessageDigest md = MessageDigest.getInstance("MD5")
					md.update(outputFile.bytes)
					def checksum = md.digest()
					def digestedFile = new File("target/assets/${fileName}-${checksum.encodeHex()}${extension ? ('.' + extension) : ''}")
					digestedFile.createNewFile()
					assetHelper.copyFile(outputFile, digestedFile)
					// digestedFile.sync()
					manifestProperties.setProperty("${fileName}.${extension}", "${fileName}-${checksum.encodeHex()}${extension ? ('.' + extension) : ''}")

					// Zip it Good!
					def targetStream = new java.io.ByteArrayOutputStream()
					def zipStream = new java.util.zip.GZIPOutputStream(targetStream)
					event("StatusUpdate",["Compressing File ${counter+1} of ${filesToProcess.size()} - ${fileName}"])
					zipStream.write(outputFile.bytes)
					def zipFile = new File("${outputFile.getAbsolutePath()}.gz")
					def zipFileDigest = new File("${digestedFile.getAbsolutePath()}.gz")
					zipFile.createNewFile()
					zipFileDigest.createNewFile()
					zipStream.finish()

					zipFile.bytes = targetStream.toByteArray()
					assetHelper.copyFile(zipFile, zipFileDigest)
					// zipFile.sync()
					// zipFileDigest.sync()
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
	def manifestFile = new File('target/assets/manifest.properties')
	manifestProperties.store(manifestFile.newWriter(),"")
}
