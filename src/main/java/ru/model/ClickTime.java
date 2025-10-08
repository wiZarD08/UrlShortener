package ru.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
public class ClickTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(insertable = false)
    private LocalDateTime dateTime;

    @ManyToOne
    @JoinColumn(name = "url_id")
    private Url url;

    public ClickTime() {
        dateTime = LocalDateTime.now();
    }
}
