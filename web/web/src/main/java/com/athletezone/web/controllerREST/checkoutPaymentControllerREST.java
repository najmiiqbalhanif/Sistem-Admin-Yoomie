package com.athletezone.web.controllerREST;

import com.athletezone.web.dto.OrderDTO; // Import OrderDTO
import com.athletezone.web.dto.PaymentDTO;
import com.athletezone.web.dto.PaymentItemDTO;
import com.athletezone.web.models.Order;
import com.athletezone.web.models.Payment;
import com.athletezone.web.models.User;
import com.athletezone.web.services.OrderService;
import com.athletezone.web.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
// Ubah base request mapping atau buat controller baru untuk orders
@RequestMapping("/api")
public class checkoutPaymentControllerREST { // Atau rename menjadi OrderControllerREST jika lebih sesuai
    private final OrderService orderService;
    private final UserService userService;

    public checkoutPaymentControllerREST(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    public static class CheckoutPaymentRequest extends PaymentDTO {
        private List<PaymentItemDTO> paymentItems;

        public List<PaymentItemDTO> getPaymentItems() {
            return paymentItems;
        }

        public void setPaymentItems(List<PaymentItemDTO> paymentItems) {
            this.paymentItems = paymentItems;
        }
    }

    @PostMapping("/checkoutpayment/submit")
    public ResponseEntity<?> submitCheckout(@RequestBody CheckoutPaymentRequest request) {
        User user = userService.getUserById(request.getUserId());
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        Payment payment = new Payment();
        payment.setAddress(request.getAddress());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTotalAmount(request.getTotalAmount());
        payment.setStatus("Paid"); // Status awal mungkin PENDING, Paid, etc. Sesuaikan dengan logika Anda.

        try {
            Order order = orderService.processCheckout(user, payment, request.getPaymentItems());
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to process checkout: " + e.getMessage());
        }
    }

    // --- NEW: Endpoint untuk mendapatkan semua order ---
    @GetMapping("/orders") // Endpoint yang akan kita panggil dari Flutter
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    // --- NEW: Endpoint untuk mendapatkan order berdasarkan user ID (lebih relevan untuk aplikasi user) ---
    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUserId(@PathVariable Long userId) {
        // Asumsi OrderService.getOrdersByUserId akan mengembalikan List<Order> atau List<OrderDTO>
        // Jika mengembalikan List<Order>, Anda perlu mengubahnya ke DTO di sini
        List<Order> orders = orderService.getOrdersByUserId(userId);
        // Konversi List<Order> ke List<OrderDTO>
        List<OrderDTO> orderDTOs = orders.stream()
                .map(order -> {
                    // Implementasi konversi dari Order ke OrderDTO
                    // Anda sudah punya orderToDTO di OrderServiceImpl, bisa reuse logikanya di sini
                    return OrderDTO.builder()
                            .id(order.getId())
                            .userId(order.getUser().getId())
                            .username(order.getUser().getUsername())
                            .createdOn(order.getPayment().getCreatedOn().toString())
                            .cartSummary(order.getPayment().getPaymentItems().stream()
                                    .map(item -> item.getProductName() + " x " + item.getQuantity())
                                    .collect(Collectors.joining(", ")))
                            .totalAmount(order.getPayment().getTotalAmount())
                            .paymentMethod(order.getPayment().getPaymentMethod())
                            .paymentStatus(order.getPayment().getStatus())
                            .address(order.getPayment().getAddress())
                            .build();
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }
}