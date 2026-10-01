package com.example.ecommerce_project.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.ecommerce_project.model.Product;
import com.example.ecommerce_project.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final Cloudinary cloudinary;

    public Product add(Product product) {
        return productRepository.save(product);
    }

    public void delete(Long prodId) {
        productRepository.deleteById(prodId);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getProductsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return productRepository.findAll(pageable);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("პროდუქტი ID-ით: " + id + " ვერ მოიძებნა!"));
    }

    public Product updateProduct(
            Long id,
            String productName,
            Double price,
            String description,
            MultipartFile coverFile,
            List<MultipartFile> carouselFiles
    ) throws IOException {
        // 1. იპოვე არსებული პროდუქტი
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("პროდუქტი ვერ მოიძებნა ID-ით: " + id));

        // 2. განაახლე ტექსტური ველები
        product.setProductName(productName);
        product.setPrice(price);
        product.setDescription(description);

        // 3. თუ ახალი ქოვერ ფოტო აიტვირთა, ატვირთე Cloudinary-ზე და განაახლე URL
        if (coverFile != null && !coverFile.isEmpty()) {
            String coverImageUrl = uploadImageToCloudinary(coverFile);
            product.setCoverImage(coverImageUrl);
        }

        // 4. თუ ახალი კარუსელის ფოტოები აიტვირთა, ატვირთე Cloudinary-ზე და ჩაანაცვლე
        if (carouselFiles != null && !carouselFiles.isEmpty()) {
            List<String> carouselUrls = new ArrayList<>();
            int limit = Math.min(carouselFiles.size(), 3);

            for (int i = 0; i < limit; i++) {
                MultipartFile file = carouselFiles.get(i);
                if (file != null && !file.isEmpty()) {
                    String imgUrl = uploadImageToCloudinary(file);
                    if (imgUrl != null) {
                        carouselUrls.add(imgUrl);
                    }
                }
            }
            // მხოლოდ იმ შემთხვევაში ვაახლებთ, თუ რეალურად აიტვირთა ახალი ფოტოები
            if (!carouselUrls.isEmpty()) {
                product.setCarouselImages(carouselUrls);
            }
        }

        // 5. შეინახე ბაზაში
        return productRepository.save(product);
    }

    public Product saveProductWithImages(
            String name,
            Double price,
            String description,
            MultipartFile coverFile,
            List<MultipartFile> carouselFiles
    ) throws IOException {

        Product product = new Product();
        product.setProductName(name);
        product.setPrice(price);
        product.setDescription(description);

        if (coverFile != null && !coverFile.isEmpty()) {
            String coverUrl = uploadImageToCloudinary(coverFile);
            product.setCoverImage(coverUrl);
        }

        if (carouselFiles != null && !carouselFiles.isEmpty()) {
            List<String> carouselUrls = new ArrayList<>();
            int limit = Math.min(carouselFiles.size(), 3);

            for (int i = 0; i < limit; i++) {
                MultipartFile file = carouselFiles.get(i);
                if (file != null && !file.isEmpty()) {
                    String imgUrl = uploadImageToCloudinary(file);
                    if (imgUrl != null) {
                        carouselUrls.add(imgUrl);
                    }
                }
            }
            product.setCarouselImages(carouselUrls);
        }

        return productRepository.save(product);
    }

    public Product saveProductWithImage(String name, Double price, String description, MultipartFile file) throws IOException {
        return saveProductWithImages(name, price, description, file, null);
    }

    private String uploadImageToCloudinary(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap("resource_type", "auto")
        );

        return uploadResult.get("secure_url") != null
                ? uploadResult.get("secure_url").toString()
                : uploadResult.get("url").toString();
    }
}