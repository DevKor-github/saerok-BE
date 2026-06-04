package org.devkor.apu.saerok_server.domain.collection.core.service;

import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.enums.ConservationGrade;

public final class CollectionLocationMasker {

    private CollectionLocationMasker() {
    }

    public static Double latitude(UserBirdCollection collection, boolean isOwner) {
        if (shouldMaskLocation(collection, isOwner) || collection == null || collection.getLocation() == null) {
            return null;
        }
        return collection.getLatitude();
    }

    public static Double longitude(UserBirdCollection collection, boolean isOwner) {
        if (shouldMaskLocation(collection, isOwner) || collection == null || collection.getLocation() == null) {
            return null;
        }
        return collection.getLongitude();
    }

    public static String locationAlias(UserBirdCollection collection, boolean isOwner) {
        if (shouldMaskLocation(collection, isOwner) || collection == null) {
            return null;
        }
        return collection.getLocationAlias();
    }

    public static String address(UserBirdCollection collection, boolean isOwner) {
        if (shouldMaskLocation(collection, isOwner) || collection == null) {
            return null;
        }
        return collection.getAddress();
    }

    public static boolean shouldMaskLocation(UserBirdCollection collection, boolean isOwner) {
        if (isOwner || collection == null) {
            return false;
        }

        Bird bird = collection.getBird();
        if (bird == null) {
            return false;
        }

        ConservationGrade grade = bird.getConservationGrade();
        return grade != null && grade.shouldHideLocation();
    }
}
