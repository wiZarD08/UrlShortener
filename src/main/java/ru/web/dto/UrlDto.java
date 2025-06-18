package ru.web.dto;

import lombok.Data;

@Data
public class UrlDto {
    private String fullUrl;
    private String shortUrl;
    private String customShortUrl;
    private Integer expirationPeriod;
}
