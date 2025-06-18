package ru.web.dto;

import lombok.Data;

@Data
public class CreateUrlRequest {
    private String fullUrl;
    private Integer days = 90;
}
