package ru.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.service.UrlService;
import ru.service.UtmStatService;
import ru.web.dto.UtmTagDto;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatApiController {
    private final UtmStatService statService;
    private final UrlService urlService;

    @GetMapping("/{urlId}")
    public List<UtmTagDto> getUrlStatistics(@PathVariable Long urlId) {
        if (!urlService.isUrlSupportUtm(urlId)) return new ArrayList<>();
        return statService.getUtmList(urlId);
    }

    @GetMapping("/utm/{urlId}")
    public List<UtmTagDto> getUtmList(@PathVariable Long urlId) {
        if (!urlService.isUrlSupportUtm(urlId)) return new ArrayList<>();
        return statService.getUtmList(urlId);
    }
}
