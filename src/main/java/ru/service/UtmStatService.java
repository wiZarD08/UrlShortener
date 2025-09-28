package ru.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.model.Statistics;
import ru.model.Url;
import ru.model.UtmTag;
import ru.repository.UtmTagRepository;
import ru.web.dto.UtmTagDto;
import ru.web.mapper.UtmTagDtoMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UtmStatService {
    private final UtmTagRepository utmRepository;
    private final UtmTagDtoMapper utmMapper;
    private static final String SOURCE = "utm_source";
    private static final String MEDIUM = "utm_medium";
    private static final String CAMPAIGN = "utm_campaign";
    private static final String CONTENT = "utm_content";
    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED"
    };
    private static final String USER_AGENT = "User-Agent";

    @Transactional
    public void checkUtmTags(Url url, HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        if (params.containsKey(SOURCE) && params.containsKey(MEDIUM) && params.containsKey(CAMPAIGN)) {
            String content = null;
            if (params.containsKey(CONTENT)) content = params.get(CONTENT)[0];
            Optional<UtmTag> utmTagOpt = utmRepository.findUtmTag(params.get(SOURCE)[0], params.get(MEDIUM)[0],
                    params.get(CAMPAIGN)[0], content, url.getId());
            if (utmTagOpt.isPresent()) {
                UtmTag utmTag = utmTagOpt.get();
                utmTag.setClicks(utmTag.getClicks() + 1);
                utmRepository.save(utmTag);
            } else {
                UtmTag utmTag = new UtmTag(params.get(SOURCE)[0], params.get(MEDIUM)[0],
                        params.get(CAMPAIGN)[0], content, 1L, url);
                utmRepository.save(utmTag);
            }
        }
    }

    public void writeStatistics(Url url, HttpServletRequest request) {
        Statistics statistics = new Statistics();
        statistics.setIpAddress(getRequestIpAddress(request));
        System.out.println(request.getHeader(USER_AGENT));

    }

    private static String getRequestIpAddress(HttpServletRequest request) {
        String ipAddress;
        for (String header : IP_HEADERS) {
            ipAddress = request.getHeader(header);
            if (ipAddress != null && (ipAddress.contains(".") || ipAddress.contains(":"))) {
                ipAddress = ipAddress.split(",")[0].trim();
                return ipAddress;
            }
        }
        return request.getRemoteAddr();
    }

    public List<UtmTagDto> getUtmList(Long urlId) {
        return utmRepository.findByUrlId(urlId).stream().map(utmMapper::toDto).toList();
    }

    public String getUtmTagString(String source, String medium, String campaign, String content) {
        StringBuilder builder = new StringBuilder().append("?").append(SOURCE).append("=").append(source).append("&")
                .append(MEDIUM).append("=").append(medium).append("&")
                .append(CAMPAIGN).append("=").append(campaign);
        if (content != null && !content.isEmpty()) builder.append("&").append(CONTENT).append("=").append(content);
        return builder.toString();
    }
}
