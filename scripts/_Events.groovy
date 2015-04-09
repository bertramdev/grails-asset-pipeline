

eventCreateWarStart = {warName, stagingDir ->
	includeTargets << new File(assetPipelinePluginDir, "scripts/_AssetCompile.groovy")
	assetCompile()

	def assetCompileDir = new File(basedir, "target/assets")
	def assetPathDir = new File(stagingDir, 'assets')
	assetPathDir.mkdirs()

	ant.copy(todir:assetPathDir.path, verbose:true) {
		fileset dir:assetCompileDir
	}
}

eventCreatePluginArchiveStart = { stagingDir ->
	event("StatusUpdate",["Packaging Assets into Binary!"])

	def assetDirs = new File(basedir, "grails-app/assets")
	def assetPathDir = new File(stagingDir, 'META-INF/assets')
	assetPathDir.mkdirs()
	assetDirs.listFiles().each { file ->
		if(file.isDirectory()) {
			println "Copying Files From ${file.path}"
			ant.copy(todir: assetPathDir.path, verbose: true) {
				fileset dir: file
			}
		}
	}
}

eventCleanStart = {
    ant.delete('dir':'target/assets')
}
