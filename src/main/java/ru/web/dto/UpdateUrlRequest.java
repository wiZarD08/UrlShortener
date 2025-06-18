package ru.web.dto;

import lombok.Data;

@Data
public class UpdateUrlRequest {
    private String customPath;
    private Integer expirationPeriod;
    private Integer extendOnDays = 0;
}
