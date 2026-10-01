package com.example.ecommerce_project.controller;

import com.example.ecommerce_project.model.Product;
import com.example.ecommerce_project.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<Page<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(productService.getProductsPaginated(page, size));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProductWithImages(
            @PathVariable Long id,
            @RequestParam("productName") String productName,
            @RequestParam("price") Double price,
            @RequestParam("description") String description,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverFile,
            @RequestParam(value = "carouselImages", required = false) List<MultipartFile> carouselFiles
    ) throws IOException {
        Product updatedProduct = productService.updateProduct(id, productName, price, description, coverFile, carouselFiles);
        return ResponseEntity.ok(updatedProduct);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> addProduct(
            @RequestParam("productName") String name,
            @RequestParam("price") Double price,
            @RequestParam("description") String description,
            @RequestParam(value = "coverImage", required = false) MultipartFile coverFile,
            @RequestParam(value = "carouselImages", required = false) List<MultipartFile> carouselFiles
    ) throws IOException {

        Product savedProduct = productService.saveProductWithImages(name, price, description, coverFile, carouselFiles);
        return ResponseEntity.ok(savedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}