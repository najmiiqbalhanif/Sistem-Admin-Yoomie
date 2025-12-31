package com.yoomie.web.controller;

import com.yoomie.web.dto.TransactionDTO;
import com.yoomie.web.dto.PaymentItemDTO;
import com.yoomie.web.dto.ProductDTO;
import com.yoomie.web.models.Admin;
import com.yoomie.web.models.Payment;
import com.yoomie.web.models.Product;
import com.yoomie.web.models.ProductStockLog;
import com.yoomie.web.repositories.ProductStockLogRepository;
import com.yoomie.web.services.AdminService;
import com.yoomie.web.services.CartService;
import com.yoomie.web.services.CashierService;
import com.yoomie.web.services.PaymentService;
import com.yoomie.web.services.ProductService;
import com.yoomie.web.services.TransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import com.yoomie.web.dto.ItemSummaryDTO;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class dashboardController {

    private final ProductService productService;
    private final TransactionService transactionService;
    private final CashierService cashierService;
    private final CartService cartService;
    private final PaymentService paymentService;
    private final AdminService adminService;
    private final ProductStockLogRepository productStockLogRepository;

    public dashboardController(ProductService productService,
                               TransactionService transactionService,
                               CashierService cashierService,
                               CartService cartService,
                               PaymentService paymentService,
                               AdminService adminService,
                               ProductStockLogRepository productStockLogRepository) {

        this.productService = productService;
        this.transactionService = transactionService;
        this.cashierService = cashierService;
        this.cartService = cartService;
        this.paymentService = paymentService;
        this.adminService = adminService;
        this.productStockLogRepository = productStockLogRepository;
    }

    @GetMapping("/A_dashboard")
    public String showDashboard(HttpSession session, Model model) {
        // AMBIL ADMIN ID DARI SESSION
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            // BELUM LOGIN → ARAHKAN KE LOGIN
            return "redirect:/login";
        }

        Admin admin = adminService.getAdminById(adminId);
        if (admin != null) {
            model.addAttribute("fullName", admin.getFullName());
        }

        // Untuk Tampilkan Transaction
        List<TransactionDTO> transactions = transactionService.getAllTransactions();
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

        // ----------------------------
        // ITEM SUMMARY HARI INI (TOP 5 PRODUK)
        // ----------------------------
        Map<String, Long> itemSoldMap = new HashMap<>();
        Map<String, Double> salesMap = new HashMap<>();

        for (TransactionDTO t : transactions) {
            String createdOnStr = t.getCreatedOn();
            if (createdOnStr == null || createdOnStr.length() < 10) {
                continue;
            }
            LocalDate txDate = LocalDate.parse(createdOnStr.substring(0, 10), dateFormatter);
            if (!txDate.equals(today)) {
                // Bukan transaksi hari ini
                continue;
            }

            // Asumsi TransactionDTO punya getId() yang merupakan transactionId
            Long transactionId = t.getId();
            if (transactionId == null) {
                continue;
            }

            try {
                Payment payment = paymentService.getPaymentByTransactionId(transactionId);
                if (payment == null || payment.getPaymentItems() == null) {
                    continue;
                }

                payment.getPaymentItems().forEach(item -> {
                    String productName = item.getProductName();
                    if (productName == null || productName.isBlank()) {
                        productName = "Unknown Item";
                    }

                    long qty = item.getQuantity();      // jumlah pcs produk di transaksi ini
                    double subTotal = item.getSubTotal(); // subtotal (qty * harga) untuk produk ini di transaksi ini

                    // Akumulasi ke map
                    itemSoldMap.merge(productName, qty, Long::sum);
                    salesMap.merge(productName, subTotal, Double::sum);
                });
            } catch (Exception e) {
                // Jangan sampai 1 error transaksi merusak dashboard
                e.printStackTrace();
            }
        }

        // Konversi ke DTO, sort desc by itemSold, ambil 5 teratas
        List<ItemSummaryDTO> todayItemSummary = itemSoldMap.entrySet().stream()
                .map(entry -> {
                    String productName = entry.getKey();
                    long totalSold = entry.getValue();
                    double totalSales = salesMap.getOrDefault(productName, 0.0);
                    return ItemSummaryDTO.builder()
                            .itemName(productName)
                            .itemSold(totalSold)
                            .sales(totalSales)
                            .build();
                })
                .sorted(Comparator.comparingLong(ItemSummaryDTO::getItemSold).reversed())
                .limit(5)
                .toList();

        model.addAttribute("todayItemSummary", todayItemSummary);


        // Untuk Tampilkan Product di Library
        List<ProductDTO> products = productService.getAllProducts();
        model.addAttribute("products", products);

        // Untuk Penampung Edit Product
        Product product = new Product();
        model.addAttribute("product", product);

        // Untuk Menampilkan List Cashier
        model.addAttribute("cashiers", cashierService.getAllCashiers());

        return "A_dashboard";
    }

    // Controller untuk Halaman Transaction (detail item)
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

    // Controller untuk Halaman Library - ADD PRODUCT
    @PostMapping("/A_dashboard/addProd")
    public String addProduct(@ModelAttribute("product") Product product,
                             @RequestParam("photo") MultipartFile file,
                             Model model) {
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
        return "redirect:/A_dashboard?section=library";
    }

    @GetMapping("/A_dashboard/getProd/{id}")
    @ResponseBody
    public ProductDTO getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping("/A_dashboard/editProd/{id}")
    public String editProduct(
            @PathVariable Long id,
            @ModelAttribute ProductDTO productDTO,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            Model model,
            HttpSession session
    ) {
        try {
            Long adminId = (Long) session.getAttribute("adminId");
            if (adminId == null) return "redirect:/login";

            if (photo != null && !photo.isEmpty()) {
                String filePath = productService.saveFile(photo);
                productDTO.setPhotoUrl(filePath);
            }

            productService.editProductById(id, productDTO, adminId);
            model.addAttribute("successMessage", "Product successfully updated!");
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to update product. Please try again.");
        }

        return "redirect:/A_dashboard?section=library";
    }

    @GetMapping("/A_dashboard/stockLogs/{productId}")
    @ResponseBody
    public List<ProductStockLog> getStockLogs(@PathVariable Long productId, HttpSession session) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) return List.of();

        return productStockLogRepository.findByProduct_IdOrderByCreatedOnDesc(productId);
    }

    @PostMapping("/A_dashboard/delProd/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProductById(id);

        return "redirect:/A_dashboard?section=library";
    }

    @PostMapping("/A_dashboard/deactivateCashier/{id}")
    public String deactivateCashier(@PathVariable Long id, HttpSession session) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) return "redirect:/login";

        cashierService.deactivateCashier(id);
        return "redirect:/A_dashboard?section=cashier";
    }

    @PostMapping("/A_dashboard/activateCashier/{id}")
    public String activateCashier(@PathVariable Long id, HttpSession session) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) return "redirect:/login";

        cashierService.activateCashier(id);
        return "redirect:/A_dashboard?section=cashier";
    }


}
