package asset.pipeline.fs

import asset.pipeline.*
import java.util.regex.Pattern
import groovy.util.logging.Log4j

@Log4j
class FileSystemAssetResolver extends AbstractAssetResolver {
	static QUOTED_FILE_SEPARATOR = Pattern.quote(File.separator)
	static DIRECTIVE_FILE_SEPARATOR = '/'

	File baseDirectory
	def scanDirectories = []

	FileSystemAssetResolver(String name,String basePath, boolean flattenSubDirectories=true) {
		this.name = name
		baseDirectory = new File(basePath)
		if(baseDirectory.exists()) {
			if(flattenSubDirectories) {
				def scopedDirectories = baseDirectory.listFiles()
				for(scopedDirectory in scopedDirectories) {
					if(scopedDirectory.isDirectory() && scopedDirectory.getName() != "WEB-INF" && scopedDirectory.getName() != 'META-INF') {
						scanDirectories << scopedDirectory.canonicalPath
					}
				}
			} else {
				scanDirectories << baseDirectory.canonicalPath
			}

		}
		log.debug "Asset Pipeline FSResolver Initialized with Scan Directories: ${scanDirectories}"
	}


	//TODO: WINDOWS SUPPORT USE QUOTED FILE SEPARATORS
	public def getAsset(String relativePath, String contentType = null, String extension = null, AssetFile baseFile=null) {
		if(!relativePath) {
			return null
		}

		def specs
		if(contentType) {
			specs = AssetHelper.getPossibleFileSpecs(contentType)
		}

		for(directoryPath in scanDirectories) {
			if(specs) {
				for(fileSpec in specs) {
					def fileName = relativePath
					if(fileName.endsWith(".${fileSpec.compiledExtension}")) {
						fileName = fileName.substring(0,fileName.lastIndexOf(".${fileSpec.compiledExtension}"))
					}
					for(ext in fileSpec.extensions) {
						def tmpFileName = fileName
						if(!tmpFileName.endsWith("." + ext)) {
							tmpFileName += "." + ext
						}
						def file = new File(directoryPath, tmpFileName)
						if(file.exists()) {
							return fileSpec.newInstance(inputStreamSource: { file.newInputStream() }, baseFile: baseFile, path: relativePathToResolver(file,directoryPath), sourceResolver: this)
						}
					}
				}
			} else {
				def fileName = relativePath
				if(extension) {
					if(!fileName.endsWith(".${extension}")) {
						fileName += ".${extension}"
					}
				}
				def file = new File(directoryPath, fileName)
				if(file.exists()) {
					return new GenericAssetFile(inputStreamSource: { file.newInputStream() }, path: relativePathToResolver(file,directoryPath))
				}
			}
		}
		return null
	}


	/**
	* Implementation Requirements
	* Should be able to take a relative to baseFile scenario
	* FIXME: Make sure they cannot traverse up levels too far
	*/
	public def getAssets(String basePath, String contentType = null, String extension = null,  Boolean recursive = true, AssetFile relativeFile=null, AssetFile baseFile = null) {
		//We are going absolute
		def fileList = []

		if(!basePath.startsWith('/') && relativeFile != null) {
			def pathArgs = relativeFile.path.split(DIRECTIVE_FILE_SEPARATOR) //(path should be relative not canonical)
			def basePathArgs = basePath.split(DIRECTIVE_FILE_SEPARATOR)
			def parentPathArgs = pathArgs[0..(pathArgs.size() - 2)]
			parentPathArgs.addAll(basePathArgs)
			basePath = (parentPathArgs).join(File.separator)
			println "Scanning with new Base Path: ${basePath}"
		}

		for(directoryPath in scanDirectories) {
			def file = new File(directoryPath,basePath)
			if(file.exists() && file.isDirectory()) {
				recursiveTreeAppend(file, fileList, contentType,baseFile,recursive, directoryPath)
			}
		}
		return fileList
	}

	def recursiveTreeAppend(directory,tree,contentType=null, baseFile, recursive=true, sourceDirectory) {
		def files = directory.listFiles()
		files = files?.sort { a, b -> a.name.compareTo b.name }
		for(file in files) {
			if(file.isDirectory() && recursive) {
				recursiveTreeAppend(file,tree, contentType, baseFile, recursive, sourceDirectory)
			}
			else if(contentType in AssetHelper.assetMimeTypeForURI(file.getAbsolutePath())) {
				tree << assetForFile(file,contentType, baseFile, sourceDirectory)
			}
		}
	}

	def assetForFile(file,contentType, baseFile=null, sourceDirectory) {
		if(file == null) {
			return null
		}

		if(contentType == null) {
			return new GenericAssetFile(inputStreamSource: { file.newInputStream() }, path: file.canonicalPath)
		}

		def possibleFileSpecs = AssetHelper.getPossibleFileSpecs(contentType)
		for(fileSpec in possibleFileSpecs) {
			for(extension in fileSpec.extensions) {
				def fileName = file.getAbsolutePath()
				if(fileName.endsWith("." + extension)) {
					return fileSpec.newInstance(inputStreamSource: { file.newInputStream() }, baseFile: baseFile, path: relativePathToResolver(file,sourceDirectory), sourceResolver: this)
				}
			}
		}
		return file
	}

	def relativePathToResolver(file, scanDirectoryPath) {
		def filePath = file.canonicalPath

		if(filePath.startsWith(scanDirectoryPath)) {
			return filePath.substring(scanDirectoryPath.size() + 1).replace(QUOTED_FILE_SEPARATOR, DIRECTIVE_FILE_SEPARATOR)
		} else {
			for(scanDir in scanDirectories) {
				if(filePath.startsWith(scanDir)) {
					return filePath.substring(scanDir.size() + 1).replace(QUOTED_FILE_SEPARATOR, DIRECTIVE_FILE_SEPARATOR)
				}
			}
			throw RuntimeException("File was not sourced from the same ScanDirectory #{filePath}")
		}
	}

	def fileSystemPathFromDirectivePath(directivePath) {
		return directivePath?.replace(DIRECTIVE_FILE_SEPARATOR, File.separator)
	}

}
