package ru.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UtmTagDto {
    @NotBlank(message = "The source is required")
    @Pattern(regexp = "^[a-zA-Z0-9\\-._]*$",
            message = "Source contains invalid characters. Only letters, numbers, hyphens, dots, and underscores are allowed")
    private String source;

    @NotBlank(message = "The medium is required")
    @Pattern(regexp = "^[a-zA-Z0-9\\-._]*$",
            message = "Medium contains invalid characters. Only letters, numbers, hyphens, dots, and underscores are allowed")
    private String medium;

    @NotBlank(message = "The campaign is required")
    @Pattern(regexp = "^[a-zA-Z0-9\\-._]*$",
            message = "Campaign contains invalid characters. Only letters, numbers, hyphens, dots, and underscores are allowed")
    private String campaign;

    @Pattern(regexp = "^[a-zA-Z0-9\\-._]*$",
            message = "Content contains invalid characters. Only letters, numbers, hyphens, dots, and underscores are allowed")
    private String content;

    private Long clicks;
}
