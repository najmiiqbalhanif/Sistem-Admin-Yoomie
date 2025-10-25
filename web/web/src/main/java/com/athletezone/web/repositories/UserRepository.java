package com.athletezone.web.repositories;

import com.athletezone.web.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Menambahkan method untuk mencari user berdasarkan email
    Optional<User> findByEmail(String email);
}