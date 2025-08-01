package org.devkor.apu.saerok_server.domain.dex.bird.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;

@Entity
@Getter
public class BirdImage extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bird_id", nullable = false)
    private Bird bird;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "original_url", nullable = false)
    private String originalUrl;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "is_thumb", nullable = false)
    private boolean isThumb;
}
