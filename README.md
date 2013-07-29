Grails Asset Pipeline
=====================
The Grails `asset-pipeline` is a plugin used for managing/processing static assets. These include processing, and minification of both css, and javascript files. It is also capable of being extended to compile custom static assets, such as coffeescript.

Asset Pipeline is intended to replace the defacto Grails equivalent (`resources-plugin`) with a more efficient, developer friendly architecture (similar to rails asset-pipeline). The asset-pipeline levereges the latest in minification (UglifyJS) to reduce your asset sizes as much as possible. A few differences between the resources plugin and asset-pipeline include:

* On the fly processing - No more waiting for your assets to reload after making a change
* Compiled assets on war creation - No more hanging up application boot times while processing files.
* Reduced Dependence - The plugin has compression, minification, and cache-digests built in.
* Easy Debugging - Makes for easy debugging by keeping files seperate in development mode.
* Simpler manifests and taglibs - Read on for more information.


Usage
-----
Create a new directory in your poject: `grails-app\assets\javascript , grails-app\assets\images, grails-app\assets\stylesheets`

Place your static assets in those directories and simply include them into your layouts. Asset pipeline supports setting up manifests using these files.

Example `grails-app/javascripts/application.js` :

```javascript
//This is a javascript file with its top level require directives
//= require jquery
//= require app/models.js
//= require_tree views
//= require_self

console.log("This is my javascript manifest");
```

The above is an example of some of the require directives that can be used. Custom directives can be created and overridden into the `DirectiveProcessor` class.

Optionally, assets can be excluded from processing if included by your require tree. This can dramatically reduce compile time for your assets. To do so simply leverage the excludes configuration option:

```groovy
	grails.assets.excludes = ["tiny_mce/src/*.js"]
```

Including Assets in your Views
------------------------------
Asset pipeline provides several new tag libs for including javascript and css into your gsp files.

Example:
```gsp
<head>
	<asset:javascript src="application.js"/>
	<asset:stylesheet src="application.css"/>
</head>
```

These helpers will automatically adjust to point to the cache-digested versions of the files when running in a non-development environment.

**NOTE:** In development mode your stylesheets and javascripts will be included as individual script tags. This is intended to make it easier for debugging. Bundling is enabled in all other environments and can be forced in development mode by adding `grails.assets.bundle=true` to your `Config.groovy`.

Precompiling For Production
---------------------------
Assets should be compiled before building a war file. This can be done by running `grails asset-precompile`

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
		grails.assets.url = "http://s3.amazonaws.com/asset-pipe/assets"
	}
}
```

Custom Files
------------
Asset Pipeline has defined a new Grails artefact type called `AssetFile`. By default, this plugin comes with a `JsAssetFile`, and `CssAssetFile`. These define the match pattern syntax for understanding requires directives, known extensions, processors, and content-type. The application bases its file look-up on content-type of the request rather than extension. This allows the user to maybe define a `CoffeeAssetFile` with the javascript content type and a request to `localhost/assets/app.js` would be able to find `assets/app.coffee`.


Things to be Done
-----------------
* Add more configuration options.
* Tests would be good.

Additional Resources
--------------------
* [Rails Asset Pipeline Guide](http://guides.rubyonrails.org/asset_pipeline.html)
