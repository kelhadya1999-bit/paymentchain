package com.paymentchain.businessDomain.controller;

import com.paymentchain.businessDomain.entities.Transaction;
import com.paymentchain.businessDomain.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/transaction")
public class TransactionController {
    TransactionRepository transactionRepository;
    //inyección por constructos, es obligatorio para evitar k pete al encontrarse nulos
    public TransactionController(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @GetMapping
    public List<Transaction> list() {
        return (List<Transaction>) transactionRepository.findAll();
    }
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> findById(@PathVariable long id) {
       Optional<Transaction> transaction = transactionRepository.findById(id);
       if (transaction.isPresent()) {
           return new ResponseEntity<>(transaction.get(), HttpStatus.OK);
       }else{
           return new ResponseEntity<>(HttpStatus.NOT_FOUND);
       }
    }
    @GetMapping("/customer/transactions")
    //obtener el campo iban del parametro
    public List<Transaction> getCustomerByIban(@RequestParam (name="ibanAccount")  String ibanAccount) {
        //ejectuta la consulta sql de la inter
        return transactionRepository.findByIbanAccount(ibanAccount);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(@PathVariable (name="id") long id,@RequestBody Transaction input) {
        Transaction transaction = transactionRepository.findById(id).get();
        if (transaction != null) {
            input.setIbanAccount(transaction.getIbanAccount());
            input.setAmount(transaction.getAmount());
            input.setDescription(transaction.getDescription());
            input.setDate(transaction.getDate());
            transactionRepository.save(input);
            return new ResponseEntity<>(HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody Transaction input) {
        Transaction transaction =  transactionRepository.save(input);
        return  ResponseEntity.ok(transaction);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable (name="id") long id) {
        Optional<Transaction> transaction = transactionRepository.findById(id);
        if (transaction.isPresent()) {
            transactionRepository.delete(transaction.get());
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return ResponseEntity.ok().build();
    }

}
