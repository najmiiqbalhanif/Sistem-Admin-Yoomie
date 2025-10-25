package com.athletezone.web.controllerREST;

import com.athletezone.web.dto.LoginResponseDTO;
import com.athletezone.web.dto.UserDTO;
import com.athletezone.web.models.User;
import com.athletezone.web.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class loginRegisterControllerREST {

    private final UserService userService;

    public loginRegisterControllerREST(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public @ResponseBody ResponseEntity<String> registerUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            userService.registerUserApp(userDTO);
            return ResponseEntity.ok("User registered successfully: ");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody UserDTO userDTO, HttpSession session) {
        boolean isAuthenticated = userService.authenticateUser(userDTO.getEmail(), userDTO.getPassword());
        if (isAuthenticated) {
            User user = userService.findByEmail(userDTO.getEmail());
            if (user != null) {
                session.setAttribute("userId", user.getId());

                // Buat DTO untuk response
                LoginResponseDTO loginResponse = new LoginResponseDTO(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getProfileImage()
                );

                return ResponseEntity.ok(loginResponse);
            }
        }
        return ResponseEntity.status(401).body("Invalid email or password");
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("User logged out successfully");
    }
}
