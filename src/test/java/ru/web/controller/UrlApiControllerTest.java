package ru.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.model.Url;
import ru.model.User;
import ru.repository.UrlRepository;
import ru.repository.UserRepository;
import ru.web.dto.CreateUrlRequest;
import ru.web.dto.UpdateUrlRequest;
import ru.web.dto.UrlDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class UrlApiControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UrlRepository urlRepository;

    @MockitoBean
    private UserRepository userRepository;

    private static final String fullUrl = "https://google.com";

    @WithMockUser(username = "user")
    @Test
    public void testGetUrlList_CreatedByUser() throws Exception {
        List<Url> urlList = new ArrayList<>();
        Url url1 = new Url(fullUrl, "code88", 30);
        urlList.add(url1);
        Mockito.when(urlRepository.findByUserUsername("user")).thenReturn(urlList);

        mockMvc.perform(get("/api/urls/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fullUrl").value(url1.getFullUrl()));

        Mockito.verify(urlRepository, times(1)).findByUserUsername("user");
    }

    @WithMockUser(username = "user", roles = "ADMIN")
    @Test
    public void testGetUrlList_ForAdmin() throws Exception {
        List<Url> urlList = new ArrayList<>();
        Url url1 = new Url(fullUrl, "code88", 30);
        Url url2 = new Url(fullUrl + "/page1234", "code73", 50);
        urlList.add(url1);
        urlList.add(url2);
        Mockito.when(urlRepository.findAll()).thenReturn(urlList);
        Mockito.when(urlRepository.findByUserUsername("user")).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/urls/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fullUrl").value(url1.getFullUrl()))
                .andExpect(jsonPath("$[1].fullUrl").value(url2.getFullUrl()));

        Mockito.verify(urlRepository, times(1)).findAll();
        Mockito.verify(urlRepository, times(0)).findByUserUsername("user");
    }

    @WithMockUser(username = "user")
    @Test
    public void testCreateUrl_ByRealUser() throws Exception {
        Url url = new Url();
        url.setFullUrl(fullUrl);
        CreateUrlRequest urlRequest = new CreateUrlRequest();
        urlRequest.setFullUrl(fullUrl);
        User user = new User("user", "password");
        user.setId(1L);

        Mockito.when(userRepository.findByUsername(any(String.class)))
                .thenReturn(Optional.of(user));
        Mockito.when(urlRepository.findByCode(any(String.class))).thenReturn(Optional.empty());
        Mockito.when(urlRepository.save(any(Url.class))).thenReturn(url);

        System.out.println(objectMapper.writeValueAsString(urlRequest));
        mockMvc.perform(post("/api/urls")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(urlRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortUrl").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.fullUrl").value(fullUrl))
                .andExpect(jsonPath("$.expirationPeriod").value(90));

        Mockito.verify(userRepository, times(1)).findByUsername("user");
        Mockito.verify(urlRepository, times(1)).findByCode(any(String.class));
        Mockito.verify(urlRepository, times(1)).save(any(Url.class));
    }

    @WithMockUser(username = "user")
    @Test
    public void testUpdateUrl_NotCreatedByThisUser() throws Exception {
        User user = new User("user", "password");
        List<Url> urlList = new ArrayList<>();
        Url url1 = new Url(fullUrl, "code88", 30);
        Url url2 = new Url(fullUrl + "/page1234", "code73", 50);
        url1.setId(0L);
        url2.setId(1L);
        url1.setUser(user);
        url2.setUser(user);
        urlList.add(url1);
        urlList.add(url2);

        Long urlId = 12L;
        Url urlToUpdate = new Url(fullUrl + "/update", "code93", 10);
        UpdateUrlRequest urlRequest = new UpdateUrlRequest();
        urlRequest.setCustomPath("path");
        urlRequest.setExpirationPeriod(90);

        Mockito.when(urlRepository.findByUserUsername("user")).thenReturn(urlList);
        Mockito.when(urlRepository.findById(urlId)).thenReturn(Optional.of(urlToUpdate));
        Mockito.when(urlRepository.findByCustomPath(any(String.class))).thenReturn(Optional.empty());
        Mockito.when(urlRepository.save(any(Url.class))).thenReturn(new Url());


        mockMvc.perform(patch("/api/urls/" + urlId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(urlRequest)))
                .andExpect(status().isForbidden());

        Mockito.verify(urlRepository, times(1)).findByUserUsername("user");
        Mockito.verify(urlRepository, times(0)).findById(urlId);
    }

    @WithMockUser(username = "user")
    @Test
    public void testUpdateUrl_CreatedByThisUser() throws Exception {
        User user = new User("user", "password");
        List<Url> urlList = new ArrayList<>();
        Url url1 = new Url(fullUrl, "code88", 30);
        Url url2 = new Url(fullUrl + "/page1234", "code73", 50);
        url1.setId(0L);
        url2.setId(1L);
        url1.setUser(user);
        url2.setUser(user);
        urlList.add(url1);
        urlList.add(url2);

        Long urlId = 12L;
        Url urlToUpdate = new Url(fullUrl + "/update", "code93", 10);
        urlToUpdate.setId(urlId);
        urlToUpdate.setUser(user);
        urlList.add(urlToUpdate);
        UpdateUrlRequest urlRequest = new UpdateUrlRequest();
        urlRequest.setCustomPath("path");
        urlRequest.setExpirationPeriod(90);

        Mockito.when(urlRepository.findByUserUsername("user")).thenReturn(urlList);
        Mockito.when(urlRepository.findById(urlId)).thenReturn(Optional.of(urlToUpdate));
        Mockito.when(urlRepository.findByCustomPath(any(String.class))).thenReturn(Optional.empty());
        Mockito.when(urlRepository.save(any(Url.class))).thenReturn(new Url());

        MvcResult result = mockMvc.perform(patch("/api/urls/" + urlId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(urlRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(urlId))
                .andExpect(jsonPath("$.fullUrl").value(urlToUpdate.getFullUrl()))
                .andExpect(jsonPath("$.expirationPeriod").value(urlRequest.getExpirationPeriod()))
                .andReturn();

        UrlDto urlDtoResult = objectMapper.readValue(result.getResponse().getContentAsString(), UrlDto.class);
        assertTrue(urlDtoResult.getCustomShortUrl().endsWith(urlRequest.getCustomPath()));

        Mockito.verify(urlRepository, times(1)).findByUserUsername("user");
        Mockito.verify(urlRepository, times(1)).findById(urlId);
        Mockito.verify(urlRepository, times(1)).findByCustomPath(any(String.class));
        Mockito.verify(urlRepository, times(1)).save(any(Url.class));
    }
}
