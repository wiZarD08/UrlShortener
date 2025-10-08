package ru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import ru.model.ClickTime;

import java.util.List;

public interface ClickTimeRepository extends JpaRepository<ClickTime, Long> {
    @Modifying
    @Query(value = "DELETE FROM click_time WHERE date_time < CURRENT_DATE - 10",
            nativeQuery = true)
    void deleteOldRecords();

    List<ClickTime> findByUrlId(Long urlId);

    void deleteAllByUrlId(Long urlId);
}
