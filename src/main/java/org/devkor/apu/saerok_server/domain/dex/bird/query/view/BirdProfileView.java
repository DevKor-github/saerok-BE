package org.devkor.apu.saerok_server.domain.dex.bird.query.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import org.devkor.apu.saerok_server.domain.dex.bird.core.contract.HasBodyLength;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdDescription;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdName;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdTaxonomy;
import org.devkor.apu.saerok_server.domain.dex.bird.core.enums.HabitatType;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Immutable
@Getter
@Table(name = "bird_profile_mv")
public class BirdProfileView implements HasBodyLength {

    @Id
    private Long id;

    @Embedded
    private BirdName name;

    @Embedded
    private BirdTaxonomy taxonomy;

    @Embedded
    private BirdDescription description;

    @Column(name = "body_length_cm")
    private Double bodyLengthCm;

    @Column(name = "nibr_url")
    private String nibrUrl;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "habitats")
    private List<HabitatType> habitats;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "seasons_with_rarity")
    private List<SeasonWithRarity> seasonsWithRarity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "images")
    private List<Image> images;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Data
    public static class SeasonWithRarity {

        @JsonProperty("rarity")
        private String rarity;

        @JsonProperty("season")
        private String season;

        @JsonProperty("priority")
        private Integer priority;
    }

    @Data
    public static class Image {

        @JsonProperty("s3_url")
        private String s3Url;

        @JsonProperty("original_url")
        private String originalUrl;

        @JsonProperty("is_thumb")
        private Boolean isThumb;

        @JsonProperty("order_index")
        private Integer orderIndex;
    }

    public String toSummaryString() {
        // 대표 이미지 URL (없으면 "N/A")
        String thumbUrl = images != null
                ? images.stream()
                .filter(Image::getIsThumb)
                .map(Image::getS3Url)
                .findFirst()
                .orElse("N/A")
                : "N/A";

        // taxonomy 에서 큰 분류만 뽑아보기 (예: 계–강–목)
        String taxonomySummary = String.join(" > ",
                taxonomy.getPhylumKor(),
                taxonomy.getClassKor(),
                taxonomy.getOrderKor()
        );

        return "=== BirdProfile Summary ===\n" +
                String.format("ID             : %d\n", id) +
                String.format("Common Name    : %s\n", name.getKoreanName()) +
                String.format("Scientific Name: %s\n", name.getScientificName()) +
                String.format("Taxonomy       : %s\n", taxonomySummary) +
                String.format("Body Length    : %.1f cm\n", bodyLengthCm) +
                String.format("Habitats       : %s\n", habitats) +
                String.format("Updated At     : %s\n", updatedAt) +
                String.format("Thumbnail URL  : %s\n", thumbUrl);
    }

    @Override
    public String toString() {
        return "BirdProfile{" +
                "id=" + id +
                ", name=" + name +
                ", taxonomy=" + taxonomy +
                ", description=" + description +
                ", bodyLengthCm=" + bodyLengthCm +
                ", nibrUrl='" + nibrUrl + '\'' +
                ", habitats=" + habitats +
                ", seasonsWithRarity=" + seasonsWithRarity +
                ", images=" + images +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
