package ru.netology.cloudstorage.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "auth_tokens")
@Data
public class AuthToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
