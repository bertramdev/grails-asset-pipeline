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

import grails.plugin.spock.IntegrationSpec

class DirectiveProcessorSpec extends IntegrationSpec {
    def "gets entire tree when required"() {
        given: "A uri and file extension with require directives"
            def uri                = "asset-pipeline/test/test.js"
            def fileExtension      = "js"
            def contentType        = "application/javascript"
            def file               = AssetHelper.fileForUri(uri, contentType, fileExtension)
            def directiveProcessor = new DirectiveProcessor(contentType)
        when:
            def fileContent = directiveProcessor.compile(file).toString()
        then:
            fileContent.contains("This is File B") && fileContent.contains("This is File A") && fileContent.contains("This is File C") && fileContent.contains("console.log(\"Subset A\");");
    }

    def "gets dependency list flattened with all files"() {
        given: "A uri and file extension with require directives"
            def uri                = "asset-pipeline/test/test.js"
            def fileExtension      = "js"
            def contentType        = "application/javascript"
            def file               = AssetHelper.fileForUri(uri, contentType, fileExtension)
            def directiveProcessor = new DirectiveProcessor(contentType)
        when:
            def dependencyList = directiveProcessor.getFlattenedRequireList(file)
            println dependencyList
        then:
            dependencyList.size() == 5
    }

    def "evaluates GString directives if detected"() {
        given: "A uri and file extension with require directives"
            def uri                = "asset-pipeline/test/gstringtest.js"
            def fileExtension      = "js"
            def contentType        = "application/javascript"
            def file               = AssetHelper.fileForUri(uri, contentType, fileExtension)
            def directiveProcessor = new DirectiveProcessor(contentType)
        when:
            def dependencyList = directiveProcessor.getFlattenedRequireList(file)
            println dependencyList
        then:
            dependencyList.size() == 2
    }

    def "gets dependency list order correct"() {
        given: "A uri and file extension with require directives"
            def uri                = "asset-pipeline/test/test.js"
            def fileExtension      = "js"
            def contentType        = "application/javascript"
            def file               = AssetHelper.fileForUri(uri, contentType, fileExtension)
            def directiveProcessor = new DirectiveProcessor(contentType)
        when:
            def dependencyList = directiveProcessor.getFlattenedRequireList(file)
        then:
            dependencyList.findIndexOf{ it.path == "asset-pipeline/test/libs/file_c.js" } < dependencyList.findIndexOf{ it.path == "asset-pipeline/test/libs/file_b.js"}
    }
}
