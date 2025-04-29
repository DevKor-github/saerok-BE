package org.devkor.apu.saerok_server.domain.dex.bird.domain.service;

import org.devkor.apu.saerok_server.domain.dex.bird.query.view.builder.BirdProfileViewTestBuilder;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.contract.HasBodyLength;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.dto.SizeCategoryDto;
import org.devkor.apu.saerok_server.global.config.SizeCategoryRulesConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SizeCategoryServiceTest {

    private SizeCategoryRulesConfig sizeCategoryRules;
    private SizeCategoryService sizeCategoryService;

    @BeforeAll
    void setUpSizeCategoryRules() {
        SizeCategoryRulesConfig rules = new SizeCategoryRulesConfig();
        rules.setBoundaries(List.of(
                new SizeCategoryRulesConfig.Boundary("small", 10.0),
                new SizeCategoryRulesConfig.Boundary("large", 31.5),
                new SizeCategoryRulesConfig.Boundary("xlarge", null)
        ));
        rules.setLabels(Map.of(
            "small", "작은 크기",
                "large", "큰 크기",
                "xlarge", "아주 큰 크기"
        ));

        sizeCategoryRules = rules;
    }

    @BeforeEach
    void setUp() {
         sizeCategoryService = new SizeCategoryService(sizeCategoryRules);
    }

    @ParameterizedTest
    @CsvSource(
            value = {
            "NULL, NULL, NULL",
            "10.0, small, 작은 크기",
            "10.1, large, 큰 크기",
            "31.5, large, 큰 크기",
            "31.6, xlarge, 아주 큰 크기"
            },
            nullValues = {"NULL"}
    )
    void getSizeCategory_경계값테스트(Double lenStr, String expectedCode, String expectedLabel) {
        // given
        HasBodyLength bird = new BirdProfileViewTestBuilder().bodyLengthCm(lenStr).build();

        // when
        SizeCategoryDto dto = sizeCategoryService.getSizeCategory(bird);

        // then
        assertThat(dto.getCode()).isEqualTo(expectedCode);
        assertThat(dto.getLabel()).isEqualTo(expectedLabel);
    }
}