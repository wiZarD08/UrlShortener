package ru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.model.Url;

import java.util.List;
import java.util.Optional;

public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByCode(String code);

    Optional<Url> findByCustomPath(String customPath);

    List<Url> findByUserUsername(String username);
}
