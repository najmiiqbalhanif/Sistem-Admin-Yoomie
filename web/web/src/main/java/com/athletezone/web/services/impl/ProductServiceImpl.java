package com.athletezone.web.services.impl;

import com.athletezone.web.dto.ProductDTO;
import com.athletezone.web.models.Product;
import com.athletezone.web.repositories.ProductRepository;
import com.athletezone.web.services.ProductService;
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
        // Cari produk berdasarkan ID
        Product Product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Update data produk
        Product.setName(productDTO.getName());
        Product.setBrand(productDTO.getBrand());
        Product.setCategory(productDTO.getCategory());
        Product.setPrice(productDTO.getPrice());
        Product.setStock(productDTO.getStock());

        // Simpan perubahan
        productRepository.save(Product);
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