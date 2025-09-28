package ru.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Statistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String device;

    @Column(nullable = false)
    private String agent;

    @Column(name = "os", nullable = false)
    private String OS;

    @ManyToOne
    @JoinColumn(name = "url_id")
    private Url url;
}
