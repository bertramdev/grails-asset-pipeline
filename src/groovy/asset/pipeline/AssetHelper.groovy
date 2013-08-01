package asset.pipeline

import grails.util.Holders

import java.nio.channels.FileChannel

import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

class AssetHelper {

  static assetSpecs = [JsAssetFile, CssAssetFile]

  static fileForUri(uri, contentType=null,ext=null) {

    def grailsApplication = Holders.getGrailsApplication()

    if(contentType) {
      def possibleFileSpecs = AssetHelper.assetFileClasses().findAll { it.contentType == contentType }
      if(possibleFileSpecs) {

        for(fileSpec in possibleFileSpecs) {
          for(extension in fileSpec.extensions) {

            def fullName = uri
            if(fullName.endsWith(".${fileSpec.compiledExtension}")) {
              fullName = fullName.substring(0,fullName.lastIndexOf(".${fileSpec.compiledExtension}"))
            }
            if(!fullName.endsWith("." + extension)) {
              fullName += "." + extension
            }

            def file = AssetHelper.fileForFullName(fullName)
            if(file) {
              return fileSpec.newInstance(file)
            }
          }

        }
      }
      else {

        def assetFile = AssetHelper.fileForFullName(uri + "." + ext)
        if(assetFile) {
          return assetFile
        }
      }
    } else {

      def fullName = uri
      if(ext) {
        fullName = uri + "." + ext
      }
      def assetFile = AssetHelper.fileForFullName(fullName)
      if(assetFile) {
        return assetFile
      }
    }

    return null
  }

  static assetFileClasses() {
    return AssetHelper.assetSpecs
  }

  static artefactForFile(file,contentType) {
    if(contentType == null || file == null) {
      return file
    }

    def grailsApplication = Holders.getGrailsApplication()
    def possibleFileSpecs = AssetHelper.assetFileClasses().findAll { it.contentType == contentType }
    for(fileSpec in possibleFileSpecs) {
      for(extension in fileSpec.extensions) {
        def fileName = file.getAbsolutePath()
        if(fileName.endsWith("." + extension)) {
          return fileSpec.newInstance(file)
        }
      }
    }

    return file
  }

  static artefactForFile(file) {
    if(file == null) {
      return file
    }

    def possibleFileSpec = AssetHelper.artefactForFileName(file.getName())
    if(possibleFileSpec) {
      return possibleFileSpec.newInstance(file)
    }
    return file
  }

  static artefactForFileName(filename) {
    def grailsApplication = Holders.getGrailsApplication()
    return AssetHelper.assetFileClasses().find{ fileClass ->
      fileClass.extensions.find { filename.endsWith(".${it}") }
    }
  }

  static fileForFullName(uri) {
    def assetPaths = AssetHelper.getAssetPaths()
    for(assetPath in assetPaths) {
      def path = [assetPath, uri].join(File.separator)
      def fileDescriptor = new File(path)

      if(fileDescriptor.exists()) {
        return fileDescriptor
      }
    }
    return null
  }

  static getAssetPaths() {
    def assetPaths = AssetHelper.scopedDirectoryPaths(new File("grails-app/assets").getAbsolutePath())

    for(plugin in GrailsPluginUtils.pluginInfos) {
      def assetPath = [plugin.pluginDir.getPath(),"grails-app/assets"].join(File.separator)
      def fallbackPath = [plugin.pluginDir.getPath(),"web-app"].join(File.separator)
      assetPaths += AssetHelper.scopedDirectoryPaths(assetPath)

      assetPaths += AssetHelper.scopedDirectoryPaths(fallbackPath)

    }
    return assetPaths.unique()
  }

  static scopedDirectoryPaths(assetPath) {
    def assetPaths = []
    def assetFile = new File(assetPath)
    if(assetFile.exists()) {
      def scopedDirectories = assetFile.listFiles()
      for(scopedDirectory in scopedDirectories) {
        if(scopedDirectory.isDirectory() && scopedDirectory.getName() != "WEB-INF" && scopedDirectory.getName() != 'META-INF') {
          assetPaths << scopedDirectory.getAbsolutePath()
        }

      }
    }
    return assetPaths
  }

  static extensionFromURI(uri) {

    def extension
    if(uri.lastIndexOf(".") >= 0) {
      extension = uri.substring(uri.lastIndexOf(".") + 1)
    }
    return extension
  }

  static nameWithoutExtension(uri) {
    if(uri.lastIndexOf(".") >= 0) {
      return uri.substring(0,uri.lastIndexOf("."))
    }
    return uri
  }

  static fileNameWithoutExtensionFromArtefact(filename,assetFile) {
    if(assetFile == null) {
      return null
    }

    def rootName = filename
    assetFile.extensions.each { extension ->

      if(filename.endsWith(".${extension}")) {
        def potentialName = filename.substring(0,filename.lastIndexOf(".${extension}"))
        if(potentialName.length() < rootName.length()) {
          rootName = potentialName
        }
      }
    }
    return rootName
  }

  static assetMimeTypeForURI(uri) {
    def fileSpec = artefactForFileName(uri)
    if(fileSpec) {
      return fileSpec.contentType
    }
    return null
  }

  static void copyFile(File sourceFile, File destFile) throws IOException {
   if(!destFile.exists()) {
    destFile.createNewFile()
   }

   FileChannel source
   FileChannel destination
   try {
    source = new FileInputStream(sourceFile).getChannel()
    destination = new FileOutputStream(destFile).getChannel()
    destination.transferFrom(source, 0, source.size())
    destination.force(true)
   }
   finally {
      source?.close()
      destination?.close()
    }
  }
}
