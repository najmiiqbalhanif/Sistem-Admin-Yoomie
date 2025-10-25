package com.athletezone.web.services;

import com.athletezone.web.dto.UserDTO;
import com.athletezone.web.models.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UserService {
    User registerUser(UserDTO userDTO);

    User registerUserApp(UserDTO userDTO);

    boolean authenticateUser(String email, String password);

    User findByEmail(String email);

    public User getUserById(Long id);

    public UserDTO DTOgetUserById(Long id);

    public void updateUserProfile(Long userId, String username, String email, String fullName, MultipartFile profileImage) throws IOException;
}