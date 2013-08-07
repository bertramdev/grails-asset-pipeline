package asset.pipeline.processors
import asset.pipeline.*

class CssProcessor {

	CssProcessor() {
	}

	def process(inputText, assetFile) {
		def cachedPaths = [:]
		return inputText.replaceAll(/url\([\'\"]?([a-zA-Z0-9\-\_\.\/\@]+)[\'\"]?\)/) { fullMatch, assetPath ->
			def replacementPath = assetPath
			if(cachedPaths[assetPath]) {
				replacementPath = cachedPaths[assetPath]
			} else if(isRelativePath(assetPath)) {
				def relativeFileName = [relativePath(assetFile.file),assetPath].join(File.separator)
				// println "Found Relative Path ${assetPath} - Relative: ${relativeFileName}"
				def cssFile = AssetHelper.fileForFullName(relativeFileName)
				if(cssFile) {
					replacementPath = relativePathToBaseFile(cssFile, assetFile.baseFile ?: assetFile.file)
					cachedPaths[assetPath] = replacementPath
				}
			}
			return "url('${replacementPath}')"
		}

	}

	private isRelativePath(assetPath) {
		return !assetPath.startsWith("/") && !assetPath.startsWith("http")
	}

	private relativePathToBaseFile(file, baseFile) {
		def baseRelativePath = relativePath(baseFile).split(AssetHelper.DIRECTIVE_FILE_SEPARATOR).reverse()
		def currentRelativePath = relativePath(file, true).split(AssetHelper.DIRECTIVE_FILE_SEPARATOR).reverse()

		def filePathIndex=currentRelativePath.size()- 1
		def baseFileIndex=baseRelativePath.size() - 1

		while(filePathIndex > 0 && baseFileIndex > 0 && baseRelativePath[baseFileIndex] == currentRelativePath[filePathIndex]) {
			filePathIndex--
			baseFileIndex--
		}

		def calculatedPath = []

		// for each remaining level in the home path, add a ..
		for(;baseFileIndex>=0;baseFileIndex--) {
			calculatedPath << ".."
		}

		for(;filePathIndex>=0;filePathIndex--) {
			calculatedPath << currentRelativePath[filePathIndex]
		}


		return calculatedPath.join(AssetHelper.DIRECTIVE_FILE_SEPARATOR)
	}

	private relativePath(file, includeFileName=false) {
		def path
		if(includeFileName) {
			path = file.class.name == 'java.io.File' ? file.getCanonicalPath().split(AssetHelper.QUOTED_FILE_SEPARATOR) : file.file.getCanonicalPath().split(AssetHelper.QUOTED_FILE_SEPARATOR)
		} else {
			path = file.class.name == 'java.io.File' ? file.getParent().split(AssetHelper.QUOTED_FILE_SEPARATOR) : file.file.getParent().split(AssetHelper.QUOTED_FILE_SEPARATOR)
		}

		def startPosition = path.findLastIndexOf{ it == "grails-app" }
		if(startPosition == -1) {
			startPosition = path.findLastIndexOf{ it == 'web-app' }
			if(startPosition+2 >= path.length) {
				return ""
			}
			path = path[(startPosition+2)..-1]
		}
		else {
			if(startPosition+3 >= path.length) {
				return ""
			}
			path = path[(startPosition+3)..-1]
		}

		return path.join(AssetHelper.DIRECTIVE_FILE_SEPARATOR)
	}


}
