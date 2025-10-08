package ru.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
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

    @Column(nullable = false)
    private Long clicks = 0L;

    @ManyToOne
    @JoinColumn(name = "url_id")
    private Url url;

    public UtmTag(String source, String medium, String campaign, String content, Long clicks, Url url) {
        this.source = source;
        this.medium = medium;
        this.campaign = campaign;
        this.content = content;
        this.clicks = clicks;
        this.url = url;
    }
}
