package com.yoomie.web.controller;

import com.yoomie.web.dto.AdminDTO;
import com.yoomie.web.models.Admin;
import com.yoomie.web.services.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
public class loginRegisterController {

    private final AdminService adminService;

    @Autowired
    public loginRegisterController(AdminService adminService) {
        this.adminService = adminService;
    }

    // REGISTER ADMIN
    @PostMapping("/register")
    public @ResponseBody ResponseEntity<String> registerAdmin(@Valid @RequestBody AdminDTO adminDTO) {
        adminService.registerAdmin(adminDTO);
        return ResponseEntity.ok("Admin registered successfully");
    }

    // Tampilkan halaman register (root diarahkan ke register admin)
    @GetMapping("/")
    public String showRegisterPage() {
        return "register";
    }

    // LOGIN ADMIN
    @PostMapping("/login")
    public @ResponseBody ResponseEntity<String> loginAdmin(
            @RequestBody AdminDTO adminDTO, HttpSession session) {

        boolean isAuthenticated = adminService.authenticateAdmin(
                adminDTO.getEmail(),
                adminDTO.getPassword()
        );

        if (isAuthenticated) {
            Admin admin = adminService.findByEmail(adminDTO.getEmail());
            if (admin != null) {
                // Simpan id admin ke dalam session
                session.setAttribute("adminId", admin.getId());
                return ResponseEntity.ok("Login successful");
            }
        }
        return ResponseEntity.status(401).body("Invalid email or password");
    }

    // Halaman login admin
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // LOGOUT ADMIN
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Hapus semua data di session
        session.invalidate();

        // Arahkan kembali ke halaman login
        return "redirect:/login";
    }
}
