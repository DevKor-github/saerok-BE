package org.devkor.apu.saerok_server.domain.collection.core.service;

import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.enums.ConservationGrade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class CollectionLocationMaskerTest {

    private static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    @Test
    @DisplayName("보호등급 새 컬렉션은 소유자가 아닌 사용자에게 위치를 숨긴다")
    void protectedBird_nonOwner_masksLocation() {
        UserBirdCollection collection = collectionWithBird(ConservationGrade.GRADE_I);

        assertThat(CollectionLocationMasker.shouldMaskLocation(collection, false)).isTrue();
        assertThat(CollectionLocationMasker.latitude(collection, false)).isNull();
        assertThat(CollectionLocationMasker.longitude(collection, false)).isNull();
        assertThat(CollectionLocationMasker.locationAlias(collection, false)).isNull();
        assertThat(CollectionLocationMasker.address(collection, false)).isNull();
    }

    @Test
    @DisplayName("보호등급 새 컬렉션도 소유자에게는 원본 위치를 보여준다")
    void protectedBird_owner_keepsLocation() {
        UserBirdCollection collection = collectionWithBird(ConservationGrade.GRADE_II);

        assertThat(CollectionLocationMasker.shouldMaskLocation(collection, true)).isFalse();
        assertThat(CollectionLocationMasker.latitude(collection, true)).isEqualTo(37.5665);
        assertThat(CollectionLocationMasker.longitude(collection, true)).isEqualTo(126.9780);
        assertThat(CollectionLocationMasker.locationAlias(collection, true)).isEqualTo("서울광장");
        assertThat(CollectionLocationMasker.address(collection, true)).isEqualTo("서울 중구");
    }

    @Test
    @DisplayName("보호등급이 없는 새 컬렉션은 소유자가 아니어도 위치를 보여준다")
    void unprotectedBird_nonOwner_keepsLocation() {
        UserBirdCollection collection = collectionWithBird(ConservationGrade.NONE);

        assertThat(CollectionLocationMasker.shouldMaskLocation(collection, false)).isFalse();
        assertThat(CollectionLocationMasker.latitude(collection, false)).isEqualTo(37.5665);
        assertThat(CollectionLocationMasker.longitude(collection, false)).isEqualTo(126.9780);
        assertThat(CollectionLocationMasker.locationAlias(collection, false)).isEqualTo("서울광장");
        assertThat(CollectionLocationMasker.address(collection, false)).isEqualTo("서울 중구");
    }

    @Test
    @DisplayName("bird가 아직 없는 동정 요청 컬렉션은 위치를 숨기지 않는다")
    void pendingCollection_keepsLocation() {
        UserBirdCollection collection = collectionWithBird(null);

        assertThat(CollectionLocationMasker.shouldMaskLocation(collection, false)).isFalse();
        assertThat(CollectionLocationMasker.latitude(collection, false)).isEqualTo(37.5665);
        assertThat(CollectionLocationMasker.longitude(collection, false)).isEqualTo(126.9780);
    }

    private static UserBirdCollection collectionWithBird(ConservationGrade grade) {
        UserBirdCollection collection = new UserBirdCollection();
        collection.setLocation(GEOMETRY_FACTORY.createPoint(new Coordinate(126.9780, 37.5665)));
        collection.setLocationAlias("서울광장");
        collection.setAddress("서울 중구");

        if (grade != null) {
            Bird bird = new Bird();
            ReflectionTestUtils.setField(bird, "conservationGrade", grade);
            ReflectionTestUtils.setField(collection, "bird", bird);
        }

        return collection;
    }
}
