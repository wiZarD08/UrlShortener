package ru;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.model.User;
import ru.repository.UrlRepository;
import ru.repository.UserRepository;

@EnableJpaRepositories
@EnableScheduling
@SpringBootApplication
public class SpringApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(SpringApplication.class, args);
    }

    @Bean
    public ApplicationRunner addAdminUser(UrlRepository repo, UserRepository userRepo, PasswordEncoder passEncoder) {
        return (args -> {
            if (userRepo.findByUsername("admin").isEmpty()) {
                userRepo.save(new User("admin", passEncoder.encode("1234")));
            }
        });
    }
}
