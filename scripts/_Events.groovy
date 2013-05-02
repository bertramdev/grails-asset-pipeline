// includeTargets << grailsScript("AssetPrecompile")
eventCreateWarStart = {warName, stagingDir ->
	ant.exec(executable: "grails", dir: "${basedir}") {
        arg(value: "asset-precompile")
    }

}
