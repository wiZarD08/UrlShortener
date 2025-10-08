package ru.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.model.Url;
import ru.model.User;
import ru.repository.UrlRepository;
import ru.repository.UserRepository;
import ru.service.UrlService;
import ru.service.UtmStatService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class WebController {
    private final UrlRepository urlRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UrlService urlService;
    private final UtmStatService utmStatService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signUp")
    public String signUp() {
        return "signUp";
    }

    @PostMapping("/signUp")
    public String signUp(@RequestParam String username,
                         @RequestParam String password,
                         HttpServletRequest request, Model model) {
        if (password != null && (password.length() < 4 || password.length() > 50)) {
            model.addAttribute("error", "Password must be between 4 and 50 characters");
            return "signUp";
        }
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            model.addAttribute("error", "Please, choose another username");
            return "signUp";
        }
        User user = new User(username, passwordEncoder.encode(password));

        try {
            userRepository.save(user);
        } catch (ConstraintViolationException e) {
            List<String> validationErrors = new ArrayList<>();
            e.getConstraintViolations().forEach(x -> validationErrors.add(x.getMessage()));
            model.addAttribute("validationErrors", validationErrors);
            return "signUp";
        }

        authenticateUser(username, password, request);
        return "redirect:/main";
    }

    private void authenticateUser(String username, String password, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password);

        // use JpaUserDetailsService
        Authentication authentication = authenticationManager.authenticate(authToken);

        // Manually set authentication in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // For adding JSESSIONID cookie by Spring
        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );
    }

    @GetMapping("/code/{code}")
    public String redirectToFullUrl(@PathVariable String code, HttpServletRequest request, HttpServletResponse response) {
        Optional<Url> urlOpt = urlRepository.findByCode(code);
        if (urlOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "notFoundError";
        }
        if (!urlService.deleteIfExpired(urlOpt.get())) {
            if (urlOpt.get().getUser() != null) {
                utmStatService.checkUtmTags(urlOpt.get(), request);
                utmStatService.writeStatistics(urlOpt.get(), request);
            }
            return "redirect:" + urlOpt.get().getFullUrl();
        }
        return "notFoundError";
    }

    @GetMapping("/str/{customPath}")
    public String redirectToFullUrlUsingCustomPath(@PathVariable String customPath, HttpServletRequest request, HttpServletResponse response) {
        Optional<Url> urlOpt = urlRepository.findByCustomPath(customPath);
        if (urlOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "notFoundError";
        }
        if (!urlService.deleteIfExpired(urlOpt.get())) {
            utmStatService.checkUtmTags(urlOpt.get(), request);
            utmStatService.writeStatistics(urlOpt.get(), request);
            return "redirect:" + urlOpt.get().getFullUrl();
        }
        return "notFoundError";
    }

    @GetMapping("/main")
    public String getMainPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof AnonymousAuthenticationToken) {
            model.addAttribute("anonymous", true);
        } else if (authentication instanceof UsernamePasswordAuthenticationToken) {
            model.addAttribute("anonymous", false);
            model.addAttribute("username", authentication.getName());
        }
        return "main";
    }

    @GetMapping("/profile")
    public String getProfilePage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("username", authentication.getName());
        return "profile";
    }

    @GetMapping("/stats/{urlId}")
    @PreAuthorize("hasRole('ADMIN') or @urlService.isOwner(#urlId, authentication.name)")
    public String getStatisticsUtmPage(@PathVariable Long urlId) {
        return "statistics";
    }
}
