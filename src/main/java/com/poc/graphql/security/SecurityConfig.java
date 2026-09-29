package com.poc.graphql.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SamlLoginSuccessHandler successHandler) throws Exception {
        PathPatternRequestMatcher.Builder path = PathPatternRequestMatcher.withDefaults();
        RequestMatcher apiRequests = new OrRequestMatcher(path.matcher("/graphql"), path.matcher("/api/**"));

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/login.html", "/css/**", "/favicon.ico", "/error").permitAll()
                        .anyRequest().authenticated())
                .saml2Login(saml -> saml
                        .loginPage("/login")
                        .successHandler(successHandler))
                // Serves the SP metadata at /saml2/metadata/{registrationId} for registering this app with the IdP
                .saml2Metadata(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
                // API clients get a 401 instead of a redirect to the login page
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED), apiRequests))
                // /graphql only accepts JSON bodies, which a cross-site form cannot send, so it is safe to exempt
                .csrf(csrf -> csrf
                        .spa()
                        .ignoringRequestMatchers("/graphql"));
        return http.build();
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("forward:/login.html");
    }
}
