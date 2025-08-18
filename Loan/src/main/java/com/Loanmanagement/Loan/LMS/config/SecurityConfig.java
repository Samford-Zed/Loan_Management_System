package com.Loanmanagement.Loan.LMS.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity // Enables method-level security annotations like @PreAuthorize, @PostAuthorize
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter; // Or AuthTokenFilter, if you renamed it
    private final UserDetailsService userDetailsService; // Your implementation of UserDetailsService
    private final LogoutHandler logoutHandler;           // Your custom logout handler

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF for stateless REST APIs, as JWTs are inherently secure against CSRF attacks
                .csrf(AbstractHttpConfigurer::disable)
                // Configure authorization rules for HTTP requests
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to authentication endpoints and Swagger UI documentation
                        .requestMatchers(
                                "/api/auth/**", // All endpoints under /api/auth (e.g., login, register)
                                "/v2/api-docs",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/swagger-resources",
                                "/swagger-resources/**",
                                "/configuration/ui",
                                "/configuration/security",
                                "/swagger-ui/**", // Swagger UI resources
                                "/webjars/**",   // Webjars for Swagger UI
                                "/swagger-ui.html" // Main Swagger UI page
                        ).permitAll() // These paths do not require authentication
                        // Specific role-based access rules:
                        // Only users with the "ADMIN" role can access paths under /api/admin/
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // Users with either "USER" or "ADMIN" roles can access paths under /api/user/
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )
                // Configure session management to be stateless, suitable for JWT-based authentication
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // Register the custom authentication provider that uses UserDetailsService and PasswordEncoder
                .authenticationProvider(authenticationProvider())
                // Add the JWT authentication filter before Spring Security's default UsernamePasswordAuthenticationFilter
                // This ensures your JWT is processed first to authenticate the user
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Configure logout behavior
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout") // The URL that triggers logout
                        .addLogoutHandler(logoutHandler) // Custom handler for token invalidation/cleanup
                        .logoutSuccessHandler((request, response, authentication) ->
                                SecurityContextHolder.clearContext()) // Clear security context on successful logout
                );

        return http.build(); // Build and return the SecurityFilterChain
    }

    /**
     * Configures and provides the AuthenticationProvider.
     * Uses DaoAuthenticationProvider, which works with UserDetailsService and a PasswordEncoder.
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Sets your custom UserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder());   // Sets the password encoder
        return authProvider;
    }

    /**
     * Provides a BCryptPasswordEncoder bean for password hashing.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the AuthenticationManager bean, which is needed for authentication (e.g., in a login endpoint).
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
