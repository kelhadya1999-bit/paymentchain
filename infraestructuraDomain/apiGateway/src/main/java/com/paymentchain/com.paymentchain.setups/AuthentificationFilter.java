
package com.paymentchain.com.paymentchain.setups;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class AuthentificationFilter
        extends AbstractGatewayFilterFactory<AuthentificationFilter.Config> {

    private final WebClient webClient;

    public AuthentificationFilter(WebClient.Builder webClientBuilder) {
        super(Config.class);
        this.webClient = webClientBuilder.build();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new OrderedGatewayFilter((exchange, chain) -> {

            String authHeader = exchange.getRequest().getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")
                    || authHeader.substring(7).isBlank()) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header"));
            }

            return webClient.get()
                    .uri("http://KEYCLOAK-ADAPTER/keycloak/auth/roles")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                    .defaultIfEmpty(List.of())
                    // los errores se convierten ANTES de continuar con la petición
                    .onErrorMap(WebClientResponseException.class, e ->
                            new ResponseStatusException(
                                    e.getStatusCode().is4xxClientError()
                                            ? HttpStatus.UNAUTHORIZED
                                            : HttpStatus.SERVICE_UNAVAILABLE,
                                    "Token validation failed", e))
                    .onErrorMap(e -> !(e instanceof ResponseStatusException), e ->
                            new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                                    "Error communicating with Keycloak Adapter", e))
                    .flatMap(roles -> roles.contains("Partners")
                            ? chain.filter(exchange)
                            : Mono.<Void>error(new ResponseStatusException(
                            HttpStatus.FORBIDDEN, "User does not have Partners role")));
        }, 1);
    }

    public static class Config { }
}