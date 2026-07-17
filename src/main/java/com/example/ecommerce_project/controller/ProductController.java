package com.example.ecommerce_project.controller;


import com.example.ecommerce_project.model.Product;
import com.example.ecommerce_project.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@AllArgsConstructor
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/all")
    public List<Product> getProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping("/add")
    public ResponseEntity<Product> addProduct(@RequestBody Product product) {
        Product savedProduct = productService.add(product);
        return ResponseEntity.ok(savedProduct);
    }

    @PostMapping(value = "/add-with-image", consumes = {"multipart/form-data"})
    public ResponseEntity<?> addProduct(
            @RequestParam("productName") String productName,
            @RequestParam("price") Double price,
            @RequestParam("description") String description,
            @RequestParam("image") MultipartFile file) throws IOException {

        productService.saveProductWithImage(productName, price, description, file);

        return ResponseEntity.ok().body(Map.of("message", "პროდუქტი ფოტოსთან ერთად დაემატა!"));
    }

    @DeleteMapping("/delete/{prodId}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long prodId) {
        productService.delete(prodId);
        return ResponseEntity.ok("Product deleted successfully with ID: " + prodId);
    }

}
