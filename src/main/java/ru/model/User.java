package ru.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity(name = "app_user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 50, message = "Username must be between 2 and 50 characters")
    @Column(name = "login")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @OneToMany(mappedBy = "user")
    private List<Url> urls = new ArrayList<>();

    protected User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
