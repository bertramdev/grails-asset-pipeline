package asset.pipeline
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

class AssetHelper {

  static def fileForUri(uri, contentType=null,ext=null) {
    def grailsApplication = grails.util.Holders.getGrailsApplication()

    if(contentType) {
      def possibleFileSpecs = grailsApplication.assetFileClasses.findAll { it.getPropertyValue('contentType') == contentType }
      if(possibleFileSpecs) {
        for(fileSpec in possibleFileSpecs) {
          for(extension in fileSpec.getPropertyValue('extensions')) {
            // println "Checking Extension : ${extension}"
            def fullName = uri
            if(!fullName.endsWith("." + extension)) {
              fullName += "." + extension
            }
            def file = AssetHelper.fileForFullName(fullName)
            if(file) {
              return fileSpec.clazz.newInstance(file)
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


    return null;
  }

  static def artefactForFile(file,contentType=null) {
    if(contentType == null || file == null) {
      return file;
    }

    def grailsApplication = grails.util.Holders.getGrailsApplication()
    def possibleFileSpecs = grailsApplication.assetFileClasses.findAll { it.getPropertyValue('contentType') == contentType }
    for(fileSpec in possibleFileSpecs) {
      for(extension in fileSpec.getPropertyValue('extensions')) {
        def fileName = file.getAbsolutePath()
        if(fileName.endsWith("." + extension)) {
          return fileSpec.clazz.newInstance(file)
        }
      }
    }

    return file
  }

  static def artefactForFileWithExtension(file, extension) {
    if(extension == null || file == null) {
      return file;
    }

    def possibleFileSpec = AssetHelper.artefactForExtension(extension)
    if(possibleFileSpec) {
      return possibleFileSpec.clazz.newInstance(file)
    }
    return file
  }

  static def artefactForExtension(extension) {
    def grailsApplication = grails.util.Holders.getGrailsApplication()
    return grailsApplication.assetFileClasses.find{ it.getPropertyValue('extensions').contains(extension) }
  }

  static def fileForFullName(uri) {
    def assetPaths = AssetHelper.getAssetPaths();
    for(assetPath in assetPaths) {
      def path = [assetPath, uri].join(File.separator)
      def fileDescriptor = new File(path)
      // println "Checking ${path}"
      if(fileDescriptor.exists()) {
        return fileDescriptor
      }
    }
    return null;
  }

  static def getAssetPaths() {
    def assetPaths = AssetHelper.scopedDirectoryPaths(new File("grails-app/assets").getAbsolutePath())

    for(plugin in GrailsPluginUtils.pluginInfos) {
      def assetPath = [plugin.pluginDir.getPath(),"grails-app/assets"].join(File.separator)
      assetPaths += AssetHelper.scopedDirectoryPaths(assetPath)
    }
    return assetPaths.unique()
  }

  static def scopedDirectoryPaths(assetPath) {
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

  static def extensionFromURI(uri) {

    def extension = null
    if(uri.lastIndexOf(".") >= 0) {
      extension = uri.substring(uri.lastIndexOf(".") + 1)
    }
    return extension;
  }

  static def nameWithoutExtension(uri) {
    if(uri.lastIndexOf(".") >= 0) {
      return uri.substring(0,uri.lastIndexOf("."))
    }
    return uri
  }

  static def assetMimeTypeForURI(uri) {
    def extension = AssetHelper.extensionFromURI(uri);
    def fileSpec = artefactForExtension(extension);
    if(fileSpec) {
      return fileSpec.getPropertyValue('contentType')
    }
    return null
  }
}
