package asset.pipeline.grails.fs

import asset.pipeline.*
import asset.pipeline.fs.*
import org.springframework.core.io.support.*
import org.springframework.core.io.*


class SpringAssetCache extends Thread {
	private SpringResourceAssetResolver resolver

	public SpringAssetCache(SpringResourceAssetResolver resolver) {
		this.resolver = resolver
	}

	public void run() {
		resolver.cacheAllResources()
	}
}