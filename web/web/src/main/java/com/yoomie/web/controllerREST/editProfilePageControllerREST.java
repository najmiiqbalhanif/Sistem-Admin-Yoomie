package com.yoomie.web.controllerREST;

import com.yoomie.web.dto.CashierDTO;
import com.yoomie.web.dto.ChangePasswordRequest;
import com.yoomie.web.services.CashierService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/editprofilepage")
@CrossOrigin(origins = "*")
public class editProfilePageControllerREST {

    private final CashierService cashierService;

    @Autowired
    public editProfilePageControllerREST(CashierService cashierService) {
        this.cashierService = cashierService;
    }

    @GetMapping("/{cashierId}")
    public ResponseEntity<CashierDTO> getProfileByCashierId(@PathVariable Long cashierId) {
        CashierDTO cashier = cashierService.DTOgetCashierById(cashierId);
        if (cashier == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cashier);
    }

    @PutMapping(value = "/{cashierId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateProfile(
            @PathVariable Long cashierId,
            @RequestParam("cashierName") String cashierName,
            @RequestParam("email") String email,
            @RequestParam("fullName") String fullName,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        try {
            cashierService.updateCashierProfile(cashierId, cashierName, email, fullName, profileImage);
            return ResponseEntity.ok("Profile updated successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload image");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ✅ NEW: Change password endpoint
    @PutMapping(value = "/{cashierId}/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> changePassword(
            @PathVariable Long cashierId,
            @Valid @RequestBody ChangePasswordRequest req
    ) {
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            // kalau kamu punya GlobalExceptionHandler, boleh lempar IllegalArgumentException saja
            return ResponseEntity.badRequest().body("Confirm password does not match");
        }

        cashierService.changePassword(cashierId, req.getCurrentPassword(), req.getNewPassword());
        return ResponseEntity.ok("Password updated successfully");
    }
}
