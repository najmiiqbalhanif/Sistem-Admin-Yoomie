package com.yoomie.web.services.impl;

import com.yoomie.web.dto.ProductDTO;
import com.yoomie.web.models.Product;
import com.yoomie.web.repositories.ProductRepository;
import com.yoomie.web.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {
    private ProductRepository productRepository;
    private static final String UPLOAD_DIR = "web/web/src/main/resources/static/storage/";

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    // Mendapatkan semua produk dari database
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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

        // Buat folder jika belum ada
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Buat nama unik untuk file
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        // Path lengkap file
        Path filePath = uploadPath.resolve(fileName);
        // Simpan file ke direktori
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        // Return path relatif
        return "storage" + File.separator + fileName;
    }

    @Override
    // Menyimpan produk baru
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void editProductById(Long id, ProductDTO productDTO) throws IOException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(productDTO.getName());
        product.setBrand(productDTO.getBrand());
        product.setCategory(productDTO.getCategory());
        product.setPrice(productDTO.getPrice());
        product.setStock(productDTO.getStock());

        // ✅ Kalau ada photoUrl baru dari controller, update ke entity
        if (productDTO.getPhotoUrl() != null && !productDTO.getPhotoUrl().isEmpty()) {
            // nilai ini adalah "storage/xxx" atau "storage\\xxx" hasil dari saveFile
            product.setPhotoUrl(productDTO.getPhotoUrl());
        }

        productRepository.save(product);
    }

    @Override
    // Menghapus pr7 oduk berdasarkan ID
    public void deleteProductById(Long id) {
        productRepository.deleteById(id);
    }

    private static final String BASE_IMAGE_URL = "http://10.0.2.2:8080/"; // Masih untuk emulator, harusnya sesuaikan dengan backendnya

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