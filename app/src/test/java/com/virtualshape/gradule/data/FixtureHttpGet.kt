package com.virtualshape.gradule.data

/** Тестовый [HttpGet]: отдаёт заранее заданные ответы, в сеть не ходит. */
class FixtureHttpGet(
    private val responses: Map<String, String> = emptyMap(),
) : HttpGet {
    val requestedUrls = mutableListOf<String>()

    override fun get(url: String): String {
        requestedUrls += url
        return requireNotNull(responses[url]) { "нет фикстуры для $url" }
    }

    companion object {
        /** Текст classpath-фикстуры из `/fixtures/`. */
        fun fixture(name: String): String {
            val stream = requireNotNull(FixtureHttpGet::class.java.getResourceAsStream("/fixtures/$name")) { "нет фикстуры $name" }
            return stream.bufferedReader().use { it.readText() }
        }
    }
}
