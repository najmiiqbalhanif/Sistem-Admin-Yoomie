package com.athletezone.web.controller;

import com.athletezone.web.dto.CartItemDTO;
import com.athletezone.web.dto.OrderDTO;
import com.athletezone.web.dto.PaymentItemDTO;
import com.athletezone.web.models.Cart;
import com.athletezone.web.models.Payment;
import com.athletezone.web.models.Product;
import com.athletezone.web.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import com.athletezone.web.dto.ProductDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Controller
public class A_dashboardController {
    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;
    private final CartService cartService;
    private final PaymentService paymentService;

    public A_dashboardController(ProductService productService, OrderService orderService, UserService userService, CartService cartService, PaymentService paymentService) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
        this.cartService = cartService;
        this.paymentService = paymentService;
    }

    @GetMapping("/A_dashboard")
    public String showDashboard(Model model) {
        // Untuk Tampilkan Order
        List<OrderDTO> orders = orderService.getAllOrders(); // Jika semua, buat query tambahan
        model.addAttribute("orders", orders);

        // Untuk Tampilkan Product di Library
        List<ProductDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);

        // Untuk Penampung Edit Product
        Product product = new Product();
        model.addAttribute("product", product);
        return "A_dashboard";
    }

    //Controller untuk Halaman Order
    @GetMapping("/A_dashboard/{orderId}/paymentItems")
    @ResponseBody
    public List<PaymentItemDTO> getPaymentItems(@PathVariable Long orderId) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        return payment.getPaymentItems().stream()
                .map(item -> new PaymentItemDTO(
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getSubTotal()
                ))
                .toList();
    }

    @PutMapping("/A_dashboard/updateStatus/{orderId}")
    public ResponseEntity<String> updateStatus(@PathVariable Long orderId, @RequestBody Map<String, String> payload) {
        String newStatus = payload.get("status");

        if (!"paid".equals(newStatus) && !"on process".equals(newStatus)) {
            return ResponseEntity.badRequest().body("Invalid status value");
        }

        orderService.updateOrderStatus(orderId, newStatus);
        return ResponseEntity.ok("Status updated successfully");
    }

    // Controller untuk Halaman Library
    @PostMapping("/A_dashboard/addProd")
    public String addProduct(@ModelAttribute("product") Product product, @RequestParam("photo") MultipartFile file, Model model) {
        try {
            // Simpan file dan dapatkan path-nya
            String filePath = productService.saveFile(file);
            // Set path ke objek Product
            product.setPhotoUrl(filePath);

            productService.saveProduct(product);
            model.addAttribute("successMessage", "Product successfully added!");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to add product. Please try again.");
        }

        // Redirect kembali ke halaman library
        return "redirect:/A_dashboard";
    }

    @GetMapping("/A_dashboard/getProd/{id}")
    public ProductDTO getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping("/A_dashboard/editProd/{id}")
    public String editProduct(
            @PathVariable Long id,
            @ModelAttribute ProductDTO productDTO,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            Model model) {
        try {
            // Periksa apakah ada file foto baru
            if (photo != null && !photo.isEmpty()) {
                // Simpan file baru dan dapatkan path-nya
                String filePath = productService.saveFile(photo);
                productDTO.setPhotoUrl(filePath); // Update URL foto pada DTO
            }

            // Update data produk
            productService.editProductById(id, productDTO);

            model.addAttribute("successMessage", "Product successfully updated!");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to update product. Please try again.");
        }

        // Redirect kembali ke halaman library
        return "redirect:/A_dashboard";
    }

    @PostMapping("/A_dashboard/delProd/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);

        return "redirect:/A_dashboard";
    }
}