package com.athletezone.web.controllerREST;

import com.athletezone.web.dto.UserDTO;
import com.athletezone.web.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/editprofilepage")
@CrossOrigin(origins = "*") // Supaya bisa diakses dari Flutter
public class EditProfilePageControllerREST {

    private final UserService userService;

    @Autowired
    public EditProfilePageControllerREST(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getProfileByUserId(@PathVariable Long userId) {
        UserDTO user = userService.DTOgetUserById(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping(value = "/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateProfile(
            @PathVariable Long userId,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("fullName") String fullName,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        try {
            userService.updateUserProfile(userId, username, email, fullName, profileImage);
            return ResponseEntity.ok("Profile updated successfully");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Failed to upload image");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
