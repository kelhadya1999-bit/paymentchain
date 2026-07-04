package com.paymentchain.businessDomain.repository;

import com.paymentchain.businessDomain.entities.Transaction;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository  extends CrudRepository<Transaction,Long> {
    //aplica la consulta y guarda en resultado en la lista
    @Query("SELECT t FROM Transaction t WHERE t.ibanAccount = ?1")
    public List<Transaction> findByIbanAccount(String ibanAccount);

}
