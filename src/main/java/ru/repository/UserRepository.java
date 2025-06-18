package ru.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
