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
import grails.util.Environment
import grails.plugin.webxml.FilterManager
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils

import asset.pipeline.grails.LinkGenerator
import asset.pipeline.grails.CachingLinkGenerator
import asset.pipeline.grails.AssetResourceLocator
import asset.pipeline.grails.fs.*
import asset.pipeline.fs.*
import asset.pipeline.*



class AssetPipelineGrailsPlugin {
    def version         = "2.1.5"
    def grailsVersion   = "2.2 > *"
    def title           = "Asset Pipeline Plugin"
    def author          = "David Estes"
    def authorEmail     = "destes@bcap.com"
    def description     = 'The Asset-Pipeline is a plugin used for managing and processing static assets in Grails applications. Asset-Pipeline functions include processing and minification of both CSS and JavaScript files. It is also capable of being extended to compile custom static assets, such as CoffeeScript.'
    def documentation   = "http://bertramdev.github.io/asset-pipeline"
    def license         = "APACHE"
    def organization    = [ name: "Bertram Capital", url: "http://www.bertramcapital.com/" ]
    def issueManagement = [ system: "GITHUB", url: "http://github.com/bertramdev/asset-pipeline/issues" ]
    def scm             = [ url: "http://github.com/bertramdev/asset-pipeline" ]
    def pluginExcludes  = [
        "grails-app/assets/**",
        "test/dummy/**"
    ]
    def developers      = [ [name: 'Brian Wheeler'] ]
    def loadAfter = ['url-mappings']

    def doWithApplicationContext = { ctx ->
        //Register Plugin Paths
        AssetPipelineConfigHolder.registerResolver(new FileSystemAssetResolver('application','grails-app/assets'))
        def pluginManager = ctx.pluginManager
        if(!application.warDeployed) {
            // for(plugin in pluginManager.getAllPlugins()) {
            //     if(plugin instanceof org.codehaus.groovy.grails.plugins.BinaryGrailsPlugin) {
            //         def descriptorURI = plugin.binaryDescriptor.resource.URI
            //         descriptorURI = new java.net.URI( new java.net.URI(descriptorURI.getSchemeSpecificPart()).getSchemeSpecificPart()).toString().split("!")[0]

            //         AssetPipelineConfigHolder.registerResolver(new JarAssetResolver(plugin.name,descriptorURI,'META-INF/assets'))
            //         AssetPipelineConfigHolder.registerResolver(new JarAssetResolver(plugin.name,descriptorURI,'META-INF/static'))
            //     }

            // }
            AssetPipelineConfigHolder.registerResolver(new SpringResourceAssetResolver('classpath',ctx, 'META-INF/assets'))
            AssetPipelineConfigHolder.registerResolver(new SpringResourceAssetResolver('classpath',ctx, 'META-INF/static'))
            AssetPipelineConfigHolder.registerResolver(new SpringResourceAssetResolver('classpath',ctx, 'META-INF/resources'))
            for(plugin in GrailsPluginUtils.pluginInfos) {
                def assetPath = [plugin.pluginDir.getPath(), "grails-app", "assets"].join(File.separator)
                def fallbackPath = [plugin.pluginDir.getPath(), "web-app"].join(File.separator)
                AssetPipelineConfigHolder.registerResolver(new FileSystemAssetResolver(plugin.name,assetPath))
                AssetPipelineConfigHolder.registerResolver(new FileSystemAssetResolver(plugin.name,fallbackPath,true))
            }
        }
        
    }
    def doWithSpring = {
        def manifestProps = new Properties()
        def manifestFile
        try {
            manifestFile = application.getParentContext().getResource("assets/manifest.properties")
        } catch(e) {
            if(application.warDeployed) {
                log.warn "Unable to find asset-pipeline manifest, etags will not be properly generated"
            }
        }
        if(manifestFile?.exists()) {
            try {
                manifestProps.load(manifestFile.inputStream)
                application.config.grails.assets.manifest = manifestProps
                AssetPipelineConfigHolder.manifest = manifestProps
            } catch(e) {
                log.warn "Failed to load Manifest"
            }
        }

        if(!application.config.grails.assets.containsKey("precompiled")) {
            application.config.grails.assets.precompiled = application.warDeployed
        }


        AssetPipelineConfigHolder.config = application.config.grails.assets

        // Register Link Generator
        String serverURL = application.config?.grails?.serverURL ?: null
        def cacheUrls = application.config?.grails.web?.linkGenerator?.useCache
        if(!(cacheUrls instanceof Boolean)) {
            cacheUrls = true
        }


        grailsLinkGenerator(cacheUrls ? CachingLinkGenerator : LinkGenerator, serverURL) { bean ->
            bean.autowire = true
        }

        assetResourceLocator(AssetResourceLocator) { bean ->
            bean.parent = "abstractGrailsResourceLocator"
        }

    }

    def getWebXmlFilterOrder() {
        ["AssetPipelineFilter": FilterManager.GRAILS_WEB_REQUEST_POSITION - 120]
    }

    def doWithWebDescriptor = { xml ->
        def mapping = application.config?.grails?.assets?.mapping ?: "assets"
        def filterClassName = 'asset.pipeline.grails.AssetPipelineFilter'

        def filters = xml.filter[0]
        filters + {
            'filter' {
                'filter-name'('AssetPipelineFilter')
                'filter-class'(filterClassName)
            }
        }

        def mappings = xml.'filter-mapping'[0]
        mappings + {
            'filter-mapping' {
                'filter-name'('AssetPipelineFilter')
                'url-pattern'("/${mapping}/*")
                dispatcher('REQUEST')
            }
        }
    }

}
