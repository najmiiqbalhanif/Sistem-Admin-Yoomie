package com.yoomie.web.controller;

import com.yoomie.web.dto.AdminDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class profileController {

    @GetMapping("/profile")
    public String showProfile(Model model) {

        // TODO: sementara hardcode dulu untuk kebutuhan front-end.
        // Nanti temenmu bisa ganti bagian ini pakai AdminService + HttpSession.
        AdminDTO dummyAdmin = AdminDTO.builder()
                .fullName("Najmi Hanif")
                .adminName("najmi_admin")
                .email("admin@tokoanda.com")
                .build();

        // kirim ke view untuk dipakai di A_profile.html
        model.addAttribute("admin", dummyAdmin);

        // inisial untuk avatar bulat (misal dari full name "Najmi Hanif" -> "NH")
        String initials = "NH";
        model.addAttribute("adminInitials", initials);

        // render template A_profile.html
        return "A_profile";
    }
}
