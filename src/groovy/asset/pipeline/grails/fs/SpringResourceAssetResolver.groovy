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

package asset.pipeline.grails.fs

import asset.pipeline.*
import asset.pipeline.fs.*
import org.springframework.core.io.support.*
import org.springframework.core.io.*

class SpringResourceAssetResolver extends AbstractAssetResolver<Resource> {

    String prefixPath
    ResourceLoader resourceLoader
    PathMatchingResourcePatternResolver resourceResolver

    SpringResourceAssetResolver(String name, ResourceLoader resourceLoader, String basePath) {
        super(name)
        this.prefixPath = basePath
        this.resourceLoader = resourceLoader
        this.resourceResolver = new PathMatchingResourcePatternResolver(resourceLoader)
    }

    AssetFile getAsset(String relativePath, String contentType = null, String extension = null, AssetFile baseFile=null) {
        if(!relativePath) {
            return null
        }
        def normalizedPath = AssetHelper.normalizePath(relativePath)
        def specs

        if(contentType) {
            specs = AssetHelper.getPossibleFileSpecs(contentType)
        } else {
            specs = AssetHelper.assetFileClasses()
        }

        if(!specs) return null

        AssetFile assetFile = resolveAsset(specs, prefixPath, normalizedPath, baseFile, extension)

        return assetFile
    }

    String relativePathToResolver(Resource file, String scanDirectoryPath) {
        if(!file.exists()) {
            return null
        }        
        def filePath = file.URL.path
        if(filePath.contains(scanDirectoryPath)) {
            def i = filePath.indexOf(scanDirectoryPath)
            return filePath.substring(i + scanDirectoryPath.size() + 1)
        }
        else {
            throw new RuntimeException("File was not sourced from the same ScanDirectory ${filePath}")
        }
 
        
    }

    Resource getRelativeFile(String relativePath, String name) {
        if(name.startsWith('/')) {
            name = name.substring(1)
        }
        resourceLoader.getResource("classpath:$relativePath/$name")
    }

    Closure<InputStream> createInputStreamClosure(Resource file) {
        if(!file.exists()) {
            return null
        }    
        {-> file.inputStream }
    }

    String getFileName(Resource file) {
        file.filename
    }

    List<AssetFile> getAssets(String basePath, String contentType = null, String extension = null,  Boolean recursive = true, AssetFile relativeFile=null, AssetFile baseFile = null) {
        def specs
        if(contentType) {
            specs = AssetHelper.getPossibleFileSpecs(contentType)
        }

        if(!specs) return []


        def extensions = []
        if(extension) {
            extensions << extension
        }
        else {
            for(spec in specs) {
                extensions.addAll(spec.extensions)
            }            
        }

        def resources = []
        def normalizedPath = AssetHelper.normalizePath(basePath)
        try {
            // println "Looking for files in classpath:$prefixPath/$normalizedPath/**"
            def scanPath = "classpath*:$prefixPath/"
            if(normalizedPath) {
                scanPath += "$normalizedPath/"
            }
            resources = resourceResolver.getResources(scanPath + "**").findAll { res ->
                def filePath = res.URL.path
                !filePath.endsWith('/') &&extensions.any { ext -> res.filename.endsWith(".$ext") }
            }
        } catch(e) {
            //ITS OK IF ITS NOT FOUND
        }

        resources = resources.collect {
            assetForFile(it, contentType, baseFile, prefixPath)
        }.findAll{it != null}
        return resources
    }

    Collection<AssetFile> scanForFiles(List<String> excludePatterns, List<String> includePatterns) {
        def excludedPatternRegex =  excludePatterns ?: []
        def includedPatternRegex =  includePatterns ?: []
        def resources = []
        try {
            resources = resourceResolver.getResources("classpath*:$prefixPath/**").findAll { res ->
                def relativePath = relativePathToResolver(res, prefixPath)  
                def filename = res.filename
                def path = res.URL.path
                return !path.endsWith('/') && (!isFileMatchingPatterns(relativePath,excludedPatternRegex) || isFileMatchingPatterns(relativePath,includedPatternRegex)) && filename.contains('.') && !filename.startsWith('.')
            }
        } catch(e) {
            //ITS OK IF ITS NOT FOUND
        }


        resources.collect { res ->
            def relativePath = relativePathToResolver(res, prefixPath)
            def assetFileClass = AssetHelper.assetForFileName(relativePath)
            if(assetFileClass) {
                assetFileClass.newInstance(inputStreamSource: createInputStreamClosure(res), path: relativePath, sourceResolver: this)
            } else {
                new GenericAssetFile(inputStreamSource: createInputStreamClosure(res), path: relativePath)
            }
        }.findAll { it != null }

    }
}