package asset.pipeline;
import org.codehaus.groovy.grails.commons.ArtefactHandlerAdapter;
 
public class AssetFileArtefactHandler extends ArtefactHandlerAdapter {
 
    // the name for these artefacts in the application
    static public final String TYPE = "AssetFile";
 
    // the suffix of all someHandler classes (i.e. how they are identified as someHandlers)
    static public final String SUFFIX = "AssetFile";
 
    // pass interface type and default impl to the supertype
    public AssetFileArtefactHandler() {
        super(TYPE, GrailsAssetFileClass.class, DefaultGrailsAssetFileClass.class, SUFFIX);
    }
}