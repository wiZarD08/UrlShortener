package ru.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.model.Url;
import ru.repository.UrlRepository;
import ru.repository.UserRepository;
import ru.service.ShortenerService;
import ru.web.dto.CreateUrlRequest;
import ru.web.dto.UpdateUrlRequest;
import ru.web.dto.UrlDto;
import ru.web.mapper.UrlDtoMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlApiController {
    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final UrlDtoMapper urlDtoMapper;
    private final ShortenerService service;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping
    public List<UrlDto> getUrlList(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            // if (authentication.getAuthorities().forEach(x -> x.getAuthority());)
            // if admin return all urls
            return urlRepository.findByUserUsername(authentication.getName()).stream().map(x -> urlDtoMapper.toDto(x, request)).toList();
        }
        return new ArrayList<>();
    }

    @GetMapping("/{id}")
    public UrlDto getUrlInfo(@PathVariable Long id, HttpServletRequest request) {
        Optional<Url> urlOpt = urlRepository.findById(id);
        if (urlOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "url is not found");
//        System.out.println("found url with code " + urlOpt.get().getCode());
        return urlDtoMapper.toDto(urlOpt.get(), request);
    }

    // create new url in db, return id of url
    @PostMapping
    public Long createNewUrl(@RequestBody CreateUrlRequest urlRequest, HttpServletRequest request) {
        String uniqueCode = service.create8ByteCode(urlRequest.getFullUrl());
        while (urlRepository.findByCode(uniqueCode).isPresent())
            uniqueCode = service.changeCode(uniqueCode);
        Url url = new Url(urlRequest.getFullUrl(), uniqueCode, urlRequest.getDays());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            userRepository.findByUsername(authentication.getName()).ifPresent(url::setUser);
        }

        try {
            urlRepository.save(url);
        } catch (ConstraintViolationException e) {
            e.getConstraintViolations().forEach(x ->
                    logger.error("ConstraintViolationException \"{}\" while saving new url", x.getMessage()));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validation error");
        }

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
        try {
            urlRepository.save(url);
        } catch (ConstraintViolationException e) {
            e.getConstraintViolations().forEach(x ->
                    logger.error("ConstraintViolationException \"{}\" while updating url", x.getMessage()));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validation error");
        }
    }
}
