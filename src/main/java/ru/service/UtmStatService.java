package ru.service;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import ru.model.ClickTime;
import ru.model.Statistics;
import ru.model.Url;
import ru.model.UtmTag;
import ru.repository.ClickTimeRepository;
import ru.repository.StatisticsRepository;
import ru.repository.UtmTagRepository;
import ru.web.dto.DateDto;
import ru.web.dto.StatisticsDto;
import ru.web.dto.UtmTagDto;
import ru.web.mapper.StatDtoMapper;
import ru.web.mapper.UtmTagDtoMapper;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class UtmStatService {
    private final UtmTagRepository utmRepository;
    private final StatisticsRepository statRepository;
    private final ClickTimeRepository timeRepository;
    private final UtmTagDtoMapper utmMapper;
    private final StatDtoMapper statMapper;
    private final DatabaseReader dbReader;
    private static UserAgentAnalyzer userAgentAnalyzer;
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
    private static final String GEO_LITE_DB = "GeoLite2-City.mmdb";

    public UtmStatService(UtmTagDtoMapper utmMapper, UtmTagRepository utmRepository,
                          StatisticsRepository statRepository, ClickTimeRepository timeRepository, StatDtoMapper statMapper)
            throws IOException {
        this.utmMapper = utmMapper;
        this.utmRepository = utmRepository;
        this.statRepository = statRepository;
        this.timeRepository = timeRepository;
        this.statMapper = statMapper;

        File database = new ClassPathResource(GEO_LITE_DB).getFile();
        dbReader = new DatabaseReader.Builder(database).build();
        userAgentAnalyzer = UserAgentAnalyzer.newBuilder()
                .withField(UserAgent.DEVICE_CLASS)
                .withField(UserAgent.AGENT_NAME)
                .withField(UserAgent.OPERATING_SYSTEM_NAME)
                .build();
    }

    public void writeStatistics(Url url, HttpServletRequest request) {
        Statistics statistics = new Statistics();
        statistics.setIpAddress(getRequestIpAddress(request));

        try {
            CityResponse response = dbReader.city(InetAddress.getByName(statistics.getIpAddress()));
            statistics.setCountry(response.getCountry().getName());
            statistics.setCity(response.getCity().getName());
        } catch (IOException | GeoIp2Exception e) {
            statistics.setCountry("Unknown");
            statistics.setCity("Unknown");
        }

        UserAgent userAgent = userAgentAnalyzer.parse(request.getHeader(USER_AGENT));
        statistics.setDevice(makeNotNull(userAgent.getValue(UserAgent.DEVICE_CLASS)));
        statistics.setAgent(makeNotNull(userAgent.getValue(UserAgent.AGENT_NAME)));
        statistics.setOS(makeNotNull(userAgent.getValue(UserAgent.OPERATING_SYSTEM_NAME)));

        statistics.setUrl(url);
        List<Statistics> statList = statRepository.findByIpAddressAndUrlId(statistics.getIpAddress(), url.getId());
        boolean isInDb = false;
        for (Statistics s : statList) {
            if (s.getDevice().equals(statistics.getDevice()) &&
                    s.getAgent().equals(statistics.getAgent()) &&
                    s.getOS().equals(statistics.getOS())) {
                isInDb = true;
                break;
            }
        }
        url.addClick();
        if (!isInDb) {
            url.addUniqueClick();
            statRepository.save(statistics);
        }

        ClickTime clickTime = new ClickTime();
        clickTime.setUrl(url);
        timeRepository.save(clickTime);
    }

    private String getRequestIpAddress(HttpServletRequest request) {
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

    private String makeNotNull(String string) {
        if (string == null || string.isEmpty()) return "Unknown";
        return string;
    }

    public List<StatisticsDto> getStatList(Long urlId) {
        return statRepository.findByUrlId(urlId).stream().map(statMapper::toDto).toList();
    }

    public List<Integer> getTimeStats(Long urlId, int timeZone) {
        Integer[] resultArray = new Integer[24];
        Arrays.fill(resultArray, 0);

        List<ZonedDateTime> zonedDateTimeList = timeRepository.findByUrlId(urlId).stream()
                .map(x -> x.getDateTime().atZone(ZoneId.systemDefault())
                        .withZoneSameInstant(ZoneOffset.ofHours(timeZone))).toList();

        zonedDateTimeList.forEach(x -> resultArray[x.getHour()]++);

        return new ArrayList<>(Arrays.asList(resultArray));
    }

    public List<DateDto> getDateStats(Long urlId, int timeZone) {
        List<DateDto> resultList = new ArrayList<>();

        LocalDate date = LocalDate.now().minusDays(9);
        while (!date.equals(LocalDate.now())) {
            resultList.add(new DateDto(date.format(DateTimeFormatter.ISO_DATE)));
            date = date.plusDays(1);
        }
        resultList.add(new DateDto(date.format(DateTimeFormatter.ISO_DATE)));

        List<LocalDateTime> dateTimeListUserTimeZone = timeRepository.findByUrlId(urlId).stream()
                .map(x -> x.getDateTime().atZone(ZoneId.systemDefault())
                        .withZoneSameInstant(ZoneOffset.ofHours(timeZone)).toLocalDateTime()).toList();

        dateTimeListUserTimeZone.forEach(x -> {
            for (DateDto dateDto : resultList) {
                if (dateDto.getDate().equals(x.format(DateTimeFormatter.ISO_DATE))) {
                    dateDto.addClick();
                    break;
                }
            }
        });

        return resultList;
    }

    @Transactional
    public void checkUtmTags(Url url, HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
        if (params.containsKey(SOURCE) && params.containsKey(MEDIUM) && params.containsKey(CAMPAIGN)) {
            String content = null;
            if (params.containsKey(CONTENT)) content = params.get(CONTENT)[0];
            Optional<UtmTag> utmTagOpt = utmRepository.findUtmTag(params.get(SOURCE)[0], params.get(MEDIUM)[0],
                    params.get(CAMPAIGN)[0], content, url.getId());
            UtmTag utmTag;
            if (utmTagOpt.isPresent()) {
                utmTag = utmTagOpt.get();
                utmTag.setClicks(utmTag.getClicks() + 1);
            } else {
                utmTag = new UtmTag(params.get(SOURCE)[0], params.get(MEDIUM)[0],
                        params.get(CAMPAIGN)[0], content, 1L, url);
            }
            utmRepository.save(utmTag);
        }
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

    @Transactional
    public void deleteAllDataConnected(Long urlId) {
        statRepository.deleteAllByUrlId(urlId);
        utmRepository.deleteAllByUrlId(urlId);
        timeRepository.deleteAllByUrlId(urlId);
    }
}
