package com.poc.graphql.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.security.saml2.relyingparty.registration.sso.assertingparty.metadata-uri=classpath:saml/idp-metadata.xml")
@AutoConfigureMockMvc
class SecurityConfigTests {

    private static final String USERS_QUERY = "{\"query\":\"{ users { success } }\"}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/login.html"));
    }

    @Test
    void pagesRedirectToLoginWhenSignedOut() throws Exception {
        mockMvc.perform(get("/").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void graphQlReturns401WhenSignedOut() throws Exception {
        mockMvc.perform(post("/graphql").contentType(MediaType.APPLICATION_JSON).content(USERS_QUERY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void restApiReturns401WhenSignedOut() throws Exception {
        mockMvc.perform(delete("/api/users/1/profile-image").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void graphQlIsAvailableWhenSignedIn() throws Exception {
        mockMvc.perform(post("/graphql").with(user("jane@example.com")).contentType(MediaType.APPLICATION_JSON).content(USERS_QUERY))
                .andExpect(status().isOk());
    }

    @Test
    void restApiRequiresCsrfToken() throws Exception {
        mockMvc.perform(delete("/api/users/1/profile-image").with(user("jane@example.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void ssoLinkStartsSamlLogin() throws Exception {
        mockMvc.perform(get("/saml2/authenticate/sso"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", Matchers.startsWith("https://mocksaml.com/api/saml/sso?SAMLRequest=")));
    }

    @Test
    void serviceProviderMetadataIsPublic() throws Exception {
        mockMvc.perform(get("/saml2/metadata/sso"))
                .andExpect(status().isOk());
    }
}
