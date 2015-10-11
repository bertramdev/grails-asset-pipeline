grails {
	project {
		source.level = 1.7
		target.level = 1.7

		work.dir = 'target'

		dependency {
			resolver = 'maven'

			resolution = {
				inherits 'global'
				log      'warn'

				repositories {
					mavenLocal()
					grailsCentral()
					mavenCentral()
					jcenter()
					mavenRepo 'http://dl.bintray.com/bertramlabs/asset-pipeline'
				}

				dependencies {
					compile group: 'com.bertramlabs.plugins', name: 'asset-pipeline-core', version: '2.6.0'
				}

				plugins {
					test    name: 'code-coverage',       version: '2.0.3-3', {export = false}
					build   name: 'release',             version: '3.1.1',   {export = false}
					build   name: 'rest-client-builder', version: '2.1.1',   {export = false}
					compile name: 'webxml',              version: '1.4.1'
				}
			}
		}
	}
}
