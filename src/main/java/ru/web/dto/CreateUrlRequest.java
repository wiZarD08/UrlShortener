package ru.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUrlRequest {
    @NotNull
    @NotBlank
    private String fullUrl;
    private Integer days = 90;
}
