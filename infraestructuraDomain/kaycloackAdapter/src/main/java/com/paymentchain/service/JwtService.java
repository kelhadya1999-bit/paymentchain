package com.paymentchain.service;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.security.interfaces.RSAPublicKey;

@Service
public class JwtService {

    @Value("${keycloak.jwk-set-uri}")
    private String jwksUri;

    @Value("${keycloak.certs-id}")
    private String certsId;

    public Jwk getJwk() throws Exception {
        URL url = new URL(jwksUri);
        JwkProvider provider = new UrlJwkProvider(url);

        return provider.get(certsId.trim());
    }
    public boolean isValid(String token) {
        try {
            RSAPublicKey key = (RSAPublicKey) getJwk().getPublicKey();
            JWT.require(Algorithm.RSA256(key, null)).build().verify(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}