package ru.web.controller;

import org.apache.catalina.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.model.Url;
import ru.model.User;
import ru.repository.UrlRepository;
import ru.repository.UserRepository;
import ru.service.ShortenerService;

@Controller
@RequestMapping("/")
public class TestController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UrlRepository urlRepository;
    @Autowired
    private ShortenerService service;

//    @GetMapping("short/{code}")
//    public String getShortCode(@PathVariable String code) {
//        System.out.println("got code: " + code);
//        User user = userRepository.findAll().get(0);
//        System.out.println("user login: " + user.getLogin());
//        String urlStr = "http://myhouse-dream-000000.com/user?=postgres";
//        Url url = new Url(urlStr, service.create8ByteCode(urlStr), 70);
//        url.setUser(user);
//        urlRepository.save(url);
//        return "error";
//    }


}
