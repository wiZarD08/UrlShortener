package ru.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.model.Url;
import ru.service.UtmStatService;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseCleanup {
    private final UrlRepository urlRepository;
    private final ClickTimeRepository timeRepository;
    private final UtmStatService statService;

    @Scheduled(cron = "0 40 23 * * ?")
    @Transactional
    public void dbCleanup() {
        deleteExpiredUrls();
        deleteDateTimeData();
    }

    private void deleteExpiredUrls() {
        List<Url> urlList = urlRepository.findAll();
        urlList.forEach(url -> {
            if (url.getExpirationDate().isBefore(LocalDate.now())) {
                statService.deleteAllDataConnected(url.getId());
                urlRepository.deleteById(url.getId());
            }
        });
    }

    private void deleteDateTimeData() {
        timeRepository.deleteOldRecords();
    }
}
