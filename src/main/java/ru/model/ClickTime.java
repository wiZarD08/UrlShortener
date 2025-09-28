package ru.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class ClickTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(insertable = false)
    private LocalDateTime dateTime;

    @ManyToOne
    @JoinColumn(name = "url_id")
    private Url url;
}
