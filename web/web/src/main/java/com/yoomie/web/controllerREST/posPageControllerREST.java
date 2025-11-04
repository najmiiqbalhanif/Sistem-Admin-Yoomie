package com.yoomie.web.controllerREST;

import com.yoomie.web.dto.ProductDTO;
import com.yoomie.web.services.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pospage") // Prefix endpoint REST
public class posPageControllerREST {

    private final ProductService productService;

    public posPageControllerREST(ProductService productService) {
        this.productService = productService;
    }

    // Endpoint untuk mendapatkan semua produk
    @GetMapping("/get")
    public List<ProductDTO> getAllProducts() {
        return productService.getAllProducts();
    }
}
