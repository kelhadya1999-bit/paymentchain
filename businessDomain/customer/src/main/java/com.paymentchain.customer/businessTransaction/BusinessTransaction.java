package com.paymentchain.customer.businessTransaction;

import com.paymentchain.customer.Repository.CustomerRepository;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import com.paymentchain.customer.entities.TransactionDTO;
import com.paymentchain.customer.entities.productDTO;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.netty.http.client.HttpClient;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class BusinessTransaction {

    private final WebClient.Builder webClientBuilder; // el bean @LoadBalanced de CustomerApplication
    private final CustomerRepository customerRepository;

    public BusinessTransaction(@Qualifier("getWebClientBuilder") WebClient.Builder webClientBuilder,
                               CustomerRepository customerRepository) {
        this.webClientBuilder = webClientBuilder;
        this.customerRepository = customerRepository;
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

    public Customer get(long id) throws UnknownHostException {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Customer with id " + id + " not found"));

        List<CustomerProduct> products = customer.getProducts();
        if (products != null) {
            for (CustomerProduct cp : products) {
                cp.setProductName(getProductName(cp.getProductId()));
            }
        }

        customer.setTransactions(getTransactions(customer.getIban()));
        return customer;
    }

    /**
     * Devuelve el nombre del producto, o "" si el producto no existe (404).
     * Cualquier otro fallo (Product caído, 500...) se propaga como error técnico.
     */
    public String getProductName(long id) throws UnknownHostException {
        try {
            WebClient build = webClientBuilder.clone()
                    .clientConnector(new ReactorClientHttpConnector(client))
                    .baseUrl("http://BUISNESSDOMAIN-PRODUCT/product/product")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            // IMPORTANTE: aquí NO hay onErrorResume. Si lo hubiera, se tragaría el 404 y el
            // catch de abajo nunca se ejecutaría.
            productDTO product = build.get()
                    .uri("/{id}", id)
                    .retrieve()
                    .bodyToMono(productDTO.class)
                    .block();

            return (product != null && product.getName() != null) ? product.getName() : "";
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                return "";
            }
            throw new UnknownHostException(ex.getMessage());
        }
    }

    public List<TransactionDTO> getTransactions(String iban) {
        WebClient build = webClientBuilder.clone()
                .clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://BUISNESSDOMAIN-TRANSACTION/transaction/transaction")
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

    public Customer post(Customer input) throws BusinessRuleException, UnknownHostException {
        if (input.getProducts() != null) {
            for (CustomerProduct product : input.getProducts()) {
                String productName = getProductName(product.getProductId());
                if (productName.isBlank()) {
                    // 4 argumentos: (id, code, message, status). Con 3 el code quedaba en null.
                    throw new BusinessRuleException(1025, "1025",
                            "Error validación, producto con id " + product.getProductId() + " no existe",
                            HttpStatus.PRECONDITION_FAILED);
                }
                product.setCustomer(input);
            }
        }
        return customerRepository.save(input);
    }
}