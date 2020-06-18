package fr.sdis64.brain.test

import fr.sdis64.brain.systel.SystelClient
import fr.sdis64.brain.systel.SystelConfiguration
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.*

fun systelClientWithMockedHttp(block: SystelDataSourceMocksScope.() -> Unit): SystelClient {
    val systelConfiguration = SystelConfiguration("https://localhost:9999")
    val httpClientMock = systelHttpClientMock(block)
    return SystelClient(httpClientMock, systelConfiguration)
}

fun integerSystelResponse(n: Int) = """{"result":[[$n]],"errors":[]}"""

private fun systelHttpClientMock(block: SystelDataSourceMocksScope.() -> Unit): HttpClient {
    val handlers = SystelDataSourceMocksScope().apply { block() }.handlers
    return HttpClient(MockEngine) {
        install(HttpTimeout)
        engine {
            addHandler { request ->
                check(request.url.encodedPath == "/ServPA/rest") { "Unsupported URL ${request.url}" }
                val dataSource = request.url.parameters["ds"] ?: error("Query has no datasource specified")
                val query = Query(dataSource, request.url.parameters["p1"])
                val handler = handlers[query] ?: error("No handler registered for data source '$query'")
                handler(request)
            }
        }
    }
}

class SystelDataSourceMocksScope {
    private val _handlers: MutableMap<Query, MockRequestHandler> = mutableMapOf()
    val handlers: Map<Query, MockRequestHandler> = _handlers

    fun onDataSourceCall(query: Query, block: MockRequestHandler) {
        _handlers[query] = block
    }

    fun onDataSourceCall(dataSource: String, block: MockRequestHandler) {
        _handlers[Query(dataSource = dataSource, category = null)] = block
    }
}

data class Query(
    val dataSource: String,
    val category: String?,
)
