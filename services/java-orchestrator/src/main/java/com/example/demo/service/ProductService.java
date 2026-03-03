package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        log.info("Fetching product with id: {}", id);
        return productRepository.findById(id);
    }

    @Transactional
    public Product createProduct(Product product) {
        log.info("Creating new product: {}", product.getName());
        Product savedProduct = productRepository.save(product);
        log.info("Product created with id: {}", savedProduct.getId());
        return savedProduct;
    }

    @Transactional
    public Optional<Product> updateProduct(Long id, Product productDetails) {
        log.info("Updating product with id: {}", id);
        return productRepository.findById(id).map(product -> {
            product.setName(productDetails.getName());
            product.setPrice(productDetails.getPrice());
            product.setDescription(productDetails.getDescription());
            Product updated = productRepository.save(product);
            log.info("Product updated: {}", id);
            return updated;
        });
    }

    @Transactional
    public boolean deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            productRepository.deleteById(id);
            log.info("Product deleted: {}", id);
            return true;
        }
        log.warn("Product not found for deletion: {}", id);
        return false;
    }
}
