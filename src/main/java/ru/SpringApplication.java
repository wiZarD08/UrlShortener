package ru;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.model.Url;
import ru.model.User;
import ru.repository.UrlRepository;
import ru.repository.UserRepository;

import java.util.Arrays;

@EnableJpaRepositories
@SpringBootApplication
public class SpringApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(SpringApplication.class, args);
    }

    @Bean
    public ApplicationRunner loadUserData(UrlRepository repo) {
        return (args -> {
//            System.out.println("save new url 123");
//            try {
//                repo.save(new Url("https://www.baeldung.com/spring-redirect-and-forward", "123", 10));
//            } catch (Exception e) {
//                System.out.println("Exception in loadData to db method !!!!!!!!!");
//                System.out.println(e.getMessage());
//            }
//            System.out.println("done");
            //repo.save(new User("login 2"));
        });
    }
}
