package com.spoonful.spoonful.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Table(name="users")
@Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true) private String email;
    @Column(name="password_hash", nullable=false) private String passwordHash;
    @Column(nullable=false) private String displayName;
    @Column(nullable=false) private LocalDateTime createdAt = LocalDateTime.now();
    @Column(nullable = false)
    private boolean emailVerified = false;
}
