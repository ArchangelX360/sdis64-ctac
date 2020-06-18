package fr.sdis64.brain.auth

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutHandler

@Configuration
@EnableWebSecurity
class WebSecurityConfigurer {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain = http
        .csrf {
            it.disable()
        }.sessionManagement {
            it.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
        }.authorizeHttpRequests {
            it
                .requestMatchers(HttpMethod.POST, "/**").hasAuthority(LdapRoles.ADMIN_ROLE)
                .requestMatchers(HttpMethod.DELETE, "/**").hasAuthority(LdapRoles.ADMIN_ROLE)
                .requestMatchers("/session/**").authenticated()
                .requestMatchers("/login", "/logout").permitAll()
                .anyRequest().permitAll()
        }.exceptionHandling {
            it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // disable redirection to login on failure
        }.formLogin {
            it.successHandler(SuccessLoginHandler())
            it.failureHandler(SimpleUrlAuthenticationFailureHandler())
        }.logout {
            it.deleteCookies("JSESSIONID")
            it.invalidateHttpSession(true)
            it.addLogoutHandler(SuccessLogoutHandler())
            it.logoutSuccessHandler(HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)) // disable redirection on success logout
        }.build()

    class SuccessLoginHandler : AuthenticationSuccessHandler {
        override fun onAuthenticationSuccess(
            request: HttpServletRequest?,
            response: HttpServletResponse?,
            authentication: Authentication?
        ) {
            response?.appendToSetCookie("SameSite", "None")
            response?.status = HttpStatus.NO_CONTENT.value()
        }
    }

    class SuccessLogoutHandler : LogoutHandler {
        override fun logout(
            request: HttpServletRequest?,
            response: HttpServletResponse?,
            authentication: Authentication?
        ) {
            response?.appendToSetCookie("SameSite", "None")
            // response statusCode 204 is set by the global `logoutSuccessHandler` to override any Spring redirection
        }
    }
}

fun HttpServletResponse.appendToSetCookie(key: String, value: String) {
    val headers = this.getHeaders(HttpHeaders.SET_COOKIE)
    var firstHeader = true
    for (header in headers) { // there can be multiple Set-Cookie attributes
        if (firstHeader) {
            this.setHeader(HttpHeaders.SET_COOKIE, "$header; $key=$value")
            firstHeader = false
            continue
        }
        this.addHeader(HttpHeaders.SET_COOKIE, "$header; $key=$value")
    }
}
