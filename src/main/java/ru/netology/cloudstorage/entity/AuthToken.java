package ru.netology.cloudstorage.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class AuthToken {

    @Id
    private String token;

    @ManyToOne(optional = false)
    private User user;
}
