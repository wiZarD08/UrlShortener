package ru.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.service.UrlService;
import ru.service.UtmStatService;
import ru.web.dto.CreateUrlRequest;
import ru.web.dto.UpdateUrlRequest;
import ru.web.dto.UrlDto;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
public class UrlApiController {
    private final UrlService service;
    private final UtmStatService statService;

    @GetMapping
    public List<UrlDto> getUrlList(HttpServletRequest request) {
        return service.getAllUrls(request);
    }

    @GetMapping("/user")
    public List<UrlDto> getUrlListCreatedByUser(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            if (authentication.getAuthorities().stream()
                    .anyMatch(x -> x.getAuthority().equalsIgnoreCase("ROLE_ADMIN")))
                return service.getAllUrls(request);

            return service.getAllUrlsCreatedByUser(authentication.getName(), request);
        }
        return new ArrayList<>();
    }

    @GetMapping("/{id}")
    public UrlDto getUrl(@PathVariable Long id, HttpServletRequest request) {
        UrlDto urlDto = service.getUrlById(id, request);
        if (urlDto == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no url with " + id + " id");
        return urlDto;
    }

    @PostMapping
    public UrlDto createNewUrl(@RequestBody CreateUrlRequest urlRequest, HttpServletRequest request) {
        String username = "";
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            username = authentication.getName();
        }
        return service.createAndSaveUrl(urlRequest, request, username);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @urlService.isOwner(#id, authentication.name)")
    public UrlDto updateUrl(@RequestBody UpdateUrlRequest urlRequest, @PathVariable Long id, HttpServletRequest request) {
        return service.updateUrl(urlRequest, id, request);
    }

    @PatchMapping("/{id}/utm_support")
    @PreAuthorize("hasRole('ADMIN') or @urlService.isOwner(#id, authentication.name)")
    public UrlDto setUtmSupportToUrl(@RequestBody boolean utmSupport, @PathVariable Long id, HttpServletRequest request) {
        UrlDto urlDto = service.setUrmSupport(id, utmSupport, request);
        if (urlDto == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no url with " + id + " id");
        return urlDto;
    }

    @PatchMapping("/{id}/add_days")
    @PreAuthorize("hasRole('ADMIN') or @urlService.isOwner(#id, authentication.name)")
    public UrlDto extendExpirationPeriod(@RequestBody int days, @PathVariable Long id, HttpServletRequest request) {
        UrlDto urlDto = service.extendExpirationPeriod(id, days, request);
        if (urlDto == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no url with " + id + " id");
        return urlDto;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @urlService.isOwner(#id, authentication.name)")
    public UrlDto deleteUrl(@PathVariable Long id, HttpServletRequest request) {
        statService.deleteAllDataConnected(id);
        UrlDto urlDto = service.deleteUrl(id, request);
        if (urlDto == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no url with " + id + " id");
        return urlDto;
    }
}
