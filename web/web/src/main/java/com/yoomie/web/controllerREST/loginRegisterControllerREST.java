package com.yoomie.web.controllerREST;

import com.yoomie.web.dto.LoginResponseDTO;
import com.yoomie.web.dto.CashierDTO;
import com.yoomie.web.models.Cashier;
import com.yoomie.web.services.CashierService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class loginRegisterControllerREST {

    private final CashierService cashierService;

    public loginRegisterControllerREST(CashierService cashierService) {
        this.cashierService = cashierService;
    }

    @PostMapping("/register")
    public @ResponseBody ResponseEntity<String> registerCashier(@Valid @RequestBody CashierDTO cashierDTO) {
        try {
            cashierService.registerCashierApp(cashierDTO);
            return ResponseEntity.ok("Cashier registered successfully: ");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginCashier(@RequestBody CashierDTO cashierDTO, HttpSession session) {
        boolean isAuthenticated = cashierService.authenticateCashier(cashierDTO.getEmail(), cashierDTO.getPassword());
        if (isAuthenticated) {
            Cashier cashier = cashierService.findByEmail(cashierDTO.getEmail());
            if (cashier != null) {
                session.setAttribute("cashierId", cashier.getId());

                // Buat DTO untuk response
                LoginResponseDTO loginResponse = new LoginResponseDTO(
                        cashier.getId(),
                        cashier.getCashierName(),
                        cashier.getEmail(),
                        cashier.getFullName(),
                        cashier.getProfileImage()
                );

                return ResponseEntity.ok(loginResponse);
            }
        }
        return ResponseEntity.status(401).body("Invalid email or password");
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logoutCashier(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Cashier logged out successfully");
    }
}
