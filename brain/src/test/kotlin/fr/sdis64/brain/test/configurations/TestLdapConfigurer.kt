package fr.sdis64.brain.test.configurations

import fr.sdis64.brain.auth.LdapConfiguration
import fr.sdis64.brain.auth.LdapRoles
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.ldap.core.support.BaseLdapPathContextSource
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.ldap.DefaultSpringSecurityContextSource
import org.springframework.security.ldap.server.UnboundIdContainer
import org.springframework.test.util.TestSocketUtils

@Configuration
class TestLdapConfigurer {
    private val randomLdapPort: Int = TestSocketUtils.findAvailableTcpPort()

    @Bean
    fun ldapContainer(): UnboundIdContainer {
        val container = UnboundIdContainer("dc=sdis64,dc=fr", "classpath:test-users.ldif")
        container.port = randomLdapPort
        return container
    }

    @Bean
    fun contextSource(
            @Autowired ldapConfiguration: LdapConfiguration,
    ): BaseLdapPathContextSource {
        val context = DefaultSpringSecurityContextSource("ldap://localhost:$randomLdapPort")
        context.userDn = ldapConfiguration.managerDn
        context.password = ldapConfiguration.managerPassword
        return context
    }

    @Bean
    fun authenticationManager(
            contextSource: BaseLdapPathContextSource,
            @Autowired ldapConfiguration: LdapConfiguration,
    ): AuthenticationManager {
        val factory = LdapBindAuthenticationManagerFactory(contextSource)
        factory.setUserDnPatterns(ldapConfiguration.userDnPattern)
        factory.setUserSearchFilter(ldapConfiguration.userSearchFilter)
        factory.setUserSearchBase(ldapConfiguration.userSearchBase)
        // embedded LDAP does not support group, so we hack it
        factory.setLdapAuthoritiesPopulator { _, username ->
            when (username) {
                "admin-user" -> listOf(SimpleGrantedAuthority(LdapRoles.ADMIN_ROLE))
                else -> emptyList()
            }
        }
        return factory.createAuthenticationManager()
    }
}
