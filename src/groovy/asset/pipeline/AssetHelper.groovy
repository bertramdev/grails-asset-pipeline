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
            // println "Checking Extension : ${extension}"
            def fullName = uri
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
        // println "Looking for file: ${uri + "." + ext}"
        def assetFile = AssetHelper.fileForFullName(uri + "." + ext)
        if(assetFile) {
          return assetFile
        }
      }
    } else {
      // println "Looking for file: ${uri  + "." + ext}"
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
    def grailsApplication = Holders.getGrailsApplication()
    return grailsApplication.assetFileClasses
  }

  static artefactForFile(file,contentType=null) {
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

  static artefactForFileWithExtension(file, extension) {
    if(extension == null || file == null) {
      return file
    }

    def possibleFileSpec = AssetHelper.artefactForExtension(extension)
    if(possibleFileSpec) {
      return possibleFileSpec.newInstance(file)
    }
    return file
  }

  static artefactForExtension(extension) {
    def grailsApplication = Holders.getGrailsApplication()
    return AssetHelper.assetFileClasses().find{ it.extensions.contains(extension) }
  }

  static fileForFullName(uri) {
    def assetPaths = AssetHelper.getAssetPaths()
    for(assetPath in assetPaths) {
      def path = [assetPath, uri].join(File.separator)
      def fileDescriptor = new File(path)
      // println "Checking ${path}"
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
      assetPaths += AssetHelper.scopedDirectoryPaths(assetPath)
    }
    return assetPaths.unique()
  }

  static scopedDirectoryPaths(assetPath) {
    def assetPaths = []
    def assetFile = new File(assetPath)
    if(assetFile.exists()) {
      def scopedDirectories = assetFile.listFiles()
      for(scopedDirectory in scopedDirectories) {
        if(scopedDirectory.isDirectory()) {
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

  static assetMimeTypeForURI(uri) {
    def extension = AssetHelper.extensionFromURI(uri)
    def fileSpec = artefactForExtension(extension)
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
