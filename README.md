Grails Asset Pipeline
=====================
[![Build Status](https://travis-ci.org/bertramdev/asset-pipeline.png?branch=master)](https://travis-ci.org/bertramdev/asset-pipeline)

The Grails `asset-pipeline` is a plugin used for managing/processing static assets. These include processing, and minification of both css, and javascript files. It is also capable of being extended to compile custom static assets, such as coffeescript.

Asset Pipeline is intended to replace the defacto Grails equivalent (`resources-plugin`) with a more efficient, developer friendly architecture (similar to rails asset-pipeline). The asset-pipeline levereges the latest in minification (UglifyJS) to reduce your asset sizes as much as possible. A few differences between the resources plugin and asset-pipeline include:

* On the fly processing - No more waiting for your assets to reload after making a change
* Compiled assets on war create - No more hanging up application boot times while processing files. `grails war`
* Reduced Dependence - The plugin has compression, minification, and cache-digests built in.
* Easy Debugging - Makes for easy debugging by keeping files seperate in development mode.
* Simpler manifests and taglibs - Read on for more information.


Usage
-----
Asset-Pipeline automatically creates a series of folders within your grails-app directory: `grails-app\assets\javascript , grails-app\assets\images, grails-app\assets\stylesheets`

Place your static assets in those directories and simply include them into your layouts. Asset pipeline supports setting up manifests using these files.

Example `grails-app/assets/javascripts/application.js` :

```javascript
//This is a javascript file with its top level require directives
//= require jquery
//= require app/models.js
//= require_tree views
//= require_self

console.log("This is my javascript manifest");
```

The above is an example of some of the require directives that can be used. Custom directives can be created and overridden into the `DirectiveProcessor` class.
**NOTE**: Make sure an empty line does not exist between the top of the file and your require directives, these requires will be ignored!

Optionally, assets can be excluded from processing if included by your require tree. This can dramatically reduce compile time for your assets. To do so simply leverage the excludes configuration option:

```groovy
	grails.assets.excludes = ["tiny_mce/src/*.js"]
```

Or Exclude at the plugin level:

```groovy
  grails.assets.plugin."twitter-bootstrap".excludes = ["**/*.less"]
  grails.assets.plugin."twitter-bootstrap".includes = ["bootstrap.less"]
```

The above will tell asset pipeline not to precompile less files individually within the twitter-bootstrap plugin, but will compile bootstrap.less

Including Assets in your Views
------------------------------
Asset pipeline provides several new tag libs for including javascript and css into your gsp files.

Example:
```gsp
<head>
	<asset:stylesheet src="application.css"/>
	<asset:javascript src="application.js"/>
	<asset:link rel="shortcut icon" href="favicon.ico" type="image/x-icon"/>
</head>
```

These helpers will automatically adjust to point to the cache-digested versions of the files when running in a non-development environment.

**NOTE:** In development mode your stylesheets and javascripts will be included as individual script tags. This is intended to make it easier for debugging. Bundling is enabled in all other environments and can be forced in development mode by adding `grails.assets.bundle=true` to your `Config.groovy`.

### Why this instead of the grails resouces plugin?
There are several advantages to using the asset-pipeline instead of the standard grails resources plugin.

* File dependencies are in the top of your assets. (No Resources.groovy)
* Assets in plugins become level with your app.
* On the fly processing in Development mode (No more waiting for reloads)
* Coffeescript, LESS, and others become first class citizens ( debuggable )
* Require entire folder trees with one line
* Better minification (UglifyJs) , and compiling before the WAR is built
* Faster application startup time
* Easy extensibility

[Read More Here](https://github.com/bertramdev/asset-pipeline/wiki/Why-asset-pipeline-over-grails-resources-plugin)

Plugin Resources
----------------
Asset pipeline makes it easy to serve assets from within plugins. It's actually quite simple. The `grails-app/assets`, and 'web-app' (for legacy plugin support) folders from all plugins are considered include paths. Essentially, when a file is requested (i.e. `jquery.js`) The asset pipeline first will check the local applications assets folder. If it is not found it will scan through all the install plugins and serve the requested file. This has the added benefit of allowing you to override a plugins copy of the js file in your local project.

Stylesheets
-----------
**NEW**: Asset Pipeline now automatically tries to convert relative urls specified in your css file to absolute paths. This makes it easier to use third party libraries within the asset-pipeline stack.

Precompiling For Production
---------------------------
Assets are now automatically precompiled into `target/assets` when you create a war file. This should further simplify the deployment process. During a war create, only the changed assets are compiled making your precompiler phase a bit quicker. If, for any reason, you want to ensure a clean assets folder, feel free to run `grails asset-clean`

Serving Assets from External Storage Directory
----------------------------------------------
Asset Pipeline can be configured to copy your assets files out into an external storage path. This can be useful for setting up your web server (i.e. Nginx) to directly server your static assets. To do so, simply define a config variable in your `Config.groovy` environment block

```groovy
environments {
	production {
		grails.assets.storagePath = "/full/path/to/storage"
	}
}
```

It is also possible to configure a custom CDN asset url for serving this assets:

```groovy
environments {
	production {
		grails.assets.url = "http://s3.amazonaws.com/asset-pipe/assets/"
	}
}
```

Custom Files
------------
Asset Pipeline uses classes of type `AssetFile`. By default, this plugin comes with a `JsAssetFile`, and `CssAssetFile`. These define the match pattern syntax for understanding requires directives, known extensions, processors, and content-type. The application bases its file look-up on content-type of the request rather than extension. This allows the user to maybe define a `CoffeeAssetFile` with the javascript content type and a request to `localhost/assets/app.js` would be able to find `assets/app.coffee`. To add custom file definitions you must add the definition in 2 locations:

1. Add the reference to your AssetHelper.assetSpecs static property in your plugins startup or Bootstrap:

```groovy
def doWithDynamicMethods = { ctx ->
	AssetHelper.assetSpecs << HandlebarsAssetFile
}
```

2. Add an `_Events.groovy` file and register an event listener for `eventAssetPrecompileStart`:

```groovy
eventAssetPrecompileStart = { assetConfig ->
	assetConfig.specs << 'asset.pipeline.handlebars.HandlebarsAssetFile'
}
```

We do this instead of Artefacts so that we do not have to load up the full application stack during precompile. This saves > 30% of memory usage right off the bat and reduces compile time significantly.


Things to be Done
-----------------
* Increase Test Coverage.

Contributions
-------------
All contributions are of course welcome as this is an ACTIVE project. Any help with regards to reviewing platform compatibility, adding more tests, and general cleanup is most welcome.
Thanks to several people for suggestions throughout development. Notably: Rick Jensen (@cdeszaq), and more to follow I'm sure...

Additional Resources
--------------------
* [Coffeescript Asset-Pipeline Plugin](http://github.com/bertramdev/coffee-grails-asset-pipeline)
* [LESS Css Asset-Pipeline Plugin](http://github.com/bertramdev/less-grails-asset-pipeline)
* [Handlebars Asset-Pipeline Plugin](http://github.com/bertramdev/handlebars-grails-asset-pipeline)
* [Ember Asset-Pipeline Plugin](http://github.com/bertramdev/ember-grails-asset-pipeline)
* [Rails Asset Pipeline Guide](http://guides.rubyonrails.org/asset_pipeline.html)
