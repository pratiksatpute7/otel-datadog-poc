package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import com.example.demo.tracing.CustomSpanHelper;
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
    private final CustomSpanHelper customSpanHelper;

    public List<Product> getAllProducts() {
        return customSpanHelper.inSpan("product.service.get_all", span -> {
            log.info("Fetching all products");
            customSpanHelper.setAttribute(span, "product.operation", "get_all");
            List<Product> products = productRepository.findAll();
            customSpanHelper.setAttribute(span, "product.count", products.size());
            return products;
        });
    }

    public Optional<Product> getProductById(Long id) {
        return customSpanHelper.inSpan("product.service.get_by_id", span -> {
            log.info("Fetching product with id: {}", id);
            customSpanHelper.setAttribute(span, "product.operation", "get_by_id");
            customSpanHelper.setAttribute(span, "product.id", id);
            Optional<Product> product = productRepository.findById(id);
            if (product.isEmpty()) {
                span.addEvent("product.not_found");
            }
            return product;
        });
    }

    @Transactional
    public Product createProduct(Product product) {
        return customSpanHelper.inSpan("product.service.create", span -> {
            log.info("Creating new product: {}", product.getName());
            customSpanHelper.setAttribute(span, "product.operation", "create");
            customSpanHelper.setAttribute(span, "product.name", product.getName());
            Product savedProduct = productRepository.save(product);
            customSpanHelper.setAttribute(span, "product.id", savedProduct.getId());
            log.info("Product created with id: {}", savedProduct.getId());
            return savedProduct;
        });
    }

    @Transactional
    public Optional<Product> updateProduct(Long id, Product productDetails) {
        return customSpanHelper.inSpan("product.service.update", span -> {
            log.info("Updating product with id: {}", id);
            customSpanHelper.setAttribute(span, "product.operation", "update");
            customSpanHelper.setAttribute(span, "product.id", id);
            Optional<Product> updatedProduct = productRepository.findById(id).map(product -> {
                product.setName(productDetails.getName());
                product.setPrice(productDetails.getPrice());
                product.setDescription(productDetails.getDescription());
                Product updated = productRepository.save(product);
                log.info("Product updated: {}", id);
                return updated;
            });
            if (updatedProduct.isEmpty()) {
                span.addEvent("product.not_found_for_update");
            }
            return updatedProduct;
        });
    }

    @Transactional
    public boolean deleteProduct(Long id) {
        return customSpanHelper.inSpan("product.service.delete", span -> {
            log.info("Deleting product with id: {}", id);
            customSpanHelper.setAttribute(span, "product.operation", "delete");
            customSpanHelper.setAttribute(span, "product.id", id);
            Optional<Product> product = productRepository.findById(id);
            if (product.isPresent()) {
                productRepository.deleteById(id);
                log.info("Product deleted: {}", id);
                return true;
            }
            span.addEvent("product.not_found_for_delete");
            log.warn("Product not found for deletion: {}", id);
            return false;
        });
    }
}
