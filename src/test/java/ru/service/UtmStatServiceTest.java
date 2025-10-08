package ru.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.model.ClickTime;
import ru.model.Url;
import ru.repository.ClickTimeRepository;
import ru.web.dto.DateDto;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class UtmStatServiceTest {
    @Autowired
    private UtmStatService statService;

    @MockitoBean
    private ClickTimeRepository timeRepository;

    private final int TIME_DIFFERENCE = 5;

    private int getServerOffset() {
        ZoneOffset serverOffset = ZoneId.systemDefault().getRules().getOffset(Instant.now());
        // range from 0 to 23
        return (serverOffset.getTotalSeconds() / 3600 + 24) % 24;
    }

    private int getTestOffset() {
        return (getServerOffset() + TIME_DIFFERENCE) % 24;
    }

    private List<ClickTime> getTestClickTimeList(long urlId) {
        Url url = new Url();
        url.setId(urlId);

        List<ClickTime> clickTimeList = new ArrayList<>();
        LocalDateTime localDateTime = LocalDateTime.now();

        for (long i = 0; i < 9; i++) {
            localDateTime = localDateTime.minusDays(1);
            if (i % 3 == 0)
                clickTimeList.add(new ClickTime(i, localDateTime.withHour(7).withMinute(59), url));
            clickTimeList.add(new ClickTime(i, localDateTime.withHour(10).withMinute(10), url));
            clickTimeList.add(new ClickTime(i, localDateTime.withHour(22).withMinute(10), url));
        }

        return clickTimeList;
    }

    @Test
    public void testGetTimeStats() {
        Long urlId = 1L;

        Mockito.when(timeRepository.findByUrlId(urlId)).thenReturn(getTestClickTimeList(urlId));

        List<Integer> resultTimeList = statService.getTimeStats(urlId, getServerOffset());
        assertEquals(3, resultTimeList.get(7));
        assertEquals(9, resultTimeList.get(10));
        assertEquals(9, resultTimeList.get(22));

        resultTimeList = statService.getTimeStats(urlId, getTestOffset());
        assertEquals(24, resultTimeList.size());
        assertEquals(3, resultTimeList.get((7 + TIME_DIFFERENCE) % 24));
        assertEquals(9, resultTimeList.get((10 + TIME_DIFFERENCE) % 24));
        assertEquals(9, resultTimeList.get((22 + TIME_DIFFERENCE) % 24));
    }

    @Test
    public void testGetDateStats() {
        Long urlId = 1L;

        Mockito.when(timeRepository.findByUrlId(urlId)).thenReturn(getTestClickTimeList(urlId));

        List<DateDto> resultDateList = statService.getDateStats(urlId, getServerOffset());
        assertEquals(10, resultDateList.size());
        LocalDate localDate = LocalDate.now().minusDays(9);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(0).getDate());
        assertEquals(2, resultDateList.get(0).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(1).getDate());
        assertEquals(2, resultDateList.get(1).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(2).getDate());
        assertEquals(3, resultDateList.get(2).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(3).getDate());
        assertEquals(2, resultDateList.get(3).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(4).getDate());
        assertEquals(2, resultDateList.get(4).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(5).getDate());
        assertEquals(3, resultDateList.get(5).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(6).getDate());
        assertEquals(2, resultDateList.get(6).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(7).getDate());
        assertEquals(2, resultDateList.get(7).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(8).getDate());
        assertEquals(3, resultDateList.get(8).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(9).getDate());
        assertEquals(0, resultDateList.get(9).getClicks());

        // test with other offset,
        // so localDateTime.withHour(22).withMinute(10) goes to the next day

        resultDateList = statService.getDateStats(urlId, getTestOffset());
        assertEquals(10, resultDateList.size());
        localDate = LocalDate.now().minusDays(9);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(0).getDate());
        assertEquals(1, resultDateList.get(0).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(1).getDate());
        assertEquals(2, resultDateList.get(1).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(2).getDate());
        assertEquals(3, resultDateList.get(2).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(3).getDate());
        assertEquals(2, resultDateList.get(3).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(4).getDate());
        assertEquals(2, resultDateList.get(4).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(5).getDate());
        assertEquals(3, resultDateList.get(5).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(6).getDate());
        assertEquals(2, resultDateList.get(6).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(7).getDate());
        assertEquals(2, resultDateList.get(7).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(8).getDate());
        assertEquals(3, resultDateList.get(8).getClicks());
        localDate = localDate.plusDays(1);

        assertEquals(localDate.format(DateTimeFormatter.ISO_DATE), resultDateList.get(9).getDate());
        assertEquals(1, resultDateList.get(9).getClicks());
    }
}
