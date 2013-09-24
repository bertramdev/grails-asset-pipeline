package asset.pipeline

import org.codehaus.groovy.grails.web.context.ServletContextHolder

class DirectiveProcessor {

  static DIRECTIVES = [require_self: "requireSelfDirective" ,require_tree: "requireTreeDirective", require: "requireFileDirective"]

  def contentType
  def precompiler
  def files = []
  def baseFile
  def servletContext = ServletContextHolder.getServletContext()

  DirectiveProcessor(contentType, precompiler=false) {
    this.contentType = contentType
    this.precompiler = precompiler
  }

  def compile(file) {
    if(file.class.name == 'java.io.File') {
      return file.getBytes()
    }
    this.baseFile = file
    this.files = []
    def tree = getDependencyTree(file)
    def buffer = ""

    buffer = loadContentsForTree(tree,buffer)
    return buffer
  }

  def getFlattenedRequireList(file) {
    if(file.class.name == 'java.io.File') {
      return relativePath(file)
    }
    def flattenedList = []
    def tree = getDependencyTree(file)

    flattenedList = loadRequiresForTree(tree, flattenedList)
    return flattenedList
  }

  def loadRequiresForTree(treeSet, flattenedList) {
    def selfLoaded = false
    for(childTree in treeSet.tree) {
      if(childTree == "self") {
        def extension = treeSet.file.compiledExtension
        def fileName = AssetHelper.fileNameWithoutExtensionFromArtefact(relativePath(treeSet.file.file, true),treeSet.file)
        flattenedList << "${fileName}.${extension}"
        selfLoaded = true
      } else {
        flattenedList = loadRequiresForTree(childTree, flattenedList)
      }
    }

    if(!selfLoaded) {
      def extension = treeSet.file.compiledExtension
      def fileName = AssetHelper.fileNameWithoutExtensionFromArtefact(relativePath(treeSet.file.file, true),treeSet.file)
      flattenedList << "${fileName}.${extension}"
    }
    return flattenedList
  }

  def loadContentsForTree(treeSet,buffer) {
    def selfLoaded = false
    for(childTree in treeSet.tree) {
      if(childTree == "self") {
        buffer += fileContents(treeSet.file) + "\n"
        selfLoaded = true
      } else {
        buffer = loadContentsForTree(childTree,buffer)
      }
    }

    if(!selfLoaded) {
      buffer += fileContents(treeSet.file) + "\n"
    }
    return buffer
  }

	def getDependencyTree(file) {
    this.files << file
		def tree = [file:file,tree:[]]
    if(file.class.name != 'java.io.File') {
      this.findDirectives(file,tree)
    }

    return tree
	}

  def fileContents(file) {

    if(file.class.name == 'java.io.File') {

      return file.text
    }
    return file.processedStream(this.precompiler)
  }

	def findDirectives(fileSpec, tree) {
      def lines = fileSpec.file.readLines()
      // def directiveFound = false
			lines.find { line ->
        // if(!line && directiveFound == true) {
        //   return true
        // }
        def directive = fileSpec.directiveForLine(line)
				if(directive) {
					directive = directive.trim()
          def unprocessedArgs = directive.split(" ")

          def processor = DIRECTIVES[unprocessedArgs[0].toLowerCase()]

          if(processor) {
            def directiveArguments = new groovy.text.GStringTemplateEngine().createTemplate(directive).make().toString().split(" ")
            directiveArguments[0] = directiveArguments[0].toLowerCase()
            this."${processor}"(directiveArguments, fileSpec,tree)
          }
				}
        return false
			}
	}

  def requireSelfDirective(command, file, tree) {
    tree.tree << "self"
  }

  def requireTreeDirective(command, fileSpec, tree) {
    def parentFile
    if(!command[1] || command[1] == '.') {
      parentFile = new File(fileSpec.file.getParent())
    } else {
      parentFile = new File([fileSpec.file.getParent(),command[1]].join(File.separator))
    }

    recursiveTreeAppend(parentFile,tree)
  }

  def recursiveTreeAppend(directory,tree) {
    def files = directory.listFiles()
    files = files?.sort { a, b -> a.name.compareTo b.name }
    for(file in files) {
      if(file.isDirectory()) {
        recursiveTreeAppend(file,tree)
      }
      else if(AssetHelper.assetMimeTypeForURI(file.getAbsolutePath()) == contentType) {
        if(!isFileInTree(file,tree)) {
          tree.tree << getDependencyTree(AssetHelper.artefactForFile(file,contentType, this.baseFile))
        }
      }
    }
  }

  def isFileInTree(file,currentTree) {
    def realFile = file
    if(file.class.name != 'java.io.File') {
      realFile = file.file
    }
    def result = files.find { it ->
      return (it.class.name == 'java.io.File' && it.getAbsolutePath() == realFile.getAbsolutePath()) || it.file.getAbsolutePath() == realFile.getAbsolutePath()
    }
    if(result) {
      return true
    } else {
      return false
    }
  }

  def requireFileDirective(command, file, tree) {
    def fileName = command[1]
    def newFile
    if(fileName.startsWith(AssetHelper.DIRECTIVE_FILE_SEPARATOR)) {
      newFile = AssetHelper.fileForUri(fileName, this.contentType, null, this.baseFile)
    } else {
      def relativeFileName = [relativePath(file.file),fileName].join(AssetHelper.DIRECTIVE_FILE_SEPARATOR)
      // println "Including Relative File: ${relativeFileName} - ${fileName}"
      newFile = AssetHelper.fileForUri(relativeFileName, this.contentType, null, this.baseFile)
    }

    if(newFile) {
      if(!isFileInTree(newFile,tree)) {
        // println("Inserting File")
        tree.tree << getDependencyTree(newFile)
      }

    } else if(!fileName.startsWith(AssetHelper.DIRECTIVE_FILE_SEPARATOR)) {
      command[1] = AssetHelper.DIRECTIVE_FILE_SEPARATOR + command[1]
      requireFileDirective(command,file,tree)
    }
  }

  def relativePath(file, includeFileName=false) {
    def path
    if(includeFileName) {
      path = file.class.name == 'java.io.File' ? file.getCanonicalPath().split(AssetHelper.QUOTED_FILE_SEPARATOR) : file.file.getCanonicalPath().split(AssetHelper.QUOTED_FILE_SEPARATOR)
    } else {
      path = file.getParent().split(AssetHelper.QUOTED_FILE_SEPARATOR)
    }

    def startPosition = path.findLastIndexOf{ it == "grails-app" }
    if(startPosition == -1) {
      startPosition = path.findLastIndexOf{ it == 'web-app' }
      if(startPosition+2 >= path.length) {
        return ""
      }
      path = path[(startPosition+2)..-1]
    }
    else {
      if(startPosition+3 >= path.length) {
        return ""
      }
      path = path[(startPosition+3)..-1]
    }

    return path.join(AssetHelper.DIRECTIVE_FILE_SEPARATOR)
  }
}
