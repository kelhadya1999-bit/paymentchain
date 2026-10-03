package com.paymentchain.customer.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.paymentchain.customer.Repository.CustomerRepository;
import com.paymentchain.customer.entities.Customer;

import com.paymentchain.customer.entities.CustomerProduct;
import com.paymentchain.customer.entities.TransactionDTO;
import com.paymentchain.customer.entities.productDTO;
import io.netty.channel.ChannelOption;
//import io.netty.handler.codec.quic.EpollQuicUtils;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
//import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/customer")
public class CustomerRestController {


    private final CustomerRepository customerRepository;
    private final WebClient.Builder webClientBuilder;

    private final Environment env;

    public CustomerRestController(CustomerRepository customerRepository,
                                  WebClient.Builder webClientBuilder,
                                  Environment env) {
        this.customerRepository = customerRepository;
        this.webClientBuilder = webClientBuilder;
        this.env = env;
        System.out.println(">>> Constructor CustomerRestController");
    }

    //configuración del time out
    HttpClient client = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .responseTimeout(Duration.ofSeconds(1))
            .doOnConnected(conn -> {
                conn.addHandlerLast( new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS));
                conn.addHandlerLast( new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS));
            });

    //los parámetros de webclient se definen en unaclase própia
    @GetMapping
    public List<Customer> FindAll() {
        return (List<Customer>) customerRepository.findAll();
    }
    //private Environment env;
    @GetMapping("/check")
    public String check(){
        return "app=" + env.getProperty("spring.application.name")
                + ", profile=" + env.getProperty("custom.activeProfileName")
                + ", port=" + env.getProperty("server.port");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> FindOne(@PathVariable long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            return ResponseEntity.ok().body(customer.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable long id, @RequestBody Customer input) {
        Optional<Customer> customer1 = customerRepository.findById(id);
        if (customer1.isPresent()) {
            Customer newCustomer = customer1.get();
            newCustomer.setFirstName(input.getFirstName());
            newCustomer.setPhone(input.getPhone());
            Customer save = customerRepository.save(newCustomer);
            return ResponseEntity.ok(save);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se ha encontrado ningun cliente con el id: "
                    + id);
        }
    }

    @PostMapping
    public ResponseEntity<?> post(@RequestBody Customer customer) {
        //customer.getProducts().forEach(x -> x.setCustomer(customer));
        //Customer save = customerRepository.save(customer);
       // return ResponseEntity.ok(save);
        // Asegurarte de que cada producto apunte al customer
        if (customer.getProducts() != null) {
            customer.getProducts().forEach(p -> p.setCustomer(customer));
        }

        Customer saved = customerRepository.save(customer);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            customerRepository.deleteById(id);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/full")
    public Customer getByCode(@RequestParam String code) {
        //Traigo el cliente de la BD
        Customer customer = customerRepository.getByCode(code);

        if (customer == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Customer with code " + code + " not found"
            );
        }
       // Obtengo los productos del cliente
        List<CustomerProduct> products = customer.getProducts();
        //por cada producto tomo el productID y llamo al miroservicio Product usando webclient
        products.forEach(cp -> {
            String productName = getProductName(cp.getProductId());
            cp.setProductName(productName);
        });

        //obtener las transacciones del cliente
        List<?> transactions = getTransactions(customer.getIban());
        customer.setTransactions(transactions);
        return customer;
    }


    private String getProductName (long id) {
        //crear la url para acceder a los productos del cliente
        WebClient build = webClientBuilder.clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://localhost:8082/Product")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url","http://localhost:8082/Product"))
                .build();

        //poder acceder a la url del producte con su id y devuelva el json con la info
        //La clase productDTO Su único propósito es recibir o enviar datos entre sistemas
        //DTO te permite mapear la respuesta JSON de Product a un objeto Java temporal en memoria, sin tocar la base de datos.
        productDTO product = build.get()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(productDTO.class)
                .block();

        return product != null ? product.getName() : null;
    }
    private List<?> getTransactions(String iban){
        WebClient build = webClientBuilder.clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl("http://localhost:8083/transaction")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url","http://localhost:8083/transaction"))
                .build();


        List<TransactionDTO> transactions = build.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/customer/transactions") // asegúrate del endpoint correcto
                        .queryParam("ibanAccount", iban)
                        .build())
                .retrieve()
                .bodyToFlux(TransactionDTO.class)  // mapear directamente a Transaction
                .collectList()
                .block();

        return transactions;
    }
}