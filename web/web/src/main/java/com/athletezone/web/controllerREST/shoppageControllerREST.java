package com.athletezone.web.controllerREST;

import com.athletezone.web.dto.ProductDTO;
import com.athletezone.web.services.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/shoppage") // Prefix endpoint REST
public class shoppageControllerREST {

    private final ProductService productService;

    public shoppageControllerREST(ProductService productService) {
        this.productService = productService;
    }

    // Endpoint untuk mendapatkan semua produk
    @GetMapping("/get")
    public List<ProductDTO> getAllProducts() {
        return productService.getAllProducts();
    }
}
