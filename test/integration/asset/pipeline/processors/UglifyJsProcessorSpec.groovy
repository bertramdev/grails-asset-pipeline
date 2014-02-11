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

import grails.plugin.spock.IntegrationSpec

class UglifyJsProcessorSpec extends IntegrationSpec {
    def "minifies js contents"() {
        given: "some javascript and an UglifyJsProcessor"
            def js = '''
                console.log("Test Minification");
                function add(first,second) {
                    return first + second;
                }
            '''
            def uglifyJsProcessor = new UglifyJsProcessor()

        when:
            def minifiedJs = uglifyJsProcessor.process(js,[strictSemicolons: false])
        then:
            minifiedJs.size() < js.size()
    }
}