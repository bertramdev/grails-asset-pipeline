Grails Asset Pipeline
=====================
The Grails asset-pipeline is a port from the rails asset-pipeline into the grails world. It allows similar require directives within the grails-app/assets folder.


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

Precompiling For Production
---------------------------
Assets are automatically compiled when a war is created into the web-app/assets folder. They can also manually be recompiled by calling `grails asset-precompile`.

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

Custom Files
------------
Asset Pipeline has defined a new grails artefact type called `AssetFile`. By default, this plugin comes with a `JsAssetFile`, and `CssAssetFile`. These define the match pattern syntax for understanding requires directives, known extensions, processors, and content-type. The application bases its file look-up on content-type of the request rather than extension. This allows the user to maybe define a `CoffeeAssetFile` with the javascript content type and a request to `localhost/assets/app.js` would be able to find `assets/app.coffee`.


Things to be Done
-----------------
* Add more configuration options.
* Tests would be good.
