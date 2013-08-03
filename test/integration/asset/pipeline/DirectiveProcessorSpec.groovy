package asset.pipeline

import grails.plugin.spock.IntegrationSpec

class DirectiveProcessorSpec extends IntegrationSpec {
	def "gets entire tree when required"() {
		given: "A uri and file extension with require directives"
			def uri                = "asset-pipeline/test/test.js"
			def fileExtension      = "js"
			def contentType        = "application/javascript"
			def file               = AssetHelper.fileForUri(uri, contentType, fileExtension)
			def directiveProcessor = new DirectiveProcessor(contentType)
		when:
			def fileContent = directiveProcessor.compile(file).toString()
		then:
			fileContent.contains("This is File B") && fileContent.contains("This is File A") && fileContent.contains("This is File C") && fileContent.contains("console.log(\"Subset A\");");
	}

	def "gets dependency list flattened with all files"() {
		given: "A uri and file extension with require directives"
			def uri                = "asset-pipeline/test/test.js"
			def fileExtension      = "js"
			def contentType        = "application/javascript"
			def file               = AssetHelper.fileForUri(uri, contentType, fileExtension)
			def directiveProcessor = new DirectiveProcessor(contentType)
		when:
			def dependencyList = directiveProcessor.getFlattenedRequireList(file)
			println dependencyList
		then:
			dependencyList.size() == 5
	}

	def "gets dependency list order correct"() {
		given: "A uri and file extension with require directives"
			def uri                = "asset-pipeline/test/test.js"
			def fileExtension      = "js"
			def contentType        = "application/javascript"
			def file               = AssetHelper.fileForUri(uri, contentType, fileExtension)
			def directiveProcessor = new DirectiveProcessor(contentType)
		when:
			def dependencyList = directiveProcessor.getFlattenedRequireList(file)
		then:
			dependencyList.findIndexOf{ it == "asset-pipeline/test/libs/file_c.js" } < dependencyList.findIndexOf{ it == "asset-pipeline/test/libs/file_b.js"}
	}
}
