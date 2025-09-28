package ru.model;

import jakarta.persistence.*;
import lombok.Data;

//@Entity
@Data
public class Statistics {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;

    @Column(nullable = false)
    private String ipAddress;

//    @ManyToOne
//    @JoinColumn(name = "url_id")
//    private Url url;
}
