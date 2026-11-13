package com.paymentchain.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class KeycloakRestService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${keycloak.token-uri}")
    private String keycloakTokenUri;
    @Value("${keycloak.user-info-uri}")
    private String keycloakUserInfo;
    @Value("${keycloak.logout}")
    private String keycloakLogout;
    @Value("${keycloak.client-id}")
    private String clientId;
    @Value("${keycloak.client-secret}")
    private String clientSecret;
    @Value("${keycloak.scope}")
    private String scope;
    @Value("${keycloak.authorization-grant-type}")
    private String grantType;
    @Value("${keycloak.authorization-grant-type-refresh}")
    private String grantTypeRefresh;

    private HttpHeaders formHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    public String login(String username, String password) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", username);
        map.add("password", password);
        map.add("grant_type", grantType);
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("scope", scope);
        return restTemplate.postForObject(keycloakTokenUri,
                new HttpEntity<>(map, formHeaders()), String.class);
    }

    public String checkValidity(String token) {
        return getUserInfo(token);
    }

    public void logout(String refreshToken) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("refresh_token", refreshToken);
        restTemplate.postForObject(keycloakLogout,
                new HttpEntity<>(map, formHeaders()), String.class);
    }

    public List<String> getRoles(String token) {
        getUserInfo(token);   // Keycloak valida el token; si no es válido, lanza 401

        String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;
        DecodedJWT decoded = JWT.decode(jwt);

        Set<String> roles = new LinkedHashSet<>();

        Claim realmAccess = decoded.getClaim("realm_access");
        if (!realmAccess.isMissing() && !realmAccess.isNull()) {
            Map<String, Object> map = realmAccess.asMap();
            if (map != null && map.get("roles") instanceof List<?> list) {
                list.forEach(r -> roles.add(String.valueOf(r)));
            }
        }

        // por si algún día añades el mapper "roles"
        List<String> direct = decoded.getClaim("roles").asList(String.class);
        if (direct != null) roles.addAll(direct);

        return new ArrayList<>(roles);
    }

    public String getUserInfo(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, token);   // el token debe llegar como "Bearer xxxx"
        return restTemplate.postForObject(keycloakUserInfo,
                new HttpEntity<>(null, headers), String.class);
    }

    public String refresh(String refreshToken) {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("grant_type", grantTypeRefresh);
        map.add("refresh_token", refreshToken);
        return restTemplate.postForObject(keycloakTokenUri,
                new HttpEntity<>(map, formHeaders()), String.class);
    }
}