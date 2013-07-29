package asset.pipeline

import org.codehaus.groovy.grails.web.context.ServletContextHolder

class DirectiveProcessor {

  static DIRECTIVES = [require_self: "requireSelfDirective" ,require_tree: "requireTreeDirective", require: "requireFileDirective"]

  def contentType
  def files = []
  def servletContext = ServletContextHolder.getServletContext()

  DirectiveProcessor(contentType) {
    this.contentType = contentType
  }

  def compile(file) {
    if(file.class.name == 'java.io.File') {
      return file.getBytes()
    }
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
        flattenedList << relativePath(treeSet.file.file, true)
        selfLoaded = true
      } else {
        flattenedList = loadRequiresForTree(childTree, flattenedList)
      }
    }

    if(!selfLoaded) {
      flattenedList << relativePath(treeSet.file.file, true)
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
    return file.processedStream()
  }

	def findDirectives(fileSpec, tree) {
		// try {
			fileSpec.file.eachLine { line ->
        if(!line) {
          return false
          throw "End of Directive Set"
        }
        def directive = fileSpec.directiveForLine(line)
				if(directive) {
					directive = directive.trim()
          def directiveArguments = directive.split(" ")
          directiveArguments[0] = directiveArguments[0].toLowerCase()
          def processor = DIRECTIVES[directiveArguments[0]]

          if(processor) {
            this."${processor}"(directiveArguments, fileSpec,tree)
          }
				}
        return true
			}
		// } catch(except) {
  //     //TODO: Narrow exception scope here please!
  //     log.info "Done Processing Directive for File"
		// }
	}

  def requireSelfDirective(command, file, tree) {
    tree.tree << "self"
  }

  def requireTreeDirective(command, fileSpec, tree) {
    def parentFile
    if(!command[1] || command[1] == '.') {
      parentFile = new File(fileSpec.file.getParent())
      // println "Requiring Tree for File: ${parentFile}"
    } else {
      parentFile = new File([fileSpec.file.getParent(),command[1]].join(File.separator))
    }

    recursiveTreeAppend(parentFile,tree)
  }

  def recursiveTreeAppend(directory,tree) {
    def files = directory.listFiles()
    files = files.sort { a, b -> a.name.compareTo b.name }
    for(file in files) {
      // println("Finding FIle with Type: ${AssetHelper.assetMimeTypeForURI(file.getAbsolutePath())} against ${contentType}")
      if(file.isDirectory()) {
        recursiveTreeAppend(file,tree)
      }
      else if(AssetHelper.assetMimeTypeForURI(file.getAbsolutePath()) == contentType) {
        if(!isFileInTree(file,tree)) {
          // println "Appending to Tree, ${file}"
          tree.tree << getDependencyTree(AssetHelper.artefactForFile(file,contentType))
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
    if(fileName.startsWith(File.separator)) {
      newFile = AssetHelper.fileForUri(fileName, this.contentType)
    } else {
      def relativeFileName = [relativePath(file.file),fileName].join(File.separator)
      // println "Including Relative File: ${relativeFileName} - ${fileName}"
      newFile = AssetHelper.fileForUri(relativeFileName, this.contentType)
    }

    if(newFile) {
      if(!isFileInTree(newFile,tree)) {
        // println("Inserting File")
        tree.tree << getDependencyTree(newFile)
      }

    } else if(!fileName.startsWith(File.separator)) {
      command[1] = File.separator + command[1]
      requireFileDirective(command,file,tree)
    }
  }

  def relativePath(file, includeFileName=false) {
    def path
    if(includeFileName) {
      path = file.class.name == 'java.io.File' ? file.absolutePath.split(File.separator) : file.file.absolutePath.split(File.separator)
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
