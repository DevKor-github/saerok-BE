package org.devkor.apu.saerok_server.domain.dex.bird.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.devkor.apu.saerok_server.global.entity.Auditable;

@Entity
@Data
public class BirdImage extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bird_id", nullable = false)
    private Bird bird;

    @Column(name = "s3_url", nullable = false)
    private String s3Url;

    @Column(name = "original_url", nullable = false)
    private String originalUrl;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "is_thumb", nullable = false)
    private boolean isThumb;
}
