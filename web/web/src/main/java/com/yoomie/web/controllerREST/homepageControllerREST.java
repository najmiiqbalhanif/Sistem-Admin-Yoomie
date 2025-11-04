package com.yoomie.web.controllerREST;

import com.yoomie.web.dto.ProductDTO;
import com.yoomie.web.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class homepageControllerREST {

    private final ProductService productService;

    @Autowired
    public homepageControllerREST(ProductService productService) {
        this.productService = productService;
    }

    // Endpoint untuk mendapatkan 3 produk pertama (misalnya untuk homepage)
    @GetMapping("/homepage-products")
    public List<ProductDTO> getHomepageProducts() {
        List<ProductDTO> products = productService.getAllProducts();
        return products.stream().limit(3).collect(Collectors.toList());
    }

    // (Optional) Endpoint lain untuk mendapatkan semua produk jika diperlukan
    @GetMapping("/all-products")
    public List<ProductDTO> getAllProducts() {
        return productService.getAllProducts();
    }
}
