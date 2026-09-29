package com.poc.graphql.security;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AssertionAuthentication;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

// Reads the user's email and display name out of the SAML assertion; attribute names differ per IdP so they are configurable
@Component
public class SamlUserDetails {

    private final String emailAttribute;
    private final String firstNameAttribute;
    private final String lastNameAttribute;

    public SamlUserDetails(@Value("${app.saml.attribute.email:email}") String emailAttribute,
            @Value("${app.saml.attribute.first-name:firstName}") String firstNameAttribute,
            @Value("${app.saml.attribute.last-name:lastName}") String lastNameAttribute) {
        this.emailAttribute = emailAttribute;
        this.firstNameAttribute = firstNameAttribute;
        this.lastNameAttribute = lastNameAttribute;
    }

    // Falls back to the NameID when the IdP does not send an email attribute
    public String email(Authentication authentication) {
        String email = firstAttribute(authentication, emailAttribute);
        return StringUtils.hasText(email) ? email : authentication.getName();
    }

    public String name(Authentication authentication) {
        String name = Stream.of(firstAttribute(authentication, firstNameAttribute), firstAttribute(authentication, lastNameAttribute))
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(" "));
        return StringUtils.hasText(name) ? name : email(authentication);
    }

    private String firstAttribute(Authentication authentication, String attribute) {
        List<Object> values = attributes(authentication).get(attribute);
        return (values == null || values.isEmpty() || values.get(0) == null) ? null : values.get(0).toString();
    }

    private Map<String, List<Object>> attributes(Authentication authentication) {
        if (authentication instanceof Saml2AssertionAuthentication saml) {
            return saml.getCredentials().getAttributes();
        }
        if (authentication.getPrincipal() instanceof Saml2AuthenticatedPrincipal principal) {
            return principal.getAttributes();
        }
        return Map.of();
    }
}
