package asset.pipeline.grails

class AssetProcessorService {
    static transactional = false
    def grailsApplication

    /**
     * Retrieves the asset path from the property [grails.assets.mapping] which is used by the url mapping and the
     * taglib.  The property cannot contain <code>/</code>, and must be one level deep
     *
     * @return the path
     * @throws IllegalArgumentException if the path contains <code>/</code>
     */
    String getAssetMapping() {
        def path = grailsApplication.config?.grails?.assets?.mapping ?: "assets"
        if (path.contains("/")) {
            String message = "the property [grails.assets.mapping] can only be one level" +
                    "deep.  For example, 'foo' and 'bar' would be acceptable values, but 'foo/bar' is not"
            throw new IllegalArgumentException(message)
        }

        return path
    }


}
