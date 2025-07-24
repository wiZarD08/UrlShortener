package ru.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Url {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullUrl;

    @Size(min = 8, max = 8)
    @Column(nullable = false, unique = true)
    private String code;

    @Size(min = 1, max = 100)
    @Column(name = "string", unique = true)
    private String customPath;

    @Column(nullable = false)
    private LocalDate creationDate;

    @Column(nullable = false)
    private LocalDate expirationDate;

    private boolean utmSupport;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "url")
    private List<UtmTag> utm_tags = new ArrayList<>();

    public Url() {
    }

    public Url(String fullUrl, String code, int days) {
        this.code = code;
        if (fullUrl.startsWith("http")) this.fullUrl = fullUrl;
        else this.fullUrl = "https://" + fullUrl;
        creationDate = LocalDate.now();
        expirationDate = creationDate.plusDays(days);
    }
}

// = ZonedDateTime.now(Clock.systemUTC());
