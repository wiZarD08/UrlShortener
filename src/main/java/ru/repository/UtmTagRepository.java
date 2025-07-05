package ru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.model.UtmTag;

public interface UtmTagRepository extends JpaRepository<UtmTag, Long> {
}
