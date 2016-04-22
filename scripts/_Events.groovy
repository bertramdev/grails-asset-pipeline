

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
	def fileSystemAssetResolver  = classLoader.loadClass('asset.pipeline.fs.FileSystemAssetResolver')

	def fsResolver = fileSystemAssetResolver.newInstance("pluginPackage",new File(basedir, "grails-app/assets").canonicalPath)
	def assetDirs = new File(basedir, "grails-app/assets")
	def assetPathDir = new File(stagingDir, 'META-INF/assets')
	assetPathDir.mkdirs()
	def fileList = fsResolver.scanForFiles([],[])
    def manifestNames = []
        fileList.eachWithIndex { assetFile, index ->
            "Packaging File ${index+1} of ${fileList.size()} - ${assetFile.path}"
            manifestNames << assetFile.path
            File outputFile = new File(assetPathDir,assetFile.path)
            if(!outputFile.exists()) {
                outputFile.parentFile.mkdirs()
                outputFile.createNewFile()
            }
            InputStream sourceStream
            OutputStream outputStream
            try {
                sourceStream = assetFile.inputStream
                outputStream = outputFile.newOutputStream()

                outputStream << sourceStream
            } finally {
                try {
                    sourceStream.close()
                } catch(ex1) {
                    //silent fail
                }
                try {
                    outputStream.flush()
                    outputStream.close()
                } catch(ex) {
                    //silent fail
                }

            }

        }
        File assetList = new File(stagingDir,"META-INF/assets.list")
        if(!assetList.exists()) {
            assetList.parentFile.mkdirs()
            assetList.createNewFile()
        }
        OutputStream assetListOs
        try {
            assetListOs = assetList.newOutputStream()
            assetListOs <<  manifestNames.join("\n");
        } finally {
            assetListOs.flush()
            assetListOs.close()
        }
}

eventCleanStart = {
    ant.delete('dir':'target/assets')
}
