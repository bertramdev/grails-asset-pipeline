includeTargets << grailsScript("AssetPrecompile")

eventCreateWarStart = {warName, stagingDir ->
	assetPrecompile()
	// ant.exec(executable: "grails", dir: "${basedir}") {
        // arg(value: "asset-precompile")
    // }

}
