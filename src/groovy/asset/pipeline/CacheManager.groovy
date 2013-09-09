package asset.pipeline

class CacheManager {
	static cache = [:]

	static def findCache(fileName, md5) {
		def cacheRecord = CacheManager.cache[fileName]

		if(cacheRecord && cacheRecord.md5 == md5) {
			def cacheFiles = cacheRecord.dependencies.keySet()
			def expiredCacheFound = cacheFiles.find { cacheFileName ->
				def cacheFile = new File(cacheFileName)
				if(!cacheFile.exists())
					return true
				def depMd5 = AssetHelper.getByteDigest(cacheFile.bytes)
				if(cacheRecord.dependencies[cacheFileName] != depMd5) {
					return true
				}
				return false
			}

			if(expiredCacheFound) {
				CacheManager.cache.remove(fileName)
				return null
			}
			return cacheRecord.processedFileText
		} else {
			CacheManager.cache.remove(fileName)
			return null
		}
	}

	static def createCache(fileName, md5Hash, processedFileText) {
		def cacheRecord = CacheManager.cache[fileName]
		if(cacheRecord) {
			CacheManager.cache[fileName] = cacheRecord + [
				md5: md5Hash,
				processedFileText: processedFileText
			]
		} else {
			CacheManager.cache[fileName] = [
				md5: md5Hash,
				processedFileText: processedFileText,
				dependencies: [:]
			]
		}

	}

	static def addCacheDependency(fileName, dependentFile) {

		def cacheRecord = CacheManager.cache[fileName]
		if(!cacheRecord) {
			CacheManager.createCache(fileName, null, null)
			cacheRecord = CacheManager.cache[fileName]
		}
		def newMd5 = AssetHelper.getByteDigest(dependentFile.bytes)
		cacheRecord.dependencies[dependentFile.canonicalPath] = newMd5
	}
}
