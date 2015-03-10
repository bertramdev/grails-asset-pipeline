package asset.pipeline.grails.fs
import org.springframework.core.io.support.*
import org.springframework.core.io.*


class EmptyResource implements Resource {
	boolean exists() {
		return false
	}

	Resource createRelative(String relativePath) {
		return null
	}

	String getDescription() {
		return null
	}

	File getFile() {
		return null
	}
	String getFilename() {
		return null
	}
	URI getURI() {
		return null
	}
	URL getURL() {
		return null
	}
	InputStream getInputStream() {
		return null
	}

	boolean isOpen() {
		return false
	}
	boolean isReadable() {
		return false
	}

	long contentLength() {
		return 0
	}

	long lastModified() {
		return 0
	}

}