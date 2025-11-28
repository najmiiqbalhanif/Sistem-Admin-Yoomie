package com.yoomie.web.controllerREST;

import com.yoomie.web.dto.CartItemDTO;
import com.yoomie.web.dto.ProductDTO;
import com.yoomie.web.models.CartItem;
import com.yoomie.web.models.Product;
import com.yoomie.web.services.CartService;
import com.yoomie.web.services.CheckoutService;
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
    private final CheckoutService checkoutService;

    // 10.0.2.2 = alamat host (localhost) dari perspektif Android emulator
    private static final String BASE_IMAGE_URL = "http://10.0.2.2:8080/";

    // =========================
    // 1. ADD TO CART (+1 ITEM)
    // =========================
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(@RequestParam Long cashierId,
                                            @RequestParam Long productId) {
        try {
            // Hanya menambah ke cart, stok dikurangi saat checkout
            cartService.addToCart(cashierId, productId);
            return ResponseEntity.ok("Product added to cart successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Failed to add product to cart: " + e.getMessage());
        }
    }

    // ==========================================
    // 2. DECREASE QUANTITY DI CART (-1 ITEM)
    // ==========================================
    @PostMapping("/decrease")
    public ResponseEntity<String> decreaseProductQuantity(@RequestParam Long cashierId,
                                                          @RequestParam Long productId) {
        try {
            cartService.decreaseProductQuantity(cashierId, productId);
            return ResponseEntity.ok("Product quantity decreased successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to decrease product quantity: " + e.getMessage());
        }
    }

    // =============================================
    // 3. REMOVE PRODUCT DARI CART (HAPUS SATU BARIS)
    // =============================================
    @DeleteMapping("/remove")
    public ResponseEntity<String> removeProductFromCart(@RequestParam Long cashierId,
                                                        @RequestParam Long productId) {
        try {
            cartService.removeProductFromCart(cashierId, productId);
            return ResponseEntity.ok("Product removed from cart successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to remove product from cart: " + e.getMessage());
        }
    }

    // ====================================================
    // 4. UPDATE QUANTITY LANGSUNG (SET QTY BARU DI CART)
    // ====================================================
    @PostMapping("/updateQuantity")
    public ResponseEntity<String> updateProductQuantity(@RequestParam Long cashierId,
                                                        @RequestParam Long productId,
                                                        @RequestParam int quantity) {
        try {
            cartService.updateProductQuantity(cashierId, productId, quantity);
            return ResponseEntity.ok("Product quantity updated successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update product quantity: " + e.getMessage());
        }
    }

    // ==========================================
    // 5. GET ITEM CART PER CASHIER
    // ==========================================
    @GetMapping("/items/{cashierId}")
    public ResponseEntity<List<CartItemDTO>> getCartItems(@PathVariable Long cashierId) {
        try {
            List<CartItem> cartItems = cartService.getCartItemsByCashierId(cashierId);

            List<CartItemDTO> cartItemDTOs = cartItems.stream()
                    .map(item -> {
                        Product product = item.getProduct();

                        ProductDTO productDTO = ProductDTO.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .brand(product.getBrand())
                                .category(product.getCategory())
                                .price(product.getPrice())
                                .stock(product.getStock())
                                .photoUrl(buildFullImageUrl(product.getPhotoUrl()))
                                .build();

                        return CartItemDTO.builder()
                                .id(item.getId())
                                .quantity(item.getQuantity())
                                .subTotal(item.getSubTotal())
                                .product(productDTO)
                                .build();
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(cartItemDTOs);
        } catch (IllegalArgumentException e) {
            System.err.println("Error fetching cart items for cashier " + cashierId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("Unexpected error fetching cart items for cashier " + cashierId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ==========================================
    // 6. CHECKOUT / SUBMIT TRANSACTION
    // ==========================================
    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(@RequestParam Long cashierId) {
        try {
            checkoutService.checkout(cashierId);
            return ResponseEntity.ok("Transaksi berhasil. Stok produk sudah diperbarui.");
        } catch (IllegalArgumentException e) {
            // Pesan error sudah berbentuk:
            // "<Nama Produk> hanya tersisa <Jumlah>" atau "<Nama Produk> sudah habis."
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to checkout: " + e.getMessage());
        }
    }

    // =========================
    // HELPER FUNCTIONS
    // =========================
    private String buildFullImageUrl(String photoUrl) {
        if (photoUrl == null || photoUrl.isEmpty()) {
            return "";
        }
        String rawPath = photoUrl.replace("\\", "/");
        return BASE_IMAGE_URL + rawPath;
    }
}
