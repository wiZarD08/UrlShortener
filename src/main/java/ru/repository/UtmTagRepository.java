package ru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.model.UtmTag;

import java.util.List;
import java.util.Optional;

public interface UtmTagRepository extends JpaRepository<UtmTag, Long> {
    @Query("SELECT utm FROM UtmTag utm " +
            "WHERE utm.source = :source " +
            "AND utm.medium = :medium " +
            "AND utm.campaign = :campaign " +
            "AND (utm.content = :content OR (utm.content IS NULL AND :content IS NULL))" +
            "AND utm.url.id = :urlId")
    Optional<UtmTag> findUtmTag(@Param("source") String source,
                                @Param("medium") String medium,
                                @Param("campaign") String campaign,
                                @Param("content") String content,
                                @Param("urlId") Long urlId);

    List<UtmTag> findByUrlId(Long urlId);

    void deleteAllByUrlId(Long urlId);
}
