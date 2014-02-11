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
 * @author Tommy Barker
 */
// @TestFor(CacheManager)
class CacheManagerSpec extends Specification {

    void "should createCache by filename and md5"() {
        given:
            def testFile = new File('grails-app/assets/stylesheets/asset-pipeline/test/test.css')
            def testFileName = testFile.name
            def testMd5 = AssetHelper.getByteDigest(testFile.bytes)
            def cacheContent
        when:
            CacheManager.createCache(testFileName, testMd5, testFile.text)
            cacheContent = CacheManager.findCache(testFileName, testMd5)
        then: 
            cacheContent == testFile.text
    }

    void "should findcache by filename and md5"() {
        given:
            def testFile = new File('grails-app/assets/stylesheets/asset-pipeline/test/test.css')
            def testFileName = testFile.name
            def testMd5 = AssetHelper.getByteDigest(testFile.bytes)
            def cacheContent
            CacheManager.createCache(testFileName, testMd5, testFile.text)

        when: "retreiving from cache"
            cacheContent = CacheManager.findCache(testFileName, testMd5)
        then:
            cacheContent == testFile.text

        when: "retreiving with different md5"
            cacheContent = CacheManager.findCache(testFileName, "xxx")
        then:
            cacheContent == null
    }

    void "should handle cache dependencies"() {
        given:
            def testFile = new File('grails-app/assets/stylesheets/asset-pipeline/test/test.css')
            def testFileName = testFile.name
            def testMd5 = AssetHelper.getByteDigest(testFile.bytes)

            def dependentFile = new File('grails-app/assets/stylesheets/asset-pipeline/test/_dependent.css')
            dependentFile.text = "/*Cache Manager Dependency Test*/"
            def cacheContent
            CacheManager.createCache(testFileName, testMd5, testFile.text)
        when: "adding dependency should return file if unchanged"
            dependentFile.text = "/*Cache Manager Dependency Test*/"
            CacheManager.addCacheDependency(testFileName, dependentFile)
            cacheContent = CacheManager.findCache(testFileName, testMd5)
        then:
            cacheContent == testFile.text

        when: "expiring cache dependency"
            CacheManager.addCacheDependency(testFileName, dependentFile)
            dependentFile.text = "/*Cache Manager Dependency Test Cache Expire*/"
            cacheContent = CacheManager.findCache(testFileName, testMd5)
        then:
            cacheContent == null
        when: "removing dependency should expire cache"
            CacheManager.addCacheDependency(testFileName, dependentFile)
            dependentFile.delete()
            cacheContent = CacheManager.findCache(testFileName, testMd5)
        then:
            cacheContent == null
    }
}
