package ru.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.model.Url;
import ru.repository.UrlRepository;
import ru.service.ShortenerService;
import ru.web.dto.CreateUrlRequest;
import ru.web.dto.UpdateUrlRequest;
import ru.web.dto.UrlDto;
import ru.web.mapper.UrlDtoMapper;

import java.util.Optional;

@RestController
@RequestMapping("/api/url")
public class UrlApiController {
    private final UrlRepository urlRepository;
    private final UrlDtoMapper urlDtoMapper;
    private final ShortenerService service;

    public UrlApiController(UrlRepository urlRepository, UrlDtoMapper urlDtoMapper, ShortenerService service) {
        this.urlRepository = urlRepository;
        this.urlDtoMapper = urlDtoMapper;
        this.service = service;
    }

    @GetMapping("/{id}")
    public UrlDto getUrlInfo(@PathVariable Long id, HttpServletRequest request) {
        Optional<Url> urlOpt = urlRepository.findById(id);
        if (urlOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "url is not found");
        System.out.println("found url with code " + urlOpt.get().getCode());
        return urlDtoMapper.toDto(urlOpt.get(), request);
    }

    // create new url in db, return id of url
    @PostMapping
    public Long createNewUrl(@RequestBody CreateUrlRequest urlRequest, HttpServletRequest request) {
        String uniqueCode = service.create8ByteCode(urlRequest.getFullUrl());
        while (urlRepository.findByCode(uniqueCode).isPresent())
            uniqueCode = service.changeCode(uniqueCode);
        Url url = new Url(urlRequest.getFullUrl(), uniqueCode, urlRequest.getDays());
        System.out.println("new code: " + url.getCode());
//        url.setUser(user);
//        System.out.println("username: " + userDetails.getUsername());
        urlRepository.save(url);
//        System.out.println(" url " + request.getRequestURL());
        // return "http://my-site-domain.ru/" + Arrays.toString(url.getCode());
        return url.getId();
    }

    @PutMapping("/{id}")
    public void updateUrl(@RequestBody UpdateUrlRequest urlRequest, @PathVariable Long id) {
        Optional<Url> urlOpt = urlRepository.findById(id);
        if (urlOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NO_CONTENT, "no url with " + id + " id");
        Url url = urlOpt.get();
        String customPath = urlRequest.getCustomPath();
        if (customPath != null) {
            urlOpt = urlRepository.findByCustomPath(customPath);
            if (urlOpt.isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "custom path \"" + customPath + "\" is already in use");
            url.setCustomPath(customPath);
        }
        if (urlRequest.getExpirationPeriod() != null) {
            url.setExpirationDate(url.getCreationDate().plusDays(urlRequest.getExpirationPeriod()));
        }
        if (urlRequest.getExtendOnDays() > 0) {
            url.setExpirationDate(url.getExpirationDate().plusDays(urlRequest.getExtendOnDays()));
        }
        urlRepository.save(url);
    }
}
