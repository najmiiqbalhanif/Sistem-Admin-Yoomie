package com.yoomie.web.controller;

import com.yoomie.web.dto.CashierDTO;
import com.yoomie.web.models.Cashier;
import com.yoomie.web.services.CashierService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;

@Controller
public class loginRegisterController {

    private final CashierService cashierService;

    @Autowired
    public loginRegisterController(CashierService cashierService) {
        this.cashierService = cashierService;
    }

    @PostMapping("/register")
    public @ResponseBody ResponseEntity<String> registerCashier(@Valid @RequestBody CashierDTO cashierDTO) {
        cashierService.registerCashier(cashierDTO);
        return ResponseEntity.ok("Cashier registered successfully");
    }

    @GetMapping("/")
    public String showRegisterPage() {
        return "register";
    }


    @PostMapping("/login")
    public @ResponseBody ResponseEntity<String> loginCashier(
            @RequestBody CashierDTO cashierDTO, HttpSession session) {

        // Validasi email dan password
        boolean isAuthenticated = cashierService.authenticateCashier(cashierDTO.getEmail(), cashierDTO.getPassword());
        if (isAuthenticated) {
            // Ambil cashier berdasarkan email
            Cashier cashier = cashierService.findByEmail(cashierDTO.getEmail());
            if (cashier != null) {
                // Simpan id cashier ke dalam session
                session.setAttribute("cashierId", cashier.getId());
                return ResponseEntity.ok("Login successful");
            }
        }
        return ResponseEntity.status(401).body("Invalid email or password");
    }



    @GetMapping("login")
    public String login() {
        return "login";
    }
}