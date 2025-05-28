package com.cnpm.bookingflight.config;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.cnpm.bookingflight.Utils.SecurityUtil;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;

import lombok.Data;

import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
@Data
public class SecurityConfig {
    // biến môi trường
    @Value("${projectjava.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;
    @Value("${projectjava.jwt.access-token-validity-in-seconds}")
    private long accessTokenExpiration;
    @Value("${projectjava.jwt.base64-secret}")
    private String jwtKey;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            CustomAuthenticationEntryPoint cusAuthEntryPoint) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/auth/confirm", "/auth/login", "/auth/register", "/auth/refresh")
                        .permitAll() // Đảm bảo
                        // /login
                        // .anyRequest().authenticated()) // Các route khác yêu cầu xác thực
                        .anyRequest().permitAll()) // Tạm thời cho phép tất cả các route để kiểm tra
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(cusAuthEntryPoint))

                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()) // 401
                        .accessDeniedHandler(new BearerTokenAccessDeniedHandler())) // 403

                .formLogin(f -> f.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    // thuật toán mã hóa password
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Dùng để tạo (sign) JWT token bằng SecretKey
    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    // Dùng để giải mã và xác thực token khi nhận từ client.
    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                getSecretKey()).macAlgorithm(SecurityUtil.JWT_ALGORITHM).build();
        return token -> {
            try {
                return jwtDecoder.decode(token);
            } catch (Exception e) {
                System.out.println(">>> JWT error: " + e.getMessage());
                throw e;
            }
        };
    }

    // getSecretKey: bằng base64 cơ sở sử dụng cho jwtKey
    public SecretKey getSecretKey() {
        byte[] keyBytes = Base64.from(jwtKey).decode();
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, SecurityUtil.JWT_ALGORITHM.getName());
    }

    // @Bean
    // public JwtAuthenticationConverter jwtAuthenticationConverter() {
    // JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new
    // JwtGrantedAuthoritiesConverter();
    // grantedAuthoritiesConverter.setAuthorityPrefix(""); // Remove the ROLE_
    // prefix
    // grantedAuthoritiesConverter.setAuthoritiesClaimName("projectjava");

    // JwtAuthenticationConverter jwtAuthenticationConverter = new
    // JwtAuthenticationConverter();
    // jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
    // return jwtAuthenticationConverter;
    // }
}
