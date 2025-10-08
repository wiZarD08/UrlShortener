package ru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.model.Statistics;

import java.util.List;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
    List<Statistics> findByIpAddressAndUrlId(String ipAddress, Long urlId);

    List<Statistics> findByUrlId(Long urlId);

    void deleteAllByUrlId(Long urlId);
}
