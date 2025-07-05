package ru.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class UtmTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String source;

    @Column(nullable = false)
    private String medium;

    @Column(nullable = false)
    private String campaign;

    private String content;

    @ManyToOne
    @JoinColumn(name = "url_id")
    private Url url;
}
