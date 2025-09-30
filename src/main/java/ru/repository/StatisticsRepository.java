package ru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.model.Statistics;

import java.util.List;

public interface StatisticsRepository extends JpaRepository<Statistics, Long> {
    List<Statistics> findByIpAddress(String ipAddress);
    List<Statistics> findByUrlId(Long urlId);
}
