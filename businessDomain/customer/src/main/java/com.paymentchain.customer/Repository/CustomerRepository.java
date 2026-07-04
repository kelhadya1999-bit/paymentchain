package com.paymentchain.customer.Repository;

import com.paymentchain.customer.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.RequestParam;
public interface CustomerRepository extends CrudRepository<Customer, Long> {
//public interface CustomerRepository extends JpaRepository<Customer, Long> {
    //bucar el cliente por el codigo
    @Query("SELECT c FROM Customer c WHERE c.code = ?1")
    public Customer getByCode(@RequestParam String code);

    @Query("SELECT c FROM Customer c WHERE c.iban = ?1")
    public Customer findByIBAN(@RequestParam String iban);

}
