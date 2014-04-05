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

package asset.pipeline.processors

import grails.test.spock.IntegrationSpec
import asset.pipeline.*
import grails.util.Holders

class CssProcessorSpec extends IntegrationSpec {
    def "replaces image urls with relative paths"() {
        given: "some css and a CssProcessor"
            
            def cssProcessor = new CssProcessor(false)
            def file = new File("grails-app/assets/stylesheets/asset-pipeline/test/test.css")
            def assetFile    = new CssAssetFile(file: file)
        when:
            def processedCss = cssProcessor.process(assetFile.file.text, assetFile)
        then:
            processedCss.contains("url('../../grails_logo.png')")
    }

    def "replaces image urls with relative paths and cache digest names in precompiler mode"() {
        given: "some css and a CssProcessor"
            
            def cssProcessor = new CssProcessor(true)
            def file = new File("grails-app/assets/stylesheets/asset-pipeline/test/test.css")
            def assetFile    = new CssAssetFile(file: file)
            Holders.metaClass.static.getConfig = { ->
                [grails: [assets: [[minifyJs: true]]]]
            }
        when:
            def processedCss = cssProcessor.process(assetFile.file.text, assetFile)
        then:
            processedCss.contains("url('../../grails_logo-544aa48b9c2f9b532fe57ed0451c9e6e.png')")
    }
}