package com.athletezone.web.services.impl;

import com.athletezone.web.dto.UserDTO;
import com.athletezone.web.models.User;
import com.athletezone.web.models.Cart;
import com.athletezone.web.repositories.UserRepository;
import com.athletezone.web.repositories.CartRepository;
import com.athletezone.web.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    private static final String UPLOAD_DIR = "uploads/";
    private static final String BASE_IMAGE_URL = "http://10.0.2.2:8080/";

    @Autowired
    public UserServiceImpl(UserRepository userRepository, CartRepository cartRepository) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
    }

    @Override
    public User registerUser(UserDTO userDTO) {
        userRepository.findByEmail(userDTO.getEmail()).ifPresent(user -> {
            throw new IllegalStateException("Email already in use");
        });

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());

        user = userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(user);
        cartRepository.save(cart);

        user.setCart(cart);

        return userRepository.save(user);
    }

    @Override
    public User registerUserApp(UserDTO userDTO) {
        userRepository.findByEmail(userDTO.getEmail()).ifPresent(user -> {
            throw new IllegalStateException("Email already in use");
        });

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword());
        user.setFullName(userDTO.getFullName());

        user = userRepository.save(user);

        Cart cart = new Cart();
        cart.setUser(user);
        cartRepository.save(cart);

        user.setCart(cart);

        return userRepository.save(user);
    }

    @Override
    public boolean authenticateUser(String email, String password) {
        return userRepository.findByEmail(email)
                .map(user -> user.getPassword().equals(password))
                .orElse(false);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public UserDTO DTOgetUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return convertToDTO(user);
    }

    private UserDTO convertToDTO(User user) {
        String profileImagePath = user.getProfileImage() != null ? user.getProfileImage().replace("\\", "/") : "";
        String fullProfileImageUrl = profileImagePath.isEmpty() ? "" : BASE_IMAGE_URL + "ProfImg/" + profileImagePath;

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .fullName(user.getFullName())
                .profileImage(fullProfileImageUrl)
                .build();
    }

    @Override
    public void updateUserProfile(Long userId, String username, String email, String fullName, MultipartFile profileImage) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);

        if (profileImage != null && !profileImage.isEmpty()) {
            String relativePath = saveProfileImg(profileImage);
            user.setProfileImage(relativePath);
        }

        userRepository.save(user);
    }

    public String saveProfileImg(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }
}
