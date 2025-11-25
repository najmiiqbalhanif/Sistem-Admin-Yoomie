package com.yoomie.web.controller;

import com.yoomie.web.dto.AdminDTO;
import com.yoomie.web.models.Admin;
import com.yoomie.web.services.AdminService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class profileController {

    private final AdminService adminService;

    public profileController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        // Ambil adminId dari session (harusnya sudah di-set saat login)
        Long adminId = (Long) session.getAttribute("adminId");
        if (adminId == null) {
            return "redirect:/login";
        }

        // Ambil data admin asli dari DB via service
        Admin admin = adminService.getAdminById(adminId);
        if (admin == null) {
            // safety net kalau id di session sudah tidak valid
            return "redirect:/login";
        }

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

        // Simpan perubahan profile (fullName, adminName, email)
        adminService.updateAdminProfile(adminId, formAdmin);

        // Setelah update, reload halaman profile
        return "redirect:/profile";
    }
}