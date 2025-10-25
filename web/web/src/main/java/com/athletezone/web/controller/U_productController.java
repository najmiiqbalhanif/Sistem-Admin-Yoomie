package com.athletezone.web.controller;

import com.athletezone.web.dto.ProductDTO;
import com.athletezone.web.services.CartService;
import com.athletezone.web.services.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class U_productController {

    private final ProductService productService;
    private final CartService cartService;

    public U_productController(ProductService productService, CartService cartService) {
        this.productService = productService;
        this.cartService = cartService;
    }

    @GetMapping("/U_product/{id}")
    public String showProductDetails(@PathVariable Long id, Model model, HttpSession session) {
        // Periksa apakah id user ada di session
        Long userIdLong = (Long) session.getAttribute("userId");
        if (userIdLong != null) {
            // Konversi dari Long ke Integer
            Integer userId = userIdLong.intValue();

            // Ambil data produk berdasarkan ID
            ProductDTO product = productService.getProductById(id);

            // Tambahkan produk ke model
            model.addAttribute("product", product);
            //ID user ke view
            model.addAttribute("userId", userId);
            // Return halaman U_product.html
            return "U_product";
        }
        return "redirect:/login"; // Redirect ke login jika session tidak valid
    }

    @PostMapping("/U_product/addToCart")
    public String addToCart(@RequestParam Long userId, @RequestParam Long productId, RedirectAttributes redirectAttributes) {
        try {
            cartService.addToCart(userId, productId);
            redirectAttributes.addFlashAttribute("successMessage", "Product added to cart successfully.");
            return "redirect:/U_cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add product to cart: " + e.getMessage());
            return "redirect:/productPage"; // Ganti dengan halaman sebelumnya
        }
    }

}