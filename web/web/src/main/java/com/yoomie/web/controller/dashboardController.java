package com.yoomie.web.controller;

import com.yoomie.web.dto.TransactionDTO;
import com.yoomie.web.dto.PaymentItemDTO;
import com.yoomie.web.models.Payment;
import com.yoomie.web.models.Product;
import com.yoomie.web.services.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import com.yoomie.web.dto.ProductDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
public class dashboardController {
    private final ProductService productService;
    private final TransactionService transactionService;
    private final CashierService cashierService;
    private final CartService cartService;
    private final PaymentService paymentService;

    public dashboardController(ProductService productService, TransactionService transactionService, CashierService cashierService, CartService cartService, PaymentService paymentService) {
        this.productService = productService;
        this.transactionService = transactionService;
        this.cashierService = cashierService;
        this.cartService = cartService;
        this.paymentService = paymentService;
    }

    @GetMapping("/A_dashboard")
    public String showDashboard(HttpSession session, Model model) {
        // AMBIL ADMIN ID DARI SESSION
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            // BELUM LOGIN → ARAHKAN KE LOGIN
            return "redirect:/login";
        }

        // Untuk Tampilkan Transaction
        List<TransactionDTO> transactions = transactionService.getAllTransactions(); // Jika semua, buat query tambahan
        model.addAttribute("transactions", transactions);

        // ----------------------------
        // HITUNG SALES & TRANSACTION HARI INI
        // ----------------------------
        LocalDate today = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

        double todaySales = transactions.stream()
                .filter(t -> {
                    String createdOnStr = t.getCreatedOn(); // contoh: "2025-11-21T10:15:30"
                    if (createdOnStr == null || createdOnStr.length() < 10) return false;
                    LocalDate txDate = LocalDate.parse(createdOnStr.substring(0, 10), dateFormatter);
                    return txDate.equals(today);
                })
                .mapToDouble(TransactionDTO::getTotalAmount)
                .sum();

        long todayTransactionCount = transactions.stream()
                .filter(t -> {
                    String createdOnStr = t.getCreatedOn();
                    if (createdOnStr == null || createdOnStr.length() < 10) return false;
                    LocalDate txDate = LocalDate.parse(createdOnStr.substring(0, 10), dateFormatter);
                    return txDate.equals(today);
                })
                .count();

        model.addAttribute("todaySales", todaySales);
        model.addAttribute("todayTransactionCount", todayTransactionCount);

        // Untuk Tampilkan Product di Library
        List<ProductDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);

        // Untuk Penampung Edit Product
        Product product = new Product();
        model.addAttribute("product", product);
        return "A_dashboard";
    }

    //Controller untuk Halaman Transaction
    @GetMapping("/A_dashboard/{transactionId}/paymentItems")
    @ResponseBody
    public List<PaymentItemDTO> getPaymentItems(@PathVariable Long transactionId) {
        Payment payment = paymentService.getPaymentByTransactionId(transactionId);
        return payment.getPaymentItems().stream()
                .map(item -> new PaymentItemDTO(
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getSubTotal()
                ))
                .toList();
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