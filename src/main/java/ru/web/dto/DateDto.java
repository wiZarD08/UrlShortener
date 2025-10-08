package ru.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DateDto {
    private String date;
    private int clicks;

    public DateDto(String date) {
        this.date = date;
    }

    public void addClick() {
        clicks++;
    }
}
