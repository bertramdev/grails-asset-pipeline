includeTargets << new File(assetPipelinePluginDir, "scripts/_AssetCompile.groovy")

eventCreateWarStart = {warName, stagingDir ->

	assetCompile()

	def assetCompileDir = new File(basedir, "target/assets")
	def assetPathDir = new File(stagingDir, 'assets')
	assetPathDir.mkdirs()

	ant.copy(todir:assetPathDir.path, verbose:true) {
		fileset dir:assetCompileDir
	}
}
