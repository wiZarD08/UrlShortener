package ru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.model.ClickTime;

import java.util.List;

public interface ClickTimeRepository extends JpaRepository<ClickTime, Long> {
    List<ClickTime> findByUrlId(Long urlId);

    void deleteAllByUrlId(Long urlId);
}
