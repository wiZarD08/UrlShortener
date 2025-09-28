package ru.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UrlWithUtmTags {
    private String shortUrl;
    private String customShortUrl;
}
