package ru.web.dto;

import lombok.Data;

@Data
public class UrlDto {
    private Long id;
    private String fullUrl;
    private String shortUrl;
    private String customShortUrl;
    private Integer expirationPeriod;
    private Long userId;
    private boolean utmSupport;
}
