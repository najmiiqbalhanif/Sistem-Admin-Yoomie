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

        @Column(nullable = false)
        private Boolean active = true;

        @Column(nullable = false)
        private Boolean deleted = false;

        @Column(name = "profile_image")
        private String profileImage;

        @OneToOne(mappedBy = "cashier")
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        private Cart cart;

        @OneToMany(mappedBy = "cashier")
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        private List<Transaction> transactions;
    }
