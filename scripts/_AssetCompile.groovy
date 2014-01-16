import org.apache.tools.ant.DirectoryScanner
import org.codehaus.groovy.grails.commons.ApplicationHolder

// import asset.pipeline.*
includeTargets << grailsScript("_PackagePlugins")
includeTargets << grailsScript("_GrailsBootstrap")

target(assetClean: "Cleans Compiled Assets Directory") {
	// Clear compiled assets folder
	println "Asset Precompiler Args ${argsMap}"
  def assetDir = new File(argsMap.target ?: "target/assets")
  if(assetDir.exists()) {
  	assetDir.deleteDir()
  }
}

target(assetCompile: "Precompiles assets in the application as specified by the precompile glob!") {
  depends(configureProxy,compile)

  def assetHelper             = classLoader.loadClass('asset.pipeline.AssetHelper')
  def directiveProcessorClass = classLoader.loadClass('asset.pipeline.DirectiveProcessor')
  def assetConfig = [specs:[]] //Additional Asset Specs (Asset File formats) that we want to process.
  event("AssetPrecompileStart", [assetConfig])


  // def manifestMap = [:]
  Properties manifestProperties = new Properties()

  def grailsApplication = ApplicationHolder.getApplication()
  def uglifyJsProcessor =  classLoader.loadClass('asset.pipeline.processors.UglifyJsProcessor').newInstance()
  def minifyJs			  	= grailsApplication.config.grails.assets.containsKey('minifyJs') ? grailsApplication.config.grails.assets.minifyJs : (argsMap.containsKey('minifyJs') ? argsMap.minifyJs == 'true' : true)
  event("StatusUpdate",["Precompiling Assets!"])

  // Load in additional assetSpecs
  assetConfig.specs.each { spec ->
  	def specClass = classLoader.loadClass(spec)
  	if(specClass) {
    	assetHelper.assetSpecs += specClass
  	}
  }

  // Check for existing Compiled Assets
  def assetDir = new File(argsMap.target ?: "target/assets")
  if(assetDir.exists()) {
  	def manifestFile = new File("target/assets/manifest.properties")
  	if(manifestFile.exists())
	  	manifestProperties.load(manifestFile.newDataInputStream())
  } else {
  	assetDir.mkdirs()
  }

	def filesToProcess = getAllAssets(grailsApplication, assetHelper)

	def excludeGzip = ['png', 'jpg', 'jpeg', 'gif']

	if(grailsApplication.config.grails.assets.excludesGzip) {
		excludeGzip += grailsApplication.config.grails.assets.excludesGzip
	}

	removeDeletedFiles(manifestProperties,filesToProcess, assetHelper)


	for(counter = 0 ; counter < filesToProcess.size(); counter++) {
		def isUnchanged = false
		def fileName    = filesToProcess[counter]
		event("StatusUpdate",["Processing File ${counter+1} of ${filesToProcess.size()} - ${fileName}"])
		def extension   = assetHelper.extensionFromURI(fileName)
		fileName        = assetHelper.nameWithoutExtension(fileName)
		def assetFile   = assetHelper.assetForFile(assetHelper.fileForUri(filesToProcess[counter],null,null))
		def digestName

		if(assetFile) {
			def fileData

			if(assetFile.class.name != 'java.io.File') {
				if(assetFile.compiledExtension) {
					extension = assetFile.compiledExtension
					fileName = assetHelper.fileNameWithoutExtensionFromArtefact(fileName,assetFile)
				}
				def contentType = (assetFile.contentType instanceof String) ? assetFile.contentType : assetFile.contentType[0]

				def directiveProcessor = directiveProcessorClass.newInstance(contentType, true)
				fileData   = directiveProcessor.compile(assetFile)
				digestName = assetHelper.getByteDigest(fileData.bytes)
				def existingDigestFile = manifestProperties.getProperty("${fileName}.${extension}")
				if(existingDigestFile && existingDigestFile == "${fileName}-${digestName}.${extension}") {
					isUnchanged=true
				}

				if(fileName.indexOf(".min") == -1 && contentType == 'application/javascript' && minifyJs && !isUnchanged) {
					def newFileData = fileData
					try {
						event("StatusUpdate",["Uglifying File ${counter+1} of ${filesToProcess.size()} - ${fileName}"])

						newFileData = uglifyJsProcessor.process(fileData, grailsApplication.config.grails.assets.minifyOptions ?: [:])

					} catch(e) {
						println "Uglify JS Exception ${e}"
						newFileData = fileData
					}
					fileData = newFileData
				}
				if(assetFile.encoding) {
					fileData = fileData.getBytes(assetFile.encoding)
				} else {
					fileData = fileData.bytes
				}

			} else {
				digestName = assetHelper.getByteDigest(assetFile.bytes)
				def existingDigestFile = manifestProperties.getProperty("${fileName}.${extension}")
				if(existingDigestFile && existingDigestFile == "${fileName}-${digestName}.${extension}") {
					isUnchanged=true
				}
			}

			if(!isUnchanged) {
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
						digestName = assetHelper.getByteDigest(assetFile.file.bytes)
					}
				}
				if(extension) {
					try {

						def digestedFile = new File("target/assets/${fileName}-${digestName}${extension ? ('.' + extension) : ''}")
						digestedFile.createNewFile()
						assetHelper.copyFile(outputFile, digestedFile)
						// digestedFile.sync()
						manifestProperties.setProperty("${fileName}.${extension}", "${fileName}-${digestName}${extension ? ('.' + extension) : ''}")

						// Zip it Good!
						event("StatusUpdate",["Compressing File ${counter+1} of ${filesToProcess.size()} - ${fileName}"])
						if(!excludeGzip.find{ it.toLowerCase() == extension.toLowerCase()}) {
							createCompressedFiles(assetHelper, outputFile, digestedFile)
						}


					} catch(ex) {
						println("Error Compiling File ${fileName}.${extension}")
					}
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

getAllAssets = { grailsApplication, assetHelper ->
	DirectoryScanner scanner = new DirectoryScanner()
	def assetPaths           = assetHelper.getAssetPathsByPlugin()
	def filesToProcess       = []

	assetPaths.each { key, value ->
		scanner.setExcludes(excludesForPlugin(grailsApplication, key) as String[])
		scanner.setIncludes(["**/*"] as String[])
		for(path in value) {
	    scanner.setBasedir(path)
	    scanner.setCaseSensitive(false)
	    scanner.scan()
	    filesToProcess += scanner.getIncludedFiles().flatten()
		}

		scanner.setExcludes([] as String[])
		def includes = includesForPlugin(grailsApplication, key)
		if(includes.size() > 0) {
			scanner.setIncludes(includes as String[])
			for(path in value) {
		    scanner.setBasedir(path)
		    scanner.setCaseSensitive(false)
		    scanner.scan()
		    filesToProcess += scanner.getIncludedFiles().flatten()
			}
		}

	}


	filesToProcess.unique()

	return filesToProcess //Make sure we have a unique set
}

uniqueFilesByCompiledType = { filesToProcess, assetHelper ->
	for(def counter=0; counter < filesToProcess.size() ; counter++) {

	}
}

excludesForPlugin = { grailsApplication, pluginName ->
	def excludes = ["**/.*","**/.DS_Store", 'WEB-INF/**/*', '**/META-INF/*', '**/_*.*']
	if(grailsApplication.config.grails.assets.excludes) {
		excludes += grailsApplication.config.grails.assets.excludes
	}
	if(grailsApplication.config.grails.assets.plugin."${pluginName}".excludes) {
		excludes += grailsApplication.config.grails.assets.plugin."${pluginName}".excludes
	}
	return excludes.unique()
}

includesForPlugin = { grailsApplication, pluginName ->
	def includes = []
	if(grailsApplication.config.grails.assets.includes) {
		includes += grailsApplication.config.grails.assets.includes
	}
	if(grailsApplication.config.grails.assets.plugin."${pluginName}".includes) {
		includes += grailsApplication.config.grails.assets.plugin."${pluginName}".includes
	}
	return includes.unique()
}

createCompressedFiles = { assetHelper, outputFile, digestedFile ->
	def targetStream  = new java.io.ByteArrayOutputStream()
	def zipStream     = new java.util.zip.GZIPOutputStream(targetStream)
	def zipFile       = new File("${outputFile.getAbsolutePath()}.gz")
	def zipFileDigest = new File("${digestedFile.getAbsolutePath()}.gz")

	zipStream.write(outputFile.bytes)
	zipFile.createNewFile()
	zipFileDigest.createNewFile()
	zipStream.finish()

	zipFile.bytes = targetStream.toByteArray()
	assetHelper.copyFile(zipFile, zipFileDigest)
	targetStream.close()
}

removeDeletedFiles = { manifestProperties, filesToProcess, assetHelper ->
		def compiledFileNames = filesToProcess.collect { fileToProcess ->
			def fileName    = fileToProcess
			def extension   = assetHelper.extensionFromURI(fileName)
			fileName        = assetHelper.nameWithoutExtension(fileName)
			def assetFile   = assetHelper.assetForFile(assetHelper.fileForUri(fileToProcess,null,null))
			if(assetFile && assetFile.class.name != 'java.io.File' && assetFile.compiledExtension) {
				extension = assetFile.compiledExtension
				fileName = assetHelper.fileNameWithoutExtensionFromArtefact(fileName,assetFile)
			}
			return "${fileName}.${extension}"
		}

		def propertiesToRemove = []
		manifestProperties.keySet().each { compiledName ->
			def fileFound = compiledFileNames.find{ it == compiledName.toString()}
			if(!fileFound) {
				def digestedName = manifestProperties.getProperty(compiledName)
				def compiledFile = new File("target/assets", compiledName)
				def digestedFile = new File("target/assets", digestedName)
				def zippedFile = new File("target/assets", "${compiledName}.gz")
				def zippedDigestFile = new File("target/assets", "${digestedName}.gz")
				if(compiledFile.exists()) {
					compiledFile.delete()
				}
				if(digestedFile.exists()) {
					digestedFile.delete()
				}
				if(zippedFile.exists()) {
					zippedFile.delete()
				}
				if(zippedDigestFile.exists()) {
					zippedDigestFile.delete()
				}
				propertiesToRemove << compiledName
			}
		}

		propertiesToRemove.each {
			manifestProperties.remove(it)
		}
}
