grails.project.work.dir = 'target'
grails.project.target.level = 1.6
grails.project.source.level = 1.6

grails.project.dependency.resolution = {

    inherits 'global'
    log 'warn'

    repositories {
        grailsCentral()
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        runtime 'org.mozilla:rhino:1.7R4'

        test 'org.spockframework:spock-grails-support:0.7-groovy-2.0', {
            export = false
        }
    }

    plugins {
        compile(":webxml:1.4.1") 

        build ':release:2.2.1', ':rest-client-builder:1.0.3', {
            export = false
        }
        test ":code-coverage:1.2.7"
        
        test ':spock:0.7', {
            excludes 'spock-grails-support', 'hibernate', 'grails-hibernate'
            export = false
        }
    }
}
