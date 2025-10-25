package com.athletezone.web.controllerREST;

import com.athletezone.web.dto.ProductDTO;
import com.athletezone.web.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/productpage")
public class productpageControllerREST {

    @Autowired
    private ProductService productService;

    // Get Product by ID (Untuk halaman detail produk di Flutter)
    @GetMapping("/get/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
}