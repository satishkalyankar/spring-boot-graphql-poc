package com.poc.graphql.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.poc.graphql.service.UserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Creates the local user on first SAML sign-in, then continues to the originally requested page
@Component
public class SamlLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserService userService;
    private final SamlUserDetails samlUserDetails;

    public SamlLoginSuccessHandler(UserService userService, SamlUserDetails samlUserDetails) {
        this.userService = userService;
        this.samlUserDetails = samlUserDetails;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws ServletException, IOException {
        userService.provisionSsoUser(samlUserDetails.email(authentication), samlUserDetails.name(authentication));
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
