package asset.pipeline.processors



class CssProcessor {


	CssProcessor() {

  }

  def process(inputText) {
    def assetPaths = inputText.findAll(/url\([\'\"]?\(.*)[\'\"]?\)/) { fullMatch, assetPath -> return assetPath}
    println "Found Assets: ${assetPaths}"
    return inputText
  }


}
