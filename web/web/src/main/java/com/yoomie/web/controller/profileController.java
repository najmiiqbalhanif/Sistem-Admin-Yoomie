package com.yoomie.web.controller;

import com.yoomie.web.dto.AdminDTO;
import com.yoomie.web.dto.ChangePasswordRequest;
import com.yoomie.web.services.AdminService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class profileController {

    private final AdminService adminService;

    public profileController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return "redirect:/login";
        }

        AdminDTO admin = adminService.DTOgetAdminById(adminId);
        model.addAttribute("admin", admin);

        return "A_profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute("admin") AdminDTO formAdmin,
                                HttpSession session) {

        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return "redirect:/login";
        }

        // pastikan update profile TIDAK mengubah password
        formAdmin.setPassword(null);

        adminService.updateAdminProfile(adminId, formAdmin);
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest req, HttpSession session) {

        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new IllegalArgumentException("Konfirmasi password tidak sama.");
        }

        adminService.changePassword(adminId, req.getCurrentPassword(), req.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password berhasil diubah."));
    }
}
