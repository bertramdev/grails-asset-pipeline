grails.project.work.dir = 'target'
grails.project.target.level = 1.6
grails.project.source.level = 1.6

grails.project.dependency.resolver = "maven"

grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
        jcenter()
        mavenRepo "http://dl.bintray.com/bertramlabs/asset-pipeline"
    }

    dependencies {
        runtime 'org.mozilla:rhino:1.7R4'
        runtime 'commons-io:commons-io:2.2'
        compile("com.bertramlabs.plugins:asset-pipeline-core:2.1.1")

        //Temporary inclusion due to bug in 2.4.2
        compile("cglib:cglib-nodep:2.2.2") {
            export = false
        }
    }

    plugins {
        compile(":webxml:1.4.1")

        build ':release:3.0.1', ':rest-client-builder:2.0.1', {
            export = false
        }
        test ":code-coverage:1.2.7", {
            export = false
        }
    }
}
