package ru.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

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

    @Column(nullable = false)
    private Long clicks = 0L;

    @Column(nullable = false)
    private Long uniqueClicks = 0L;

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
        if (this.fullUrl.endsWith("/"))
            this.fullUrl = this.fullUrl.substring(0, fullUrl.length() - 1);
        creationDate = LocalDate.now();
        expirationDate = creationDate.plusDays(days);
    }

    public void addClick() {
        clicks++;
    }

    public void addUniqueClick() {
        uniqueClicks++;
    }
}

// = ZonedDateTime.now(Clock.systemUTC());
