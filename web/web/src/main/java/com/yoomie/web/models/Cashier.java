    package com.yoomie.web.models;

    import jakarta.persistence.*;
    import lombok.*;

    import java.util.List;

    @Entity
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Table(name = "cashiers")
    public class Cashier extends BaseEntity {

        @Column(nullable = false, unique = true)
        private String cashierName;

        @Column(nullable = false, unique = true)
        private String email;

        @Column(nullable = false)
        private String password;

        @Column(nullable = false)
        private String fullName;

        @Column(name = "profile_image")
        private String profileImage; // Bisa disimpan path ke file atau URL

        // Relasi ke Cart (satu cashier memiliki satu keranjang)
        @OneToOne(mappedBy = "cashier", cascade = CascadeType.ALL, orphanRemoval = true)
        @EqualsAndHashCode.Exclude // Mencegah infinite recursion
        @ToString.Exclude
        private Cart cart;

        // Relasi ke Transaction (satu cashier bisa memiliki banyak pesanan)
        @OneToMany(mappedBy = "cashier", cascade = CascadeType.ALL, orphanRemoval = true)
        @EqualsAndHashCode.Exclude // Mencegah infinite recursion
        @ToString.Exclude
        private List<Transaction> transactions;
    }
