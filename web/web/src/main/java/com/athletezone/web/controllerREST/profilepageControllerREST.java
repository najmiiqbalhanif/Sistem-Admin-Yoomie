package com.athletezone.web.controllerREST;

import com.athletezone.web.dto.UserDTO;
import com.athletezone.web.models.User;
import com.athletezone.web.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profilepage")
@CrossOrigin(origins = "*") // Agar Flutter dapat mengakses (jika frontend dan backend terpisah)
public class profilepageControllerREST {

    private final UserService userService;

    @Autowired
    public profilepageControllerREST(UserService userService) {
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
