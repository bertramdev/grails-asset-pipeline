// includeTargets << grailsScript("_Precompile")

eventCreateWarStart = {warName, stagingDir ->
	// assetPrecompile()
	ant.exec(executable: "grails", dir: "${basedir}") {
        arg(value: "asset-precompile")
    }

}
