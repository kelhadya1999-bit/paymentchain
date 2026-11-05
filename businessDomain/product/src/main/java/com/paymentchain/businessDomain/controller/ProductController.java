package com.paymentchain.businessDomain.controller;

import com.paymentchain.businessDomain.entities.Product;
import com.paymentchain.businessDomain.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/Product")
public class ProductController {
    //inyección por repositorio, utilizar métodos JPA con el objeto
    @Autowired
    ProductRepository productRepository;
    //obtener todos los productos
    @GetMapping
    public List<Product> findAll(){
        return (List<Product>) productRepository.findAll();
    }
    //obtener producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable (name="id") long id){
        Optional<Product> OptionalProduct = productRepository.findById(id);
        if(OptionalProduct.isPresent()){
            return  ResponseEntity.ok(OptionalProduct);
        }else{
            return  ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    //modificar el producto
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct (@PathVariable (name="id") long id, @RequestBody Product product){
        Optional<Product> OptionalProduct = productRepository.findById(id);
        if(OptionalProduct.isPresent()){
            product.setId(id);
            product.setName(product.getName());
            productRepository.save(product);
            return new ResponseEntity<>(HttpStatus.OK);
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se ha encontrado ningun producto con el id: "+id);
        }
    }

    //insertar
    @PostMapping
    public ResponseEntity<?> addProduct( @RequestBody Product product){
        Product save = productRepository.save(product);
        return new ResponseEntity<>(save, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable (name="id") long id){
        Optional<Product> OptionalProduct = productRepository.findById(id);
        if(OptionalProduct.isPresent()){
            productRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se ha encontrado ningun producto con el id: "+id);
        }
    }
}
