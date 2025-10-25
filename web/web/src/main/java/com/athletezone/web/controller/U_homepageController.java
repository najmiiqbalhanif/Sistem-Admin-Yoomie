package com.athletezone.web.controller;

import com.athletezone.web.dto.ProductDTO;
import com.athletezone.web.services.ProductService;
import org.springframework.ui.Model;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class U_homepageController {
    private final ProductService productService;
    public U_homepageController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/U_homepage")
    public String homepage(HttpSession session, Model model) {
        // Periksa apakah id user ada di session
        Long userIdLong = (Long) session.getAttribute("userId");
        if (userIdLong != null) {
            // Konversi dari Long ke Integer
            Integer userId = userIdLong.intValue();

            // Kirim id user ke view
            model.addAttribute("userId", userId);

            // Ambil semua produk dan batasi hanya 3 produk
            List<ProductDTO> products = productService.getAllProducts();
            List<ProductDTO> limitedProducts = products.stream().limit(3).toList();

            model.addAttribute("limitedProducts", limitedProducts); // Kirim data ke view
            return "U_homepage"; // Tampilkan halaman
        }
        return "redirect:/login"; // Redirect ke login jika session tidak valid
    }
}