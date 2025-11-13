package org.devkor.apu.saerok_server.global.shared.image;

/**
 * 이미지 도메인 상의 '종류'를 명시적으로 표현.
 * - USER_COLLECTION_IMAGE: 새록 이미지 (UserBirdCollectionImage)
 * - USER_PROFILE_IMAGE: 사용자 프로필 이미지 (UserProfileImage)
 * - AD_IMAGE: 광고 배너 이미지 (Ad.objectKey)
 * - DEX_BIRD_IMAGE: 도감 이미지 (BirdImage)
 */
public enum ImageKind {
    USER_COLLECTION_IMAGE,
    USER_PROFILE_IMAGE,
    AD_IMAGE,
    DEX_BIRD_IMAGE
}
