package com.athletezone.web.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    @Column(name = "profile_image")
    private String profileImage; // Bisa disimpan path ke file atau URL

    // Relasi ke Cart (satu user memiliki satu keranjang)
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude // Mencegah infinite recursion
    @ToString.Exclude
    private Cart cart;

    // Relasi ke Order (satu user bisa memiliki banyak pesanan)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude // Mencegah infinite recursion
    @ToString.Exclude
    private List<Order> orders;
}
