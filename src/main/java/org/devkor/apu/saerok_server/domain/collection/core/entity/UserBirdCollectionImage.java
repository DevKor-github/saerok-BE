package org.devkor.apu.saerok_server.domain.collection.core.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.global.entity.Auditable;

@Entity
@Getter
@NoArgsConstructor
public class UserBirdCollectionImage extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_bird_collection_id", nullable = false)
    private UserBirdCollection collection;

    @Column(name = "object_key", nullable = false)
    private String objectKey;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Builder
    public UserBirdCollectionImage(UserBirdCollection collection, String objectKey, String contentType, int orderIndex) {
        if (collection == null) throw new IllegalArgumentException("collection은 null일 수 없습니다.");
        if (objectKey == null || objectKey.isEmpty()) throw new IllegalArgumentException("objectKey는 null이거나 빈 문자열일 수 없습니다.");
        if (contentType == null || contentType.isEmpty()) throw new IllegalArgumentException("contentType은 null이거나 빈 문자열일 수 없습니다.");

        this.collection = collection;
        this.objectKey = objectKey;
        this.contentType = contentType;
        this.orderIndex = orderIndex;
    }
}
