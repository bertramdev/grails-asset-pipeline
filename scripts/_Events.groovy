includeTargets << new File(assetPipelinePluginDir, "scripts/_AssetCompile.groovy")

eventCreateWarStart = {warName, stagingDir ->

  def assetDir = new File("web-app/assets")
  // Warn of
  if(!assetDir.exists()) {
    event("StatusError",["It appears you have not precompiled your assets. Please do this before generating your WAR file! Run `grails asset-precompile` to generate your web-app/assets compiled assets."])
  }
	// assetCompile()
	// ant.exec(executable: "grails", dir: "${basedir}") {
 	//        arg(value: "asset-precompile")
  //}

}
