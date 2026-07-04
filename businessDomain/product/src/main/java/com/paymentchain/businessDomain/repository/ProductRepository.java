package com.paymentchain.businessDomain.repository;

import com.paymentchain.businessDomain.entities.Product;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository  extends CrudRepository<Product,Long> {
}
