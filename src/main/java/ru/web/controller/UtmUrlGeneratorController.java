package ru.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.repository.UrlRepository;
import ru.repository.UserRepository;
import ru.service.UrlService;
import ru.service.UtmStatService;
import ru.web.dto.UrlDto;
import ru.web.dto.UrlWithUtmTags;
import ru.web.dto.UtmTagDto;

import java.net.http.HttpRequest;

@RestController
@RequestMapping("/utm/generate")
@RequiredArgsConstructor
public class UtmUrlGeneratorController {
    private final UtmStatService utmService;
    private final UrlService urlService;

    @PostMapping("/{urlId}")
    public UrlWithUtmTags generateUtmUrl(@Valid @RequestBody UtmTagDto utmTagDto, @PathVariable Long urlId,
                                         HttpServletRequest request) {
        String utmTags = utmService.getUtmTagString(utmTagDto.getSource(), utmTagDto.getMedium(),
                utmTagDto.getCampaign(), utmTagDto.getContent());
        UrlDto urlDto = urlService.getUrlById(urlId, request);
        return new UrlWithUtmTags(urlDto.getShortUrl() + utmTags, urlDto.getCustomShortUrl() + utmTags);
    }
}
