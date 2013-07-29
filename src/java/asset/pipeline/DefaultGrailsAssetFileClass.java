package asset.pipeline;

import org.codehaus.groovy.grails.commons.AbstractInjectableGrailsClass;

public class DefaultGrailsAssetFileClass extends AbstractInjectableGrailsClass implements GrailsAssetFileClass {

    public DefaultGrailsAssetFileClass(Class<?> clazz) {
        super(clazz, AssetFileArtefactHandler.SUFFIX);
    }
}
