package com.cnpm.bookingflight.config;

import java.util.Collection;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String tokenType = jwt.getClaimAsString("token_type");
        if (!"access".equals(tokenType)) {
            throw new RuntimeException("Token không hợp lệ: không phải access token");
        }
        Collection authorities = grantedAuthoritiesConverter.convert(jwt);
        return new JwtAuthenticationToken(jwt, authorities);
    }
}
