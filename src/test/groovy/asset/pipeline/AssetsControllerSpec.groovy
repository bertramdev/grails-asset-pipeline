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

package asset.pipeline

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * @author David Estes
 */
@TestFor(AssetsController)

class AssetsControllerSpec extends Specification {
  AssetProcessorService assetProcessorServiceMock = Mock(AssetProcessorService)

  def setup() {
    AssetPipelineConfigHolder.registerResolver(new asset.pipeline.fs.FileSystemAssetResolver('application','grails-app/assets'))      
    controller.assetProcessorService = assetProcessorServiceMock
  }
  void "index() should return 404 when no file found"() {
    given:
      controller.index()
    expect:
      controller.response.status == 404
  }

  void "index() should return 404 for non-existent top level file starting with a dot"() {
    given:
      request.forwardURI = "/assets/.bash_history"
      params.id = ".bash_history"
    when:
      controller.index()
    then:
      controller.response.status == 404
  }

  void "index() should return file contents when file found"() {
    given:
      request.forwardURI = "/assets/asset-pipeline/test/test.css"
      params.id = "asset-pipeline/test/test.css"
    when:
      controller.index()
    then:
      1 * assetProcessorServiceMock.serveAsset('asset-pipeline/test/test','text/css','css',null) >> { return "File Contents "}
      controller.response.status == 200
      controller.response.contentType == "text/css"
  }

  void "index() should serve uncompiled asset when compile is false"() {
    given:
      request.forwardURI = "/assets/asset-pipeline/test/test.css"
      params.id = "asset-pipeline/test/test.css"
      params.compile = 'false'
    when:
      controller.index()
    then:
      1 * assetProcessorServiceMock.serveUncompiledAsset('asset-pipeline/test/test','text/css','css',null) >> { return "File Contents "}
      controller.response.status == 200
      controller.response.contentType == "text/css"
  }


  void "index() should pass along encoding for uncompiled asset"() {
    given:
      request.forwardURI = "/assets/asset-pipeline/test/test.css"
      params.id = "asset-pipeline/test/test.css"
      params.compile = 'false'
      params.encoding = 'utf-8'
    when:
      controller.index()
    then:
      1 * assetProcessorServiceMock.serveUncompiledAsset('asset-pipeline/test/test','text/css','css','utf-8') >> { return "File Contents "}
      controller.response.status == 200
      controller.response.contentType == "text/css"
  }

}
