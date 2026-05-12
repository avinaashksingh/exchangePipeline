package com.scetzhbook.exchangePipeline.security;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;

@Configuration
public class JwtKeyConfig {

    @Bean
    public JwtEncoder jwtEncoder() throws Exception {
        ClassPathResource publicKeyPem = new ClassPathResource("keys/demo-public.pem");
        ClassPathResource privateKeyPem = new ClassPathResource("keys/demo-private.pem");

        RSAPublicKey publicKey;
        try (var in = publicKeyPem.getInputStream()) {
            publicKey = (RSAPublicKey) RsaKeyConverters.x509().convert(in);
        }

        RSAPrivateKey privateKey;
        try (var in = privateKeyPem.getInputStream()) {
            privateKey = (RSAPrivateKey) RsaKeyConverters.pkcs8().convert(in);
        }

        JWK jwk = new RSAKey.Builder(publicKey).privateKey(privateKey).keyID("demo-key").build();
        ImmutableJWKSet<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }
}

