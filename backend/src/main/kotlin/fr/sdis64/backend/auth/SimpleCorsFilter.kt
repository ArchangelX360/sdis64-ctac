package fr.sdis64.backend.auth

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class SimpleCorsFilter : Filter {
    private val authorizedOrigins = arrayOf("localhost", "127.0.0.1", "sdis64.fr", "sdis64.local")

    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        val response = res as HttpServletResponse
        val request = req as HttpServletRequest

        val origin: String? = request.getHeader("Origin")
        if (authorizedOrigins.any { origin?.contains(it) == true }) {
            response.setHeader("Access-Control-Allow-Origin", origin)
        }
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE")
        response.setHeader("Access-Control-Max-Age", "3600")
        response.setHeader("Access-Control-Allow-Credentials", "true")
        response.setHeader("Access-Control-Allow-Headers", "x-requested-with, authorization, content-type, set-cookie")

        if ("OPTIONS".equals(request.method, ignoreCase = true)) {
            response.status = HttpServletResponse.SC_OK
        } else {
            chain.doFilter(req, res)
        }
    }

    override fun init(filterConfig: FilterConfig) {}

    override fun destroy() {}
}
