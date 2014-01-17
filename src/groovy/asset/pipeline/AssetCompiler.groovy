package asset.pipeline
import org.apache.tools.ant.DirectoryScanner

class AssetCompiler {

	def includeRules = [:]
	def excludeRules = [:]
	def assetPaths = [:]

	static compressExcludesExtensions = ['png', 'jpg','jpeg', 'gif', 'zip', 'gz']

	void addPaths(String key, String paths) {
		// If key is not specified, group with "application"

	}

	void removePathsByKey(String key) {
		assetPaths.remove(key)
	}

	def getIncludesForPathKey(String key) {
		def includes = []
		def defaultIncludes = includeRules.default
		if(defaultIncludes) {
			includes += defaultIncludes
		}
		if(includeRules[key]) {
			includes += includeRules[key]
		}
		return inclues.unique()
	}

	def getExcludesForPathKey(String key) {
		def excludes = ["**/.*","**/.DS_Store", 'WEB-INF/**/*', '**/META-INF/*', '**/_*.*']
		def defaultExcludes = excludeRules.default
		if(defaultExcludes) {
			excludes += defaultExcludes
		}
		if(excludeRules[key]) {
			excludes += excludeRules[key]
		}
		return excludes.unique()
	}


	// def getAllAssets { grailsApplication, assetHelper ->
	// 	DirectoryScanner scanner = new DirectoryScanner()
	// 	def assetPaths           = assetHelper.getAssetPathsByPlugin()
	// 	def filesToProcess       = []

	// 	assetPaths.each { key, value ->
	// 		scanner.setExcludes(excludesForPlugin(grailsApplication, key) as String[])
	// 		scanner.setIncludes(["**/*"] as String[])
	// 		for(path in value) {
	// 	    scanner.setBasedir(path)
	// 	    scanner.setCaseSensitive(false)
	// 	    scanner.scan()
	// 	    filesToProcess += scanner.getIncludedFiles().flatten()
	// 		}

	// 		scanner.setExcludes([] as String[])
	// 		def includes = includesForPlugin(grailsApplication, key)
	// 		if(includes.size() > 0) {
	// 			scanner.setIncludes(includes as String[])
	// 			for(path in value) {
	// 		    scanner.setBasedir(path)
	// 		    scanner.setCaseSensitive(false)
	// 		    scanner.scan()
	// 		    filesToProcess += scanner.getIncludedFiles().flatten()
	// 			}
	// 		}

	// 	}


	// 	filesToProcess.unique()

	// 	return filesToProcess //Make sure we have a unique set
	// }
}
