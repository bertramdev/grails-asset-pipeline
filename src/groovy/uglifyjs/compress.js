/* http://lisperator.net/uglifyjs/compress */
function compress(code, options) {
	var ast = UglifyJS.parse(code);
	ast.figure_out_scope();

	var compressor = UglifyJS.Compressor({});
	ast = ast.transform(compressor);

	if (options.mangle) {
		ast.figure_out_scope();
		ast.compute_char_frequency();
		ast.mangle_names();
	}

	var source_map = UglifyJS.SourceMap.call(UglifyJS,{});
	var stream = UglifyJS.OutputStream.call(UglifyJS,{
	    source_map: source_map
	});

	ast.print(stream);
	

	// var code = stream.toString();
	var map = source_map.toString();
	return ast.print_to_string();
}