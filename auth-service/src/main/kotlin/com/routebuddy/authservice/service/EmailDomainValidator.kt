package com.routebuddy.authservice.service

import org.springframework.stereotype.Component
import java.util.Hashtable
import javax.naming.NamingException
import javax.naming.directory.InitialDirContext

@Component
class EmailDomainValidator {
    fun hasResolvableDomain(email: String): Boolean {
        val domain = email.substringAfter('@', "").trim()
        if (domain.isBlank() || domain.endsWith('.')) return false
        return hasRecord(domain, "MX") || hasRecord(domain, "A") || hasRecord(domain, "AAAA")
    }

    private fun hasRecord(domain: String, type: String): Boolean {
        return try {
            val env = Hashtable<String, String>()
            env["java.naming.factory.initial"] = "com.sun.jndi.dns.DnsContextFactory"
            val attrs = InitialDirContext(env).getAttributes(domain, arrayOf(type))
            val attr = attrs.get(type)
            attr != null && attr.size() > 0
        } catch (_: NamingException) {
            false
        }
    }
}
