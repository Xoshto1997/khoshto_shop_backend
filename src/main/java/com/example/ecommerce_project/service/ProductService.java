package com.example.ecommerce_project.service;

import com.example.ecommerce_project.model.Product;
import com.example.ecommerce_project.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;

    public Product add(Product product) {
        return productRepository.save(product);
    }


    public void delete(Long prodId) {
        productRepository.deleteById(prodId);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("პროდუქტი ID-ით: " + id + " ვერ მოიძებნა!"));
    }

    public Product saveProductWithImage(String name, Double price, String description, MultipartFile file) throws IOException {
        Product product = new Product();
        product.setProductName(name);
        product.setPrice(price);
        product.setDescription(description);

        if (file != null && !file.isEmpty()) {
            product.setImageData(file.getBytes());
        }

        return productRepository.save(product);
    }
}
