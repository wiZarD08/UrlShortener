package ru.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import ru.model.Url;
import ru.repository.UrlRepository;
import ru.repository.UserRepository;
import ru.web.dto.CreateUrlRequest;
import ru.web.dto.UpdateUrlRequest;
import ru.web.dto.UrlDto;
import ru.web.mapper.UrlDtoMapper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class UrlService {
    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final UrlDtoMapper urlDtoMapper;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public List<UrlDto> getAllUrls(HttpServletRequest request) {
        return urlRepository.findAll().stream().map(x -> urlDtoMapper.toDto(x, request)).toList();
    }

    public List<UrlDto> getAllUrlsCreatedByUser(String username, HttpServletRequest request) {
        return urlRepository.findByUserUsername(username).stream()
                .map(x -> urlDtoMapper.toDto(x, request)).toList();
    }

    public UrlDto getUrlById(Long id, HttpServletRequest request) {
        Optional<Url> url = urlRepository.findById(id);
        return url.map(value -> urlDtoMapper.toDto(value, request)).orElse(null);
    }

    public UrlDto createAndSaveUrl(CreateUrlRequest urlRequest, HttpServletRequest httpRequest, String username) {
        String uniqueCode = create8ByteCode(urlRequest.getFullUrl());
        while (urlRepository.findByCode(uniqueCode).isPresent())
            uniqueCode = changeCode(uniqueCode);
        Url url = new Url(urlRequest.getFullUrl(), uniqueCode, urlRequest.getDays());
        userRepository.findByUsername(username).ifPresent(url::setUser);

        try {
            urlRepository.save(url);
        } catch (ConstraintViolationException e) {
            e.getConstraintViolations().forEach(x ->
                    logger.error("ConstraintViolationException \"{}\" while saving new url", x.getMessage()));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validation error");
        }
        return urlDtoMapper.toDto(url, httpRequest);
    }

    public String create8ByteCode(String fullUrl) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // use hashing algorithm sha-256
            byte[] hash32Bytes = digest.digest(fullUrl.getBytes());
            // convert bytes to chars (6 bits - one char) and get first 8 of them
            // and get rid of bad chars '/' and '+' in the result
            return Base64.getEncoder().encodeToString(hash32Bytes).substring(0, 8)
                    .replace('/', '1').replace('+', '0');
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String changeCode(String code) {
        Random rand = new Random();
        StringBuilder builder = new StringBuilder(code);
        builder.setCharAt(rand.nextInt(code.length()), (char) (rand.nextInt(10) + '0'));
        return builder.toString();
    }

    public boolean isOwner(Long id, String username) {
        return urlRepository.findByUserUsername(username).stream().anyMatch(x -> Objects.equals(x.getId(), id));
    }

    public UrlDto updateUrl(UpdateUrlRequest urlRequest, Long id, HttpServletRequest request) {
        Optional<Url> urlOpt = urlRepository.findById(id);
        if (urlOpt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no url with " + id + " id");
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
        return urlDtoMapper.toDto(url, request);
    }

    public UrlDto setUrmSupport(Long id, boolean utmSupport, HttpServletRequest request) {
        return urlRepository.findById(id).map(x -> {
            x.setUtmSupport(utmSupport);
            urlRepository.save(x);
            return urlDtoMapper.toDto(x, request);
        }).orElse(null);
    }

    public UrlDto deleteUrl(Long id, HttpServletRequest request) {
        return urlRepository.findById(id).map(x -> {
            urlRepository.deleteById(id);
            return urlDtoMapper.toDto(x, request);
        }).orElse(null);
    }
}
