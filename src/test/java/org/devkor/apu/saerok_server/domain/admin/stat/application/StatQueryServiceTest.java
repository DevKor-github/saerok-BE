package org.devkor.apu.saerok_server.domain.admin.stat.application;

import org.devkor.apu.saerok_server.domain.admin.stat.api.dto.response.StatSeriesResponse;
import org.devkor.apu.saerok_server.domain.admin.stat.core.entity.DailyStat;
import org.devkor.apu.saerok_server.domain.admin.stat.core.entity.StatMetric;
import org.devkor.apu.saerok_server.domain.admin.stat.core.repository.DailyStatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatQueryServiceTest {

    @Mock DailyStatRepository dailyRepo;
    @InjectMocks StatQueryService sut;

    @Test
    void getSeries_cumulatesDailyPlatformSignupCountsFromBeforeTheRequestedRange() {
        LocalDate firstDate = LocalDate.of(2026, 7, 1);
        LocalDate startDate = firstDate.plusDays(1);
        LocalDate endDate = firstDate.plusDays(2);
        when(dailyRepo.findSeriesByMetric(StatMetric.USER_DEVICE_PLATFORM_SIGNUP_CUMULATIVE, null, endDate))
                .thenReturn(List.of(
                        DailyStat.ofPayload(StatMetric.USER_DEVICE_PLATFORM_SIGNUP_CUMULATIVE, firstDate,
                                Map.of("IOS", 2L)),
                        DailyStat.ofPayload(StatMetric.USER_DEVICE_PLATFORM_SIGNUP_CUMULATIVE, startDate,
                                Map.of("IOS", 1L, "ANDROID", 3L)),
                        DailyStat.ofPayload(StatMetric.USER_DEVICE_PLATFORM_SIGNUP_CUMULATIVE, endDate,
                                Map.of("ANDROID", 2L))
                ));

        StatSeriesResponse response = sut.getSeries(
                List.of(StatMetric.USER_DEVICE_PLATFORM_SIGNUP_CUMULATIVE),
                startDate + "," + endDate
        );

        List<StatSeriesResponse.ComponentSeries> components = response.series().getFirst().components();
        assertThat(valuesOf(components, "IOS")).containsExactly(3L, 3L);
        assertThat(valuesOf(components, "ANDROID")).containsExactly(3L, 5L);
    }

    private List<Long> valuesOf(List<StatSeriesResponse.ComponentSeries> components, String key) {
        return components.stream()
                .filter(component -> component.key().equals(key))
                .findFirst()
                .orElseThrow()
                .points()
                .stream()
                .map(point -> point.value().longValue())
                .toList();
    }
}
