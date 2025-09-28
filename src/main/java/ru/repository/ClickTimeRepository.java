package ru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.model.ClickTime;

public interface ClickTimeRepository extends JpaRepository<ClickTime, Long> {
}
