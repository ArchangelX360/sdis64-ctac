package fr.sdis64.brain.auth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ctac.ldap")
data class LdapConfiguration(
    val userDnPattern: String,
    val managerDn: String,
    val managerPassword: String,
    val url: String,
    val userSearchBase: String,
    val userSearchFilter: String,
    val groupRoleAttribute: String,
)

object LdapRoles {
    const val ADMIN_ROLE = "GG_CHEFFERIE_CTAC"
}
