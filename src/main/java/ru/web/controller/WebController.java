package ru.web.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import ru.model.Url;
import ru.repository.UrlRepository;

import java.util.Optional;

@Controller
public class WebController {
    private final UrlRepository urlRepository;

    public WebController(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    // need to add http:// to all that does not have?

    @GetMapping("/code/{code}")
    public String redirectToFullUrl(@PathVariable String code, HttpServletResponse response) {
        Optional<Url> urlOpt = urlRepository.findByCode(code);
        if (urlOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "notFoundError";
        }
        return "redirect:" + urlOpt.get().getFullUrl();
    }

    @GetMapping("/str/{customPath}")
    public String redirectToFullUrlUsingCustomPath(@PathVariable String customPath, HttpServletResponse response) {
        Optional<Url> urlOpt = urlRepository.findByCustomPath(customPath);
        if (urlOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "notFoundError";
        }
        return "redirect:" + urlOpt.get().getFullUrl();
    }

    @GetMapping("/main")
    public String getMainPage() {
        return "main";
    }
}
