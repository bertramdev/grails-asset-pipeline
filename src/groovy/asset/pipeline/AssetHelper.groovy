package asset.pipeline

import grails.util.Holders

import java.nio.channels.FileChannel

import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

import java.util.regex.Pattern

class AssetHelper {

	static assetSpecs = [JsAssetFile, CssAssetFile]

	static QUOTED_FILE_SEPARATOR = Pattern.quote(File.separator)
	static DIRECTIVE_FILE_SEPARATOR = '/'

	static fileForUri(uri, contentType=null,ext=null, baseFile=null) {

		def grailsApplication = Holders.getGrailsApplication()

		if(contentType) {
			def possibleFileSpecs = AssetHelper.getPossibleFileSpecs(contentType)
			if(possibleFileSpecs) {
					def file =  AssetHelper.fileForUriIfHasAnyAssetType(uri, possibleFileSpecs, baseFile)
					if(file) return file
			}
			else {
				def assetFile = AssetHelper.fileForFullName(uri + "." + ext)
				if(assetFile) {
					return assetFile
				}
			}
		} else {
				return AssetHelper.getAssetFileWithExtension(uri, ext)
		}

		return null
	}

	static assetFileClasses() {
		return AssetHelper.assetSpecs
	}

	static artefactForFile(file,contentType, baseFile=null) {
		if(contentType == null || file == null) {
			return file
		}

		def grailsApplication = Holders.getGrailsApplication()
		def possibleFileSpecs = AssetHelper.getPossibleFileSpecs(contentType)
		for(fileSpec in possibleFileSpecs) {
			for(extension in fileSpec.extensions) {
				def fileName = file.getAbsolutePath()
				if(fileName.endsWith("." + extension)) {
					return fileSpec.newInstance(file, baseFile)
				}
			}
		}

		return file
	}

	static artefactForFile(file) {
		if(file == null) {
			return file
		}

		def possibleFileSpec = AssetHelper.artefactForFileName(file.getName())
		if(possibleFileSpec) {
			return possibleFileSpec.newInstance(file)
		}
		return file
	}

	static artefactForFileName(filename) {
		def grailsApplication = Holders.getGrailsApplication()
		return AssetHelper.assetFileClasses().find{ fileClass ->
			fileClass.extensions.find { filename.endsWith(".${it}") }
		}
	}

	static fileForFullName(uri) {
		def assetPaths = AssetHelper.getAssetPaths()
		for(assetPath in assetPaths) {
			def path = [assetPath, uri].join(File.separator)
			def fileDescriptor = new File(path)

			if(fileDescriptor.exists() && fileDescriptor.file) {
				return fileDescriptor
			}
		}
		return null
	}

	static getAssetPaths() {
		def assetPaths = AssetHelper.scopedDirectoryPaths(new File("grails-app/assets").getAbsolutePath())

		for(plugin in GrailsPluginUtils.pluginInfos) {
			def assetPath = [plugin.pluginDir.getPath(), "grails-app", "assets"].join(File.separator)
			def fallbackPath = [plugin.pluginDir.getPath(), "web-app"].join(File.separator)
			assetPaths += AssetHelper.scopedDirectoryPaths(assetPath)
			assetPaths += AssetHelper.scopedDirectoryPaths(fallbackPath)
		}
		return assetPaths.unique()
	}

	static scopedDirectoryPaths(assetPath) {
		def assetPaths = []
		def assetFile = new File(assetPath)
		if(assetFile.exists()) {
			def scopedDirectories = assetFile.listFiles()
			for(scopedDirectory in scopedDirectories) {
				if(scopedDirectory.isDirectory() && scopedDirectory.getName() != "WEB-INF" && scopedDirectory.getName() != 'META-INF') {
					assetPaths << scopedDirectory.getAbsolutePath()
				}

			}
		}
		return assetPaths
	}

	static extensionFromURI(uri) {

		def extension
		if(uri.lastIndexOf(".") >= 0) {
			extension = uri.substring(uri.lastIndexOf(".") + 1)
		}
		return extension
	}

	static nameWithoutExtension(uri) {
		if(uri.lastIndexOf(".") >= 0) {
			return uri.substring(0,uri.lastIndexOf("."))
		}
		return uri
	}

	static fileNameWithoutExtensionFromArtefact(filename,assetFile) {
		if(assetFile == null) {
			return null
		}

		def rootName = filename
		assetFile.extensions.each { extension ->

			if(filename.endsWith(".${extension}")) {
				def potentialName = filename.substring(0,filename.lastIndexOf(".${extension}"))
				if(potentialName.length() < rootName.length()) {
					rootName = potentialName
				}
			}
		}
		return rootName
	}

	static assetMimeTypeForURI(uri) {
		def fileSpec = artefactForFileName(uri)
		if(fileSpec) {
			return fileSpec.contentType
		}
		return null
	}

	/**
	* Copies a files contents from one file to another and flushes.
	* Note: We use FileChannel instead of FileUtils.copyFile to ensure a synchronous forced save.
	* This helps ensures files exist on the disk before a war file is created.
	* @param sourcceFile the originating file we want to copy
	* @param destFile the destination file object we want to save to
	*/
	static void copyFile(File sourceFile, File destFile) throws IOException {
	 if(!destFile.exists()) {
		destFile.createNewFile()
	 }

	 FileChannel source
	 FileChannel destination
	 try {
		source = new FileInputStream(sourceFile).getChannel()
		destination = new FileOutputStream(destFile).getChannel()
		destination.transferFrom(source, 0, source.size())
		destination.force(true)
	 }
	 finally {
			source?.close()
			destination?.close()
		}
	}

	/**
	 *
	 * @param uri the string of the asset uri.
	 * @param possibleFileSpecs is a list of possible file specs that the file for the uri can belong to.
	 * @return an AssetFile for the corresponding uri.
	 */
	static fileForUriIfHasAnyAssetType(String uri, possibleFileSpecs, baseFile=null) {
			for(fileSpec in possibleFileSpecs) {
					for(extension in fileSpec.extensions) {

							def fullName = uri
							if(fullName.endsWith(".${fileSpec.compiledExtension}")) {
									fullName = fullName.substring(0,fullName.lastIndexOf(".${fileSpec.compiledExtension}"))
							}
							if(!fullName.endsWith("." + extension)) {
									fullName += "." + extension
							}

							def file = AssetHelper.fileForFullName(fullName)
							if(file) {
									return fileSpec.newInstance(file, baseFile)
							}
					}

			}
	}

	/**
	 *
	 * @param uri string representation of the asset file.
	 * @param ext the extension of the file
	 * @return An instance of the file that the uri belongs to.
	 */
	static getAssetFileWithExtension(String uri, String ext) {
			def fullName = uri
			if(ext) {
					fullName = uri + "." + ext
			}
			def assetFile = AssetHelper.fileForFullName(fullName)
			if(assetFile) {
					return assetFile
			}
	}

	static getPossibleFileSpecs(String contentType){
			AssetHelper.assetFileClasses().findAll { it.contentType == contentType }
	}


}
