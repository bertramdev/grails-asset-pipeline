package asset.pipeline.processors

import org.mozilla.javascript.Context
import org.mozilla.javascript.Function
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.tools.shell.Global

class UglifyJsProcessor {
  private Scriptable scope
  private Function uglify

	UglifyJsProcessor() {
    ClassLoader classLoader = getClass().classLoader
    URL parserLib = classLoader.getResource('uglifyjs/lib/parse-js.js')
    URL processLib = classLoader.getResource('uglifyjs/lib/process.js')
    URL consolidatorLib = classLoader.getResource('uglifyjs/lib/consolidator.js')
    URL squeezeLib = classLoader.getResource('uglifyjs/lib/squeeze-more.js')
    URL uglifyJs = classLoader.getResource('uglifyjs/uglify-js.js')
    Context cx = Context.enter()
    cx.optimizationLevel = 9
    Global global = new Global()
    global.init cx
    scope = cx.initStandardObjects(global)
    cx.evaluateString scope, uglifyJs.text, uglifyJs.file, 1, null
    cx.evaluateString scope, parserLib.text, parserLib.file, 1, null
    cx.evaluateString scope, processLib.text, processLib.file, 1, null
    cx.evaluateString scope, consolidatorLib.text, consolidatorLib.file, 1, null
    cx.evaluateString scope, squeezeLib.text, squeezeLib.file, 1, null
    uglify = scope.get("uglify", scope)
    Context.exit()
  }

  def process(inputText) {
    call uglify, inputText
  }

  private synchronized String call(Function fn, Object[] args) {
        Context.call(null, fn, scope, scope, args)
  }
}
