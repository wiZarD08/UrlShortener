package ru.web.mapper;

import jakarta.servlet.http.HttpServletRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.model.Url;
import ru.model.User;
import ru.web.dto.UrlDto;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Mapper(componentModel = "spring")
public interface UrlDtoMapper {
    @Mapping(target = "shortUrl", expression = "java(getShortUrl(request, url.getCode()))")
    @Mapping(target = "customShortUrl", expression = "java(getCustomShortUrl(request, url.getCustomPath()))")
    @Mapping(target = "expirationPeriod", expression = "java(countDaysLeft(url))")
    @Mapping(target = "userId", expression = "java(getUserId(url.getUser()))")
    UrlDto toDto(Url url, HttpServletRequest request);

    default String getShortUrl(HttpServletRequest request, String code) {
        StringBuffer buffer = request.getRequestURL();
        return buffer.replace(buffer.indexOf("api/url"), buffer.length(), "").append("code/").append(code.trim()).toString();
    }

    default String getCustomShortUrl(HttpServletRequest request, String customPath) {
        if (customPath != null && !customPath.isEmpty()) {
            StringBuffer buffer = request.getRequestURL();
            return buffer.replace(buffer.indexOf("api/url"), buffer.length(), "").append("str/").append(customPath.trim()).toString();
        }
        return null;
    }

    default Integer countDaysLeft(Url url) {
        LocalDate.now();
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), url.getExpirationDate());
    }

    default Long getUserId(User user) {
        return (user == null) ? null : user.getId();
    }
}
