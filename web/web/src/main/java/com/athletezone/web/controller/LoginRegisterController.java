package com.athletezone.web.controller;

import com.athletezone.web.dto.UserDTO;
import com.athletezone.web.models.User;
import com.athletezone.web.services.UserService;
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
public class LoginRegisterController {

    private final UserService userService;

    @Autowired
    public LoginRegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public @ResponseBody ResponseEntity<String> registerUser(@Valid @RequestBody UserDTO userDTO) {
        userService.registerUser(userDTO);
        return ResponseEntity.ok("User registered successfully");
    }

    @GetMapping("/")
    public String showRegisterPage() {
        return "register";
    }


    @PostMapping("/login")
    public @ResponseBody ResponseEntity<String> loginUser(
            @RequestBody UserDTO userDTO, HttpSession session) {

        // Validasi email dan password
        boolean isAuthenticated = userService.authenticateUser(userDTO.getEmail(), userDTO.getPassword());
        if (isAuthenticated) {
            // Ambil user berdasarkan email
            User user = userService.findByEmail(userDTO.getEmail());
            if (user != null) {
                // Simpan id user ke dalam session
                session.setAttribute("userId", user.getId());
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