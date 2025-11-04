package com.yoomie.web.controllerREST;

import com.yoomie.web.dto.UserDTO;
import com.yoomie.web.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profilepage")
@CrossOrigin(origins = "*") // Agar Flutter dapat mengakses (jika frontend dan backend terpisah)
public class profilePageControllerREST {

    private final UserService userService;

    @Autowired
    public profilePageControllerREST(UserService userService) {
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
}
