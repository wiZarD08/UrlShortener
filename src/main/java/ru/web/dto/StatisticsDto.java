package ru.web.dto;

import lombok.Data;

@Data
public class StatisticsDto {
    private String country;
    private String city;
    private String device;
    private String agent;
    private String os;
}
