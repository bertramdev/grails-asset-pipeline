package asset.pipeline

class AssetProcessorService {

  static transactional = false

  def serveAsset(uri, contentType=null, extension=null) {
    def assetFile = AssetHelper.fileForUri(uri, contentType, extension)

    def directiveProcessor = new DirectiveProcessor(contentType)
    if(assetFile) {
      return directiveProcessor.compile(assetFile)
    }

    return null
  }

  def getDependencyList(uri,contentType = null, extension=null) {
  	def assetFile = AssetHelper.fileForUri(uri, contentType, extension)
    def directiveProcessor = new DirectiveProcessor(contentType)
    if(assetFile) {
      return directiveProcessor.getFlattenedRequireList(assetFile)
    }
    return null
  }

  def serveUncompiledAsset(uri, contentType, extension = null) {
  	def assetFile = AssetHelper.fileForUri(uri, contentType, extension)

    def directiveProcessor = new DirectiveProcessor(contentType)
    if(assetFile) {
      return directiveProcessor.fileContents(assetFile)
    }

    return null
  }
}
