package com.yoomie.web.services.impl;

import com.yoomie.web.dto.ProductDTO;
import com.yoomie.web.models.Product;
import com.yoomie.web.models.ProductStockLog;
import com.yoomie.web.repositories.ProductRepository;
import com.yoomie.web.repositories.ProductStockLogRepository;
import com.yoomie.web.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductStockLogRepository productStockLogRepository;

    private static final String UPLOAD_DIR = "web/web/src/main/resources/static/storage/";
    private static final String BASE_IMAGE_URL = "http://10.0.2.2:8080/";

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              ProductStockLogRepository productStockLogRepository) {
        this.productRepository = productRepository;
        this.productStockLogRepository = productStockLogRepository;
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        return convertToDTO(product);
    }

    @Override
    public String saveFile(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return "storage" + File.separator + fileName;
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void editProductById(Long id, ProductDTO productDTO, Long adminId) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int oldStock = product.getStock();
        int newStock = productDTO.getStock();

        product.setName(productDTO.getName());
        product.setBrand(productDTO.getBrand());
        product.setCategory(productDTO.getCategory());
        product.setPrice(productDTO.getPrice());
        product.setStock(newStock);

        if (productDTO.getPhotoUrl() != null && !productDTO.getPhotoUrl().isEmpty()) {
            product.setPhotoUrl(productDTO.getPhotoUrl());
        }

        productRepository.save(product);

        if (oldStock != newStock) {
            ProductStockLog log = ProductStockLog.builder()
                    .product(product)
                    .oldStock(oldStock)
                    .newStock(newStock)
                    .diff(newStock - oldStock)
                    .adminId(adminId)
                    .build();

            productStockLogRepository.save(log);
        }
    }

    @Override
    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }

    private ProductDTO convertToDTO(Product product) {
        String photoPath = product.getPhotoUrl() != null ? product.getPhotoUrl().replace("\\", "/") : "";
        String fullImageUrl = photoPath.isEmpty() ? "" : BASE_IMAGE_URL + photoPath;

        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .brand(product.getBrand())
                .price(product.getPrice())
                .photoUrl(fullImageUrl)
                .category(product.getCategory())
                .stock(product.getStock())
                .build();
    }
}
