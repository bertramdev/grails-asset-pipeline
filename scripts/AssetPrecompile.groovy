import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.apache.tools.ant.DirectoryScanner
import java.security.MessageDigest
import java.util.Properties;

// import asset.pipeline.*;

includeTargets << grailsScript("_GrailsBootstrap")
includeTargets << new File("${assetPipelinePluginDir}/scripts/_AssetCompile.groovy")

target(assetPrecompile: "Precompiles assets in the application as specified by the precompile glob!") {
   assetCompile()
}



setDefaultTarget(assetPrecompile)
