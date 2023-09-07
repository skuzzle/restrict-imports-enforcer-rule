var isCi = System.getenv("CI")?.toBoolean() ?: false

buildCache {
    local {
        isEnabled = true
    }
    remote(HttpBuildCache::class) {
        isEnabled = true
        url = uri("https://build-cache.taddiken.net/cache")
        if (isCi) {
            isPush = true
            credentials {
                username = System.getenv("BUILD_CACHE_USR")?.ifEmpty { null }
                password = System.getenv("BUILD_CACHE_PSW")?.ifEmpty { null }
            }
        }
    }
}
