package org.devkor.apu.saerok_server.domain.collection.core.entity;

import jakarta.persistence.*;
import org.devkor.apu.saerok_server.global.entity.Auditable;

@Entity
public class UserBirdCollectionImage extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_bird_collection_id", nullable = false)
    private UserBirdCollection collection;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;
}
