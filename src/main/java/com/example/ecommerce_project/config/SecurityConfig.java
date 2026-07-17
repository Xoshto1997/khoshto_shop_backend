    package com.example.ecommerce_project.config;

    import com.example.ecommerce_project.security.JwtAuthenticationFilter;
    import lombok.RequiredArgsConstructor;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.http.HttpMethod;
    import org.springframework.security.authentication.AuthenticationManager;
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
                    .cors(cors -> cors.configurationSource(request -> {
                        var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                        corsConfig.setAllowedOriginPatterns(java.util.List.of("*"));
                        corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                        corsConfig.setAllowedHeaders(java.util.List.of("*"));
                        corsConfig.setAllowCredentials(true);
                        return corsConfig;
                    }))
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .requestMatchers("/api/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                            .requestMatchers(HttpMethod.GET, "/api/product", "/api/product/", "/api/product/**").permitAll()

                            .requestMatchers(HttpMethod.POST, "/api/product/**").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.DELETE, "/api/product/**").hasRole("ADMIN")
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