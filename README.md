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


Custom Files
------------
Asset Pipeline has defined a new grails artefact type called `AssetFile`. By default, this plugin comes with a `JsAssetFile`, and `CssAssetFile`. These define the match pattern syntax for understanding requires directives, known extensions, processors, and content-type. The application bases its file look-up on content-type of the request rather than extension. This allows the user to maybe define a `CoffeeAssetFile` with the javascript content type and a request to `localhost/assets/app.js` would be able to find `assets/app.coffee`.

Precompiling For Production
---------------------------
This is still in development but a gant script is started that needs finished called `assets-precompile`. UglifyJs has been ported, in this project, to Rhino, and is ready to be added as a precompile postprocessor.


Things to be Done
-----------------
* Finish assets-precompile script for production use. This includes cache-digests and appropriate taglibs with manifest to alias to the new cache-digest file names.
* Finish Minification Post Processor (UglifyJS ready, just needs hooked up.)
* Add more configuration options, such as location for assets to be compiled to.
* Tests would be good.
