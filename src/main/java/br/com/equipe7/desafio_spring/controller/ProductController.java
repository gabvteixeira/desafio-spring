package br.com.equipe7.desafio_spring.controller;

import br.com.equipe7.desafio_spring.dto.ProductCreatedDTO;
import br.com.equipe7.desafio_spring.dto.ProductResponseDTO;
import br.com.equipe7.desafio_spring.model.Product;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import br.com.equipe7.desafio_spring.service.IProduct;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class ProductController {
    @Autowired
    private IProduct service;

    @GetMapping("/articles")
    public ResponseEntity<List<ProductResponseDTO>> getAll(@RequestParam Optional<String> category,
                                                @RequestParam Optional<Boolean> freeShipping,
                                                @RequestParam Optional<String> prestige,
                                                @RequestParam Optional<Integer> order) {
            List<ProductResponseDTO> data = service.getAll(category, freeShipping, prestige, order);
            return new ResponseEntity<>(data, HttpStatus.OK);
    }


    @GetMapping("/articles/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable int productId) {
        Product product =  this.service.getProductById(productId);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }
    /**
     * @author Giovanna
     * @param newProduct
     * @return data -> um DTO que filtra pelo productID, name e quantity
     */
    @PostMapping("/insert-articles-request")
    public ResponseEntity<ProductResponseDTO> save(@RequestBody(required = false) ProductCreatedDTO newProduct) {
        ProductResponseDTO data = service.save(newProduct);
        return new ResponseEntity<>(data, HttpStatus.CREATED);
    }
}
