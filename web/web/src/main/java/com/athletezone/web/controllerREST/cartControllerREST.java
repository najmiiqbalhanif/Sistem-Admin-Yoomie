package com.athletezone.web.controllerREST;

import com.athletezone.web.dto.CartItemDTO;
import com.athletezone.web.dto.ProductDTO; // Import ProductDTO
import com.athletezone.web.models.CartItem;
import com.athletezone.web.services.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class cartControllerREST {

    private final CartService cartService;
    private static final String BASE_IMAGE_URL = "http://10.0.2.2:8080/"; // Sesuaikan jika perlu

    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestParam Long userId, @RequestParam Long productId) {
        try {
            cartService.addToCart(userId, productId);
            return ResponseEntity.ok("Product added to cart successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to add product to cart: " + e.getMessage());
        }
    }

    // --- START: Endpoint Baru/Revisi ---

    @PostMapping("/decrease")
    public ResponseEntity<String> decreaseProductQuantity(@RequestParam Long userId, @RequestParam Long productId) {
        try {
            cartService.decreaseProductQuantity(userId, productId);
            return ResponseEntity.ok("Product quantity decreased successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to decrease product quantity: " + e.getMessage());
        }
    }

    @DeleteMapping("/remove") // Menggunakan DELETE request untuk penghapusan
    public ResponseEntity<String> removeProductFromCart(@RequestParam Long userId, @RequestParam Long productId) {
        try {
            cartService.removeProductFromCart(userId, productId);
            return ResponseEntity.ok("Product removed from cart successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to remove product from cart: " + e.getMessage());
        }
    }

    @PostMapping("/updateQuantity") // Menggunakan POST untuk update quantity
    public ResponseEntity<String> updateProductQuantity(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam int quantity) { // 'quantity' adalah nilai baru
        try {
            if (quantity <= 0) {
                // Jika quantity 0 atau kurang, perlakukan sebagai penghapusan
                cartService.removeProductFromCart(userId, productId);
                return ResponseEntity.ok("Product removed from cart (quantity updated to 0).");
            } else {
                cartService.updateProductQuantity(userId, productId, quantity);
                return ResponseEntity.ok("Product quantity updated successfully.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error updating product quantity: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update product quantity: " + e.getMessage());
        }
    }

    // --- END: Endpoint Baru/Revisi ---

    // Ambil semua item cart berdasarkan userId
    @GetMapping("/items/{userId}")
    public ResponseEntity<List<CartItemDTO>> getCartItems(@PathVariable Long userId) {
        try {
            List<CartItem> cartItems = cartService.getCartItemsByUserId(userId);

            List<CartItemDTO> cartItemDTOs = cartItems.stream()
                    .map(item -> {
                        ProductDTO productDTO = ProductDTO.builder()
                                .id(item.getProduct().getId())
                                .name(item.getProduct().getName())
                                .brand(item.getProduct().getBrand())
                                .category(item.getProduct().getCategory())
                                .price(item.getProduct().getPrice())
                                .stock(item.getProduct().getStock())
                                .photoUrl(buildFullImageUrl(item.getProduct().getPhotoUrl()))
                                .build();

                        return CartItemDTO.builder()
                                .id(item.getId()) // Ini adalah ID CartItem, bukan Product ID
                                .quantity(item.getQuantity())
                                .subTotal(item.getSubTotal())
                                .product(productDTO) // Menyertakan objek ProductDTO bersarang
                                .build();
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(cartItemDTOs);
        } catch (IllegalArgumentException e) { // Tangkap exception jika cart tidak ditemukan
            System.err.println("Error fetching cart items for user " + userId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Misalnya, 404 Not Found
        } catch (Exception e) {
            System.err.println("Unexpected error fetching cart items for user " + userId + ": " + e.getMessage());
            e.printStackTrace(); // Cetak stack trace untuk debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String buildFullImageUrl(String photoUrl) {
        if (photoUrl == null || photoUrl.isEmpty()) {
            return "";
        }
        String rawPath = photoUrl.replace("\\", "/");
        return BASE_IMAGE_URL + rawPath;
    }
}