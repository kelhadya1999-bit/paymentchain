package com.paymentchain.customer.controller;

import com.paymentchain.customer.Repository.CustomerRepository;
import com.paymentchain.customer.businessTransaction.BusinessRuleException;
import com.paymentchain.customer.businessTransaction.BusinessTransaction;
import com.paymentchain.customer.entities.Customer;
import com.paymentchain.customer.entities.CustomerProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/customer/V1")
public class CustomerRestController {

    private final CustomerRepository customerRepository;
    @Autowired
    BusinessTransaction businessTransaction;
    private final Environment env;


    // Un solo constructor: Spring inyecta todo sin necesidad de @Autowired
    public CustomerRestController(CustomerRepository customerRepository,
                                  Environment env) {
        this.customerRepository = customerRepository;
        this.env = env;
    }

    @GetMapping
    public List<Customer> FindAll() {
        return (List<Customer>) customerRepository.findAll();
    }

    @GetMapping("/check")
    public String check() {
        return "app=" + env.getProperty("spring.application.name")
                + ", profile=" + env.getProperty("custom.activeProfileName")
                + ", port=" + env.getProperty("server.port");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> FindOne(@PathVariable(name = "id") long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            return ResponseEntity.ok().body(customer.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable(name = "id") long id, @RequestBody Customer input) {
        Optional<Customer> customer1 = customerRepository.findById(id);
        if (customer1.isPresent()) {
            Customer newCustomer = customer1.get();
            newCustomer.setFirstName(input.getFirstName());
            newCustomer.setPhone(input.getPhone());
            Customer save = customerRepository.save(newCustomer);
            return ResponseEntity.ok(save);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se ha encontrado ningún cliente con el id: " + id);
        }
    }

    @PostMapping
    public ResponseEntity<?> postCostumer(@RequestBody Customer input) throws BusinessRuleException, UnknownHostException {
        Customer post =businessTransaction.post(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable(name = "id") long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            customerRepository.deleteById(id);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/full")
    public Customer getById(@RequestParam(name = "id") long id) throws UnknownHostException {
        // Traigo el cliente de la BD por su id
         Customer customer =  businessTransaction.get(id);
        return customer;
    }
}