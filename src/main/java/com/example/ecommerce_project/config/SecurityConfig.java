package com.example.ecommerce_project.config;

import com.example.ecommerce_project.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 1. ყოველთვის დაუშვი Preflight (OPTIONS) მოთხოვნები
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 🎯 დაემატა: მთავარი Root, static და Error გვერდები
                        .requestMatchers("/", "/error", "/index.html", "/static/**", "/favicon.ico").permitAll()

                        // 2. Auth & Public ენდპოინტები
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/verify-2fa",
                                "/api/auth/setup-2fa",
                                "/api/auth/enable-2fa",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        .requestMatchers("/api/auth/change-password").authenticated()

                        // 3. Cart & Orders ენდპოინტები
                        .requestMatchers("/api/cart/**").permitAll()
                        .requestMatchers("/api/orders/**").permitAll()

                        // 4. Products & Reviews (GET)
                        .requestMatchers(HttpMethod.GET,
                                "/api/product/**",
                                "/api/products/**",
                                "/api/reviews/**"
                        ).permitAll()

                        // 5. ADMIN ენდპოინტები
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/product/**", "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/product/**", "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/product/**", "/api/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/product/**", "/api/products/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // 🎯 დაემატა ახალი დომენები (3dstudio.ge)
        corsConfig.setAllowedOrigins(List.of(
                "https://3dstudio.ge",
                "https://www.3dstudio.ge",
                "http://3dstudio.ge",
                "http://www.3dstudio.ge",
                "https://khoshtoshopp.netlify.app",
                "http://localhost:4200",
                "http://localhost:8090"
        ));

        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        corsConfig.setExposedHeaders(List.of("Authorization", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        corsConfig.setAllowCredentials(true);
        corsConfig.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new AuthenticationProvider() {
            @Override
            public org.springframework.security.core.Authentication authenticate(
                    org.springframework.security.core.Authentication authentication
            ) throws org.springframework.security.core.AuthenticationException {

                String username = authentication.getName();
                String password = authentication.getCredentials().toString();

                var userDetails = userDetailsService.loadUserByUsername(username);

                if (passwordEncoder.matches(password, userDetails.getPassword())) {
                    return new UsernamePasswordAuthenticationToken(
                            userDetails,
                            password,
                            userDetails.getAuthorities()
                    );
                }
                throw new org.springframework.security.authentication.BadCredentialsException("არასწორი პაროლი!");
            }

            @Override
            public boolean supports(Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        };
    }
}