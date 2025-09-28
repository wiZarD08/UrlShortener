package ru.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UtmTagDto {
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-\\.\\_]*$",
            message = "Source contains invalid characters. Only letters, numbers, spaces, hyphens, dots, and underscores are allowed")
    private String source;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-\\.\\_]*$",
            message = "Medium contains invalid characters. Only letters, numbers, spaces, hyphens, dots, and underscores are allowed")
    private String medium;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-\\.\\_]*$",
            message = "Campaign contains invalid characters. Only letters, numbers, spaces, hyphens, dots, and underscores are allowed")
    private String campaign;

    @Pattern(regexp = "^[a-zA-Z0-9\\s\\-\\.\\_]*$",
            message = "Content contains invalid characters. Only letters, numbers, spaces, hyphens, dots, and underscores are allowed")
    private String content;

    private Long clicks;
}
