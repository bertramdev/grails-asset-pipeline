package asset.pipeline.processors
import asset.pipeline.*
import org.codehaus.groovy.grails.commons.ApplicationHolder

class CssProcessor {
	def grailsApplication
	CssProcessor() {
		grailsApplication = ApplicationHolder.getApplication()
  }

  def process(inputText, assetFile) {
  	def cachedPaths = [:]
    return inputText.replaceAll(/url\([\'\"]?([a-zA-Z0-9\-\_\.\/\@]+)[\'\"]?\)/) { fullMatch, assetPath ->
    	def replacementPath = assetPath
    	if(cachedPaths[assetPath]) {
    		replacementPath = cachedPaths[assetPath]
    	} else {
	    		if(isRelativePath(assetPath)) {

					def relativeFileName = [relativePath(assetFile.file),assetPath].join(File.separator)
					// println "Found Relative Path ${assetPath} - Relative: ${relativeFileName}"
		      def cssFile = AssetHelper.fileForFullName(relativeFileName)
		      if(cssFile) {
		      	def prefixPath = grailsApplication.config.grails.assets.url ?: "/assets/"
		      	replacementPath = prefixPath + relativePath(cssFile,true)
		      	cachedPaths[assetPath] = replacementPath
		      }
	    	}
    	}
    	return "url('${replacementPath}')"
    }

  }

  private isRelativePath(assetPath) {
  	return !assetPath.startsWith("/") && !assetPath.startsWith("http")
  }

  private relativePath(file, includeFileName=false) {
    def path
    if(includeFileName) {
      path = file.class.name == 'java.io.File' ? file.getCanonicalPath().split(File.separator) : file.file.getCanonicalPath().split(File.separator)
    } else {
      path = file.getParent().split(File.separator)
    }

    def startPosition = path.findIndexOf{ it == "grails-app" }
    if(startPosition+3 >= path.length) {
      return ""
    }

    path = path[(startPosition+3)..-1]
    return path.join(file.separator)
  }

}
