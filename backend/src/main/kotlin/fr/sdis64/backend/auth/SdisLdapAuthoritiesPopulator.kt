package fr.sdis64.backend.auth

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.ldap.NamingException
import org.springframework.ldap.core.DirContextOperations
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator
import org.springframework.stereotype.Component

@Component
class SdisLdapAuthoritiesPopulator(
    @Autowired private val ldapConfiguration: LdapConfiguration,
) : LdapAuthoritiesPopulator {

    private val authorityRegex = Regex("CN=(\\w*),OU=.*")

    override fun getGrantedAuthorities(userData: DirContextOperations, username: String): Collection<GrantedAuthority> {
        try {
            val nameAwareAttribute = userData.attributes.get(ldapConfiguration.groupRoleAttribute)
            if (nameAwareAttribute == null || nameAwareAttribute.size() == 0) {
                LOG.error("error when parsing authorities: no attribute identified by '${ldapConfiguration.groupRoleAttribute}' found")
                return emptyList()
            }
            return nameAwareAttribute.all.asSequence()
                .mapNotNull { parseGrantedAuthority(it as String) }
                .toList()
        } catch (e: NamingException) {
            LOG.error("error when parsing authorities: ${e.message}")
            return emptyList()
        }
    }

    private fun parseGrantedAuthority(param: String): SimpleGrantedAuthority? {
        val match = authorityRegex.matchEntire(param) ?: return null
        return SimpleGrantedAuthority(match.groupValues[1])
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SdisLdapAuthoritiesPopulator::class.java)
    }
}
