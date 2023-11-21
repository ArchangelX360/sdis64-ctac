package fr.sdis64.backend.auth

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.ldap.core.support.BaseLdapPathContextSource
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.ldap.LdapBindAuthenticationManagerFactory
import org.springframework.security.ldap.DefaultSpringSecurityContextSource
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator

@Configuration
class LdapConfigurer {
    @Bean
    fun contextSource(
        @Autowired ldapConfiguration: LdapConfiguration,
    ): BaseLdapPathContextSource {
        val context = DefaultSpringSecurityContextSource(ldapConfiguration.url)
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
        factory.setLdapAuthoritiesPopulator(SdisLdapAuthoritiesPopulator(ldapConfiguration))
        return factory.createAuthenticationManager()
    }

    @Bean
    fun authorities(
        contextSource: BaseLdapPathContextSource,
        @Autowired ldapConfiguration: LdapConfiguration,
    ): LdapAuthoritiesPopulator {
        val authorities = DefaultLdapAuthoritiesPopulator(contextSource, "")
        authorities.setGroupRoleAttribute(ldapConfiguration.groupRoleAttribute)
        return authorities
    }
}