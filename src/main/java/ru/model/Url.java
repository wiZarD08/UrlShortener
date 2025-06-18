package ru.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullUrl;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "string", unique = true)
    private String customPath;

    @Column(nullable = false)
    private LocalDate creationDate;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    protected Url() {
    }

    public Url(String fullUrl, String code, int days) {
        this.code = code;
        this.fullUrl = fullUrl;
        creationDate = LocalDate.now();
        expirationDate = creationDate.plusDays(days);
    }
}

// = ZonedDateTime.now(Clock.systemUTC());
