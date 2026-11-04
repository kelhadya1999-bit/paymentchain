package com.paymentchain.customer.businessTransaction;

import com.paymentchain.customer.entities.TransactionDTO;
import com.paymentchain.customer.entities.productDTO;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

// @Component: sin esta anotación Spring no crea el bean y no se puede inyectar en el controller
@Component
public class BusinessTransaction {

    private final WebClient.Builder webClientBuilder; // el bean @LoadBalanced de CustomerApplication

    public BusinessTransaction(@Qualifier("getWebClientBuilder") WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    // Configuración del timeout
    private final HttpClient client = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .responseTimeout(Duration.ofSeconds(5))
            .doOnConnected(conn -> {
                conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                conn.addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            });

    // public: el controller es otra clase y tiene que poder llamarlos
    public String getProductName(long id) {
        // clone(): el builder es un singleton compartido, no se debe modificar directamente
        WebClient build = webClientBuilder.clone()
                .clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://BUISNESSDOMAIN-PRODUCT/Product")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        productDTO product = build.get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(productDTO.class)
                .onErrorResume(e -> {
                    System.out.println("Error llamando a Product " + id + ": " + e.getMessage());
                    return Mono.empty();
                })
                .block();

        return product != null ? product.getName() : null;
    }

    public List<TransactionDTO> getTransactions(String iban) {
        WebClient build = webClientBuilder.clone()
                .clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://BUISNESSDOMAIN-TRANSACTION/transaction")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        return build.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/customer/transactions")
                        .queryParam("ibanAccount", iban)
                        .build())
                .retrieve()
                .bodyToFlux(TransactionDTO.class)
                .collectList()
                .block();
    }
}
