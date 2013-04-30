package asset.pipeline
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

class AssetProcessorService {
  static transactional = false
  def serveAsset(uri, contentType=null, extension=null) {
    def assetFile = AssetHelper.fileForUri(uri, contentType, extension)

    def directiveProcessor = new DirectiveProcessor(contentType)
    if(assetFile) {
      return directiveProcessor.compile(assetFile);
    }

    return null
  }


}
