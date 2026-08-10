package com.example.hotpotrestaurantbooking_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SercurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) //tương dương (csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/auth/**").permitAll()

                        .requestMatchers("/api/payment/vnpay/create").permitAll()
                        .requestMatchers("/api/payment/vnpay-return").permitAll()

                        .requestMatchers("/api/quan-ly-payment/vnpay-return").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/dat-ban-quan-ly/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER")
                        .requestMatchers(HttpMethod.GET, "/api/dat-bans/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF", "ROLE_USER")

                        .requestMatchers("/phanTrangMon", "/phanTrangComBo").permitAll()
                        .requestMatchers("/upload").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/ws-print/**").permitAll()

                        .requestMatchers("/api/quan-ly-payment/**")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_STAFF")


                        .requestMatchers("/sepay/webhook").permitAll()



                        .anyRequest().authenticated()
                )
                // Spring Boot sẽ tự động tìm Bean JwtDecoder trong Context (đã được tạo ở JwtKeyConfig)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter = new JwtGrantedAuthoritiesConverter();
        converter.setAuthorityPrefix(""); // Vì trong Service ta đã gắn "ROLE_" rồi
        converter.setAuthoritiesClaimName("scope"); // Token phải chứa claim "scope"

        JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
        return jwtConverter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}