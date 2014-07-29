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

import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject
import org.mozilla.javascript.tools.shell.Global
import org.mozilla.javascript.NativeObject

class UglifyJsProcessor {
    static contentTypes = ['application/javascript']
    private Scriptable scope
    private Function uglify

    UglifyJsProcessor() {
        ClassLoader classLoader = getClass().classLoader
        URL uglifyLib = classLoader.getResource('uglifyjs/tools/node.js')

        def uglifyLibs = [
            "lib/utils.js",
            "lib/ast.js",
            "lib/parse.js",
            "lib/transform.js",
            "lib/scope.js",
            "lib/output.js",
            "lib/compress.js",
            "lib/sourcemap.js",
            "lib/mozilla-ast.js"
        ]
        // URL sourceMapLib = classLoader.getResource('uglifyjs/sourcemap.js')
        URL compressLib = classLoader.getResource('uglifyjs/compress.js')
        
        Context cx = Context.enter()
        cx.optimizationLevel = 9
        Global global = new Global()
        global.init cx
        // scope = cx.initStandardObjects(global)
        Scriptable sharedScope = cx.initStandardObjects(global)
        Scriptable uglifyJsScope = cx.newObject(sharedScope)
        sharedScope.defineProperty('UglifyJS',uglifyJsScope,ScriptableObject.DONTENUM)

        scope = cx.newObject(sharedScope);
        scope.setPrototype(sharedScope);
        scope.setParentScope(null);
        Scriptable argsObj = cx.newArray(sharedScope, [] as Object[]);
        sharedScope.defineProperty("arguments", argsObj, ScriptableObject.DONTENUM);

        requireJs(cx,sharedScope, 'MOZ_SourceMap', 'uglifyjs/source-map.js')

        uglifyLibs.each { libPath ->
            loadIntoContext(cx, uglifyJsScope, "uglifyjs/${libPath}")
        }
        

        cx.evaluateString sharedScope, uglifyLib.text, uglifyLib.file, 1 , null
        cx.evaluateString scope, compressLib.text, compressLib.file, 1 , null
        

        uglify = scope.get("compress", scope)
        Context.exit()
    }


    def loadIntoContext(cx, scope, filePath) {
        ClassLoader classLoader = getClass().classLoader
        URL scriptToLoad = classLoader.getResource(filePath)

        cx.evaluateString scope, scriptToLoad.text, scriptToLoad.file, 1, null
    }

    def requireJs(cx,scope, propertyName ,filePath) {
        ClassLoader classLoader = getClass().classLoader
        Scriptable requireScope = cx.newObject(scope)
        Scriptable exportScope = cx.newObject(requireScope)
        URL scriptToLoad = classLoader.getResource(filePath)
        requireScope.defineProperty('exports',exportScope,ScriptableObject.DONTENUM)

        cx.evaluateString requireScope, scriptToLoad.text, scriptToLoad.file, 1, null

        scope.defineProperty(propertyName,exportScope,ScriptableObject.DONTENUM)
    }

    def process(inputText, options = [:]) {
        call uglify, inputText, parseOptions(options)
    }

    private NativeObject parseOptions(options) {

        def jsOptions = new NativeObject()

        options.each{ it ->
            if(it.key == 'strictSemicolons') {
                // jsOptions.put('strict_semicolons', scope, it.value)
                jsOptions.defineProperty("strict_semicolons",it.value, NativeObject.READONLY)
            } else if(it.key == 'mangleOptions' || it.key == 'genOptions') {
                def nestedMap = new NativeObject()
                def key = (it.key == 'mangleOptions' ? 'mangle_options' : 'gen_options')
                it.value.each { nested ->
                    nestedMap.defineProperty(nested.key, nested.value, NativeObject.READONLY)
                    // nestedMap.put(nested.key, scope,nested.value)
                }
                jsOptions.defineProperty(key,nestedMap, NativeObject.READONLY)
            }
        }
        return jsOptions
    }

    private synchronized String call(Function fn, Object[] args) {
        Context.call(null, fn, scope, scope, args)
    }
}
