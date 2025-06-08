package org.devkor.apu.saerok_server.global.config;

import com.vane.badwordfiltering.BadWordFiltering;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * BadWordFiltering 라이브러리 설정
 * - 부적절하지 않다고 판단되는 단어들을 필터링 목록에서 제거
 */
@Slf4j
@Configuration
public class BadWordFilterConfig {

    @Bean
    @Primary
    public BadWordFiltering badWordFiltering() {
        BadWordFiltering filter = new BadWordFiltering();
        filter.remove("마스터");
        return filter;
    }
}
