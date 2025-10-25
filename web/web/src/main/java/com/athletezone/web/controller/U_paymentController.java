package com.athletezone.web.controller;

import com.athletezone.web.dto.PaymentDTO;
import com.athletezone.web.models.*;
import com.athletezone.web.services.CartService;
import com.athletezone.web.services.OrderService;
import com.athletezone.web.services.PaymentService;
import com.athletezone.web.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
public class U_paymentController {
    private final CartService cartService;
    private final UserService userService;
    private final PaymentService paymentService;
    private final OrderService orderService;

    public U_paymentController(CartService cartService, UserService userService, PaymentService paymentService, OrderService orderService) {
        this.cartService = cartService;
        this.userService = userService;
        this.paymentService = paymentService;
        this.orderService = orderService;
    }

    @GetMapping("/U_payment")
    public String showPayment(Model model, HttpSession session) {
        // Periksa apakah id user ada di session
        Long userIdLong = (Long) session.getAttribute("userId");
        if (userIdLong != null) {
            // Konversi dari Long ke Integer
            Integer userId = userIdLong.intValue();
            Cart cart = cartService.getCartByUserId(Long.valueOf(userId));
            User user = userService.getUserById(userIdLong);
            // Kirim id user ke view
            model.addAttribute("userId", userId);
            model.addAttribute("user", user);
            model.addAttribute("cart", cart);
            model.addAttribute("cartItems", cart.getCartItems());

            // Untuk Penampung Edit Product
            PaymentDTO paymentDTO = new PaymentDTO();
            model.addAttribute("paymentDTO", paymentDTO);
            return "U_payment";
        }
        return "redirect:/login"; // Redirect ke login jika session tidak valid
    }

    @PostMapping("/U_payment/createPayment")
    public String createPayment(@ModelAttribute PaymentDTO paymentDTO, RedirectAttributes redirectAttributes) {
        // Cari user berdasarkan ID
        User user = userService.getUserById(paymentDTO.getUserId());

        // Buat entitas Payment
        Payment payment = new Payment();
        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        payment.setAddress(paymentDTO.getAddress());
        payment.setTotalAmount(paymentDTO.getTotalAmount());
        payment.setUser(user);

        // Ambil semua cart items milik user
        List<CartItem> cartItems = cartService.getCartItemsByUserId(user.getId());
        List<PaymentItem> paymentItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            PaymentItem paymentItem = new PaymentItem();
            paymentItem.setProductName(cartItem.getProduct().getName());
            paymentItem.setQuantity(cartItem.getQuantity());
            paymentItem.setPrice(cartItem.getProduct().getPrice());
            paymentItem.setSubTotal(cartItem.getSubTotal());
            paymentItem.setPayment(payment);
            paymentItems.add(paymentItem);
        }

        // Simpan data payment beserta items
        payment.setPaymentItems(paymentItems);
        paymentService.savePayment(payment);

        // Buat entitas Order
        Order order = new Order();
        order.setUser(payment.getUser());
        order.setPayment(payment);

        // Simpan Order
        orderService.saveOrder(order);

        cartService.clearCart(user.getId());

        redirectAttributes.addFlashAttribute("paymentSuccess", "Payment successfully processed!");

        return "redirect:/U_payment";
    }
}