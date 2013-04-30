package asset.pipeline;
import org.codehaus.groovy.grails.commons.AbstractInjectableGrailsClass;
import org.codehaus.groovy.grails.commons.GrailsClassUtils;
 
public class DefaultGrailsAssetFileClass extends AbstractInjectableGrailsClass implements GrailsAssetFileClass {
 
    public DefaultGrailsAssetFileClass(Class clazz) {
        super(clazz, AssetFileArtefactHandler.SUFFIX);
    }
 
	//any methods you define 
 
}