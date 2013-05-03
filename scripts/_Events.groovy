includeTargets << new File("${assetPipelinePluginDir}/scripts/_AssetCompile.groovy")

eventCreateWarStart = {warName, stagingDir ->
	// assetCompile()
	// ant.exec(executable: "grails", dir: "${basedir}") {
 //        arg(value: "asset-precompile")
 //    }

}
