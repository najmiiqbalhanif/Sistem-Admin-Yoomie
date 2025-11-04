package com.yoomie.web.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import com.yoomie.web.models.Cart;
import com.yoomie.web.services.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class U_cartController {
    private final CartService cartService;

    public U_cartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/U_cart")
        public String viewCart(Model model, HttpSession session) {
        // Periksa apakah id user ada di session
        Long userIdLong = (Long) session.getAttribute("userId");
        if (userIdLong != null) {
            // Konversi dari Long ke Integer
            Integer userId = userIdLong.intValue();
            Cart cart = cartService.getCartByUserId(Long.valueOf(userId));
            // Kirim id user ke view
            model.addAttribute("userId", userId);
            model.addAttribute("cart", cart);
            model.addAttribute("cartItems", cart.getCartItems());
            return "U_cart";
        }
        return "redirect:/login"; // Redirect ke login jika session tidak valid
    }
}

