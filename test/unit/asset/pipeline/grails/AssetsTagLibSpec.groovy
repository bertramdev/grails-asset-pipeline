/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package asset.pipeline.grails

import grails.test.mixin.TestFor
import spock.lang.Specification
import asset.pipeline.*

/**
 * @author David Estes
 */
@TestFor(AssetsTagLib)
class AssetsTagLibSpec extends Specification {
  AssetProcessorService assetProcessorServiceMock = Mock(AssetProcessorService)
  private static final LINE_BREAK = System.getProperty('line.separator') ?: '\n'
  private static MOCK_BASE_SERVER_URL = 'http://localhost:8080/foo'

  def setup() {
    AssetPipelineConfigHolder.registerResolver(new asset.pipeline.fs.FileSystemAssetResolver('application','grails-app/assets'))
    assetProcessorServiceMock.getAssetMapping() >> { "assets" }
    def assetMethodTagLibMock = mockTagLib(AssetMethodTagLib)
    assetMethodTagLibMock.assetProcessorService = assetProcessorServiceMock

    assetMethodTagLibMock.grailsLinkGenerator = [serverBaseURL: MOCK_BASE_SERVER_URL]
  }

  void "should return assetPath"() {
    given:
      def assetSrc = "asset-pipeline/test/test.css"
    expect:
      tagLib.assetPath(src: assetSrc) == '/assets/asset-pipeline/test/test.css'
  }

  void "should return javascript link tag when debugMode is off"() {
    given:
      grailsApplication.config.grails.assets.bundle = true
      def assetSrc = "asset-pipeline/test/test.js"
    expect:
      tagLib.javascript(src: assetSrc) == '<script src="/assets/asset-pipeline/test/test.js" type="text/javascript" ></script>'
  }

  void "should return gzipped javascript link tag when header accept it"() {
    given:
      grailsApplication.config.grails.assets.bundle = true
      request.addHeader "Accept-Encoding", "gzip"
      def assetSrc = "asset-pipeline/test/test.js"
    expect:
      tagLib.javascript(src: assetSrc) == '<script src="/assets/asset-pipeline/test/test.js.gz" type="text/javascript" ></script>'
  }

  void "should return un-gzipped javascript link tag when header does not accept it"() {
    given:
      grailsApplication.config.grails.assets.bundle = true
      request.addHeader "Accept-Encoding", ""
      def assetSrc = "asset-pipeline/test/test.js"
    expect:
      tagLib.javascript(src: assetSrc) == '<script src="/assets/asset-pipeline/test/test.js" type="text/javascript" ></script>'
  }

  void "should return un-gzipped javascript link tag when gzip is excluded"() {
    given:
      grailsApplication.config.grails.assets.bundle = true
      grailsApplication.config.grails.assets.excludesGzip = true
      request.addHeader "Accept-Encoding", "gzip"
      def assetSrc = "asset-pipeline/test/test.js"
    expect:
      tagLib.javascript(src: assetSrc) == '<script src="/assets/asset-pipeline/test/test.js" type="text/javascript" ></script>'
  }

  void "should return javascript link tag with seperated files when debugMode is on"() {
    given:
      grailsApplication.config.grails.assets.bundle = false
      grailsApplication.config.grails.assets.allowDebugParam = true
      params."_debugAssets" = "y"
      def stringWriter = new StringWriter()

      def assetSrc = "asset-pipeline/test/test.js"
      def output
    when:
      // tagLib.out = stringWriter
      output = tagLib.javascript(src: assetSrc)

    then:
      output == '<script src="/assets/asset-pipeline/test/test.js?compile=false" type="text/javascript" ></script>' + LINE_BREAK + '<script src="/assets/asset-pipeline/test/libs/file_a.js?compile=false" type="text/javascript" ></script>' + LINE_BREAK + '<script src="/assets/asset-pipeline/test/libs/file_c.js?compile=false" type="text/javascript" ></script>' + LINE_BREAK + '<script src="/assets/asset-pipeline/test/libs/file_b.js?compile=false" type="text/javascript" ></script>' + LINE_BREAK + '<script src="/assets/asset-pipeline/test/libs/subset/subset_a.js?compile=false" type="text/javascript" ></script>' + LINE_BREAK
  }

  void "should return stylesheet link tag when debugMode is off"() {
    given:
      grailsApplication.config.grails.assets.bundle = true
      def assetSrc = "asset-pipeline/test/test.css"
    expect:
      tagLib.stylesheet(href: assetSrc) == '<link rel="stylesheet" href="/assets/asset-pipeline/test/test.css"/>'
  }

  void "should return gzipped stylesheet link tag when header accept it"() {
    given:
      grailsApplication.config.grails.assets.bundle = true
      grailsApplication.config.grails.assets.excludesGzip = false
      request.addHeader "Accept-Encoding", "gzip"
      def assetSrc = "asset-pipeline/test/test.css"
    expect:
      tagLib.stylesheet(href: assetSrc) == '<link rel="stylesheet" href="/assets/asset-pipeline/test/test.css.gz"/>'
  }

