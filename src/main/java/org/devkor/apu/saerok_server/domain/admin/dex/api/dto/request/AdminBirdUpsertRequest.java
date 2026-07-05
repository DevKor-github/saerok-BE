package org.devkor.apu.saerok_server.domain.admin.dex.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.devkor.apu.saerok_server.domain.dex.bird.core.enums.ConservationGrade;
import org.devkor.apu.saerok_server.domain.dex.bird.core.enums.HabitatType;

public record AdminBirdUpsertRequest(
        @Valid
        @NotNull(message = "이름 정보를 입력해 주세요.")
        Name name,

        @Valid
        @NotNull(message = "분류 정보를 입력해 주세요.")
        Taxonomy taxonomy,

        @Valid
        Description description,

        @Schema(description = "체장(cm)", example = "47.5")
        @Positive(message = "체장은 0보다 커야 합니다.")
        Double bodyLengthCm,

        @Schema(description = "국립생물자원관 URL")
        String nibrUrl,

        @NotNull(message = "보호등급을 선택해 주세요.")
        ConservationGrade conservationGrade,

        @NotEmpty(message = "서식지를 하나 이상 선택해 주세요.")
        List<@NotNull(message = "서식지를 선택해 주세요.") HabitatType> habitats,

        @Valid
        @NotEmpty(message = "체류/희귀도 정보를 하나 이상 입력해 주세요.")
        List<@NotNull(message = "체류/희귀도 정보를 입력해 주세요.") @Valid Residency> residencies,

        @Valid
        @NotEmpty(message = "대표 이미지를 업로드해 주세요.")
        List<@NotNull(message = "이미지 정보를 입력해 주세요.") @Valid Image> images
) {
    public record Name(
            @Schema(description = "국문명", example = "해오라기")
            @NotBlank(message = "국문명을 입력해 주세요.")
            String koreanName,

            @Schema(description = "학명", example = "Butorides striata")
            @NotBlank(message = "학명을 입력해 주세요.")
            String scientificName,

            @Schema(description = "학명 명명자", example = "Linnaeus")
            String scientificAuthor,

            @Schema(description = "학명 명명년도", example = "1758")
            Integer scientificYear
    ) {
    }

    public record Taxonomy(
            @Schema(description = "문 영문명", example = "Chordata")
            @NotBlank(message = "문 영문명을 입력해 주세요.")
            String phylumEng,

            @Schema(description = "문 국문명", example = "척삭동물문")
            @NotBlank(message = "문 국문명을 입력해 주세요.")
            String phylumKor,

            @Schema(description = "강 영문명", example = "Aves")
            @NotBlank(message = "강 영문명을 입력해 주세요.")
            String classEng,

            @Schema(description = "강 국문명", example = "조강")
            @NotBlank(message = "강 국문명을 입력해 주세요.")
            String classKor,

            @Schema(description = "목 영문명", example = "Pelecaniformes")
            @NotBlank(message = "목 영문명을 입력해 주세요.")
            String orderEng,

            @Schema(description = "목 국문명", example = "사다새목")
            @NotBlank(message = "목 국문명을 입력해 주세요.")
            String orderKor,

            @Schema(description = "과 영문명", example = "Ardeidae")
            @NotBlank(message = "과 영문명을 입력해 주세요.")
            String familyEng,

            @Schema(description = "과 국문명", example = "왜가리과")
            @NotBlank(message = "과 국문명을 입력해 주세요.")
            String familyKor,

            @Schema(description = "속 영문명", example = "Butorides")
            @NotBlank(message = "속 영문명을 입력해 주세요.")
            String genusEng,

            @Schema(description = "속 국문명", example = "해오라기속")
            @NotBlank(message = "속 국문명을 입력해 주세요.")
            String genusKor,

            @Schema(description = "종 영문명", example = "striata")
            @NotBlank(message = "종 영문명을 입력해 주세요.")
            String speciesEng,

            @Schema(description = "종 국문명", example = "해오라기")
            @NotBlank(message = "종 국문명을 입력해 주세요.")
            String speciesKor
    ) {
    }

    public record Description(
            @Schema(description = "도감 설명")
            String description,

            @Schema(description = "설명 출처")
            String source,

            @Schema(description = "AI 생성 설명 여부")
            Boolean isAiGenerated
    ) {
    }

    public record Residency(
            @Schema(description = "체류 형태", example = "RESIDENT")
            @NotNull(message = "체류 형태를 선택해 주세요.")
            org.devkor.apu.saerok_server.domain.dex.residency.entity.ResidencyType residencyType,

            @Schema(description = "희귀도", example = "COMMON")
            @NotNull(message = "희귀도를 선택해 주세요.")
            org.devkor.apu.saerok_server.domain.dex.residency.entity.RarityType rarity,

            @Schema(description = "월 비트마스크. 비우면 체류 형태 기본 월 범위를 사용합니다.", example = "4095")
            @Min(value = 0, message = "월 비트마스크는 0 이상이어야 합니다.")
            @Max(value = 4095, message = "월 비트마스크는 4095 이하여야 합니다.")
            Integer monthBitmask
    ) {
    }

    public record Image(
            @Schema(description = "업로드된 도감 이미지 S3 object key", example = "raw/uuid.jpg")
            @NotBlank(message = "이미지를 업로드해 주세요.")
            String objectKey,

            @Schema(description = "이미지 원본 출처 URL", example = "https://commons.wikimedia.org/...")
            @NotBlank(message = "이미지 원본 출처 URL을 입력해 주세요.")
            String originalUrl
    ) {
    }
}