  void "should return un-gzipped stylesheet link tag when header does not accept it"() {
    given:
      grailsApplication.config.grails.assets.bundle = true
      grailsApplication.config.grails.assets.excludesGzip = false
      request.addHeader "Accept-Encoding", ""
      def assetSrc = "asset-pipeline/test/test.css"
    expect:
      tagLib.stylesheet(href: assetSrc) == '<link rel="stylesheet" href="/assets/asset-pipeline/test/test.css"/>'
  }

  void "should return un-gzipped stylesheet link tag when gzip is excluded"() {
    given:
      grailsApplication.config.grails.assets.bundle = true
      grailsApplication.config.grails.assets.excludesGzip = true
      request.addHeader "Accept-Encoding", "gzip"
      def assetSrc = "asset-pipeline/test/test.css"
    expect:
      tagLib.stylesheet(href: assetSrc) == '<link rel="stylesheet" href="/assets/asset-pipeline/test/test.css"/>'
  }

  void "should return stylesheet link tag with seperated files when debugMode is on"() {
    given:
      grailsApplication.config.grails.assets.bundle = false
      grailsApplication.config.grails.assets.allowDebugParam = true
      params."_debugAssets" = "y"
      def stringWriter = new StringWriter()

      def assetSrc = "asset-pipeline/test/test.css"
      def output
    when:
      // tagLib.out = stringWriter
      output = tagLib.stylesheet(src: assetSrc)

    then:
      output == '<link rel="stylesheet" href="/assets/asset-pipeline/test/test.css?compile=false"  />' + LINE_BREAK + '<link rel="stylesheet" href="/assets/asset-pipeline/test/test2.css?compile=false"  />' + LINE_BREAK

  }

  void "should return image tag"() {
    given:
      def assetSrc = "grails_logo.png"
    expect:
      tagLib.image(src: assetSrc, width:'200',height:200) == '<img src="/assets/grails_logo.png" width="200" height="200"/>'
  }

    void "should return image tag with absolute path"() {
      given:
        def assetSrc = "grails_logo.png"

      expect:
        tagLib.image(src: assetSrc, absolute: true) == "<img src=\"$MOCK_BASE_SERVER_URL/assets/grails_logo.png\" />"
    }

  void "should return link tag"() {
    given:
      def assetSrc = "grails_logo.png"
    expect:
      tagLib.link(href: assetSrc, rel:'test') == '<link rel="test" href="/assets/grails_logo.png"/>'
  }

  void "test if asset path exists in dev mode"() {
    given:
      def fileUri = "asset-pipeline/test/test.css"
      grailsApplication.config.grails.assets.precompiled = false
    expect:
      tagLib.assetPathExists([src: fileUri])
  }

  void "test if asset path is missing in dev mode"() {
    given:
      def fileUri = "asset-pipeline/test/missing.css"
      grailsApplication.config.grails.assets.precompiled = false
    expect:
      !tagLib.assetPathExists([src: fileUri])
  }

  void "test if asset path exists in dev mode and closure renders the body"() {
    given:
      def fileUri = "asset-pipeline/test/test.css"
      grailsApplication.config.grails.assets.precompiled = false
    expect:
      applyTemplate( "<asset:assetPathExists src=\"$fileUri\">text to render</asset:assetPathExists>" ) == 'text to render'
  }

  void "test if asset path is missing in dev mode and closure doesn't render the body"() {
    given:
      def fileUri = "asset-pipeline/test/missing.css"
      grailsApplication.config.grails.assets.precompiled = false
    expect:
      applyTemplate( "<asset:assetPathExists src=\"$fileUri\">text to render</asset:assetPathExists>" ) == ''
  }

  void "test if asset path exists in prod mode"() {
    given:
      def fileUri = "asset-pipeline/test/test.css"
      Properties manifestProperties = new Properties()
      manifestProperties.setProperty(fileUri,fileUri)

      grailsApplication.config.grails.assets.precompiled = true
      grailsApplication.config.grails.assets.manifest = manifestProperties
    expect:
      tagLib.assetPathExists([src: fileUri])
  }

  void "asset path should not exist in dev mode"() {
    given:
      def fileUri = "asset-pipeline/test/notfound.css"
      grailsApplication.config.grails.assets.precompiled = false
    expect:
      !tagLib.assetPathExists([src: fileUri])
  }

  void "should render deferred scripts"() {
    given:
      def script1 = "console.log('hello world 1');"
      def script2 = "console.log('hello world 2');"

    when:
      applyTemplate("<asset:script type='text/javascript'>$script1</asset:script>")
      applyTemplate("<asset:script type='text/javascript'>$script2</asset:script>")

    then:
      applyTemplate("<asset:deferredScripts/>") == "<script type=\"text/javascript\">${script1}</script><script type=\"text/javascript\">${script2}</script>"
  }

  void "should render deferred scripts and evaluate nested groovy expressions"() {
    when:
      applyTemplate('<asset:script type="text/javascript"><g:if test="${isTrue}">alert("foo");</g:if></asset:script>', [isTrue: true])

    then:
      applyTemplate("<asset:deferredScripts/>") == '<script type="text/javascript">alert("foo");</script>'
  }
}
