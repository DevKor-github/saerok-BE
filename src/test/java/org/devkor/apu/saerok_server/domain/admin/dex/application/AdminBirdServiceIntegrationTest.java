package org.devkor.apu.saerok_server.domain.admin.dex.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditAction;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditTargetType;
import org.devkor.apu.saerok_server.domain.admin.audit.core.repository.AdminAuditLogRepository;
import org.devkor.apu.saerok_server.domain.admin.dex.api.dto.request.AdminBirdUpsertRequest;
import org.devkor.apu.saerok_server.domain.admin.dex.api.dto.response.AdminBirdDetailResponse;
import org.devkor.apu.saerok_server.domain.admin.dex.api.dto.response.AdminBirdImagePresignResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.core.enums.ConservationGrade;
import org.devkor.apu.saerok_server.domain.dex.bird.core.enums.HabitatType;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdHabitatRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdImageRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.query.repository.BirdProfileViewRepository;
import org.devkor.apu.saerok_server.domain.dex.residency.repository.BirdResidencyRepository;
import org.devkor.apu.saerok_server.domain.dex.residency.repository.RarityTypeRepository;
import org.devkor.apu.saerok_server.domain.dex.residency.repository.ResidencyTypeRepository;
import org.devkor.apu.saerok_server.domain.dex.residency.entity.RarityType;
import org.devkor.apu.saerok_server.domain.dex.residency.entity.ResidencyType;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.ConflictException;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@DataJpaTest
@Import({
        AdminBirdService.class,
        BirdRepository.class,
        BirdImageRepository.class,
        BirdHabitatRepository.class,
        BirdResidencyRepository.class,
        ResidencyTypeRepository.class,
        RarityTypeRepository.class,
        BirdProfileViewRepository.class,
        UserRepository.class,
        AdminAuditLogRepository.class,
        ImageDomainService.class,
        AdminBirdServiceIntegrationTest.ImageServiceTestConfig.class
})
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "aws.cloudfront.dex-image-domain=https://dex-cdn.example",
        "aws.cloudfront.upload-image-domain=https://upload-cdn.example"
})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class AdminBirdServiceIntegrationTest extends AbstractPostgresContainerTest {

    @Autowired
    AdminBirdService service;

    @Autowired
    AdminAuditLogRepository adminAuditLogRepository;

    @Autowired
    TestEntityManager testEm;

    @Autowired
    TestImageService imageService;

    @Autowired
    BirdProfileViewRepository birdProfileViewRepository;

    @Autowired
    PlatformTransactionManager transactionManager;

    private final Set<Long> createdAdminUserIds = new HashSet<>();

    @AfterEach
    void cleanUpCommittedFixtures() {
        boolean[] deletedBird = {false};
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            EntityManager entityManager = testEm.getEntityManager();
            for (Long adminUserId : createdAdminUserIds) {
                @SuppressWarnings("unchecked")
                List<Number> birdIds = entityManager.createNativeQuery("""
                                SELECT target_id
                                FROM admin_audit_log
                                WHERE admin_user_id = :adminUserId
                                  AND target_type = 'BIRD'
                                  AND target_id IS NOT NULL
                                """)
                        .setParameter("adminUserId", adminUserId)
                        .getResultList();
                for (Number birdId : birdIds) {
                    deleteBirdRelations(entityManager, birdId.longValue());
                    deletedBird[0] = true;
                }
                entityManager.createNativeQuery("DELETE FROM admin_audit_log WHERE admin_user_id = :adminUserId")
                        .setParameter("adminUserId", adminUserId)
                        .executeUpdate();
                entityManager.createNativeQuery("DELETE FROM users WHERE id = :adminUserId")
                        .setParameter("adminUserId", adminUserId)
                        .executeUpdate();
            }
        });
        if (deletedBird[0]) {
            birdProfileViewRepository.refreshMaterializedView();
        }
        createdAdminUserIds.clear();
        imageService.reset();
    }

    @Test
    void createBird_persistsBirdRelationsRefreshesMaterializedViewAndRecordsAudit() {
        Long adminUserId = persistAdminUser();
        String scientificName = uniqueScientificName();
        String objectKey = issueUploadedImage();

        AdminBirdDetailResponse response = service.createBird(adminUserId, createRequest(scientificName, objectKey));

        assertThat(response.id()).isNotNull();
        assertThat(response.name().scientificName()).isEqualTo(scientificName);
        assertThat(response.habitats()).containsExactly(HabitatType.RIVER_LAKE);
        assertThat(response.images()).hasSize(1);
        assertThat(response.images().getFirst().objectKey()).isEqualTo(objectKey);
        assertThat(response.images().getFirst().imageUrl()).isEqualTo("https://dex-cdn.example/" + objectKey);
        assertThat(response.residencies()).singleElement().satisfies(residency -> {
            assertThat(residency.residencyType()).isEqualTo(ResidencyType.RESIDENT);
            assertThat(residency.rarity()).isEqualTo(RarityType.COMMON);
            assertThat(residency.monthBitmask()).isEqualTo(4095);
            assertThat(residency.effectiveMonthBitmask()).isEqualTo(4095);
        });
        assertThat(countRows("bird_habitat", response.id())).isEqualTo(1L);
        assertThat(countRows("bird_residency", response.id())).isEqualTo(1L);
        assertThat(countRows("bird_image", response.id())).isEqualTo(1L);

        var auditLogs = adminAuditLogRepository.findAllOrderByCreatedAtDesc();
        assertThat(auditLogs).anySatisfy(log -> {
            assertThat(log.getAction()).isEqualTo(AdminAuditAction.BIRD_CREATED);
            assertThat(log.getTargetType()).isEqualTo(AdminAuditTargetType.BIRD);
            assertThat(log.getTargetId()).isEqualTo(response.id());
        });
    }

    @Test
    void updateBird_replacesRelationsAndImagesDeletesOldObjectAndRecordsAudit() {
        Long adminUserId = persistAdminUser();
        String scientificName = uniqueScientificName();
        String oldObjectKey = issueUploadedImage();
        AdminBirdDetailResponse created = service.createBird(adminUserId, createRequest(scientificName, oldObjectKey));

        String newObjectKey = issueUploadedImage();
        AdminBirdUpsertRequest updateRequest = createRequest(
                scientificName, // 자기 자신은 학명 중복으로 막히면 안 됨
                List.of(HabitatType.FOREST),
                List.of(new AdminBirdUpsertRequest.Residency(ResidencyType.SUMMER, RarityType.RARE, 4095)),
                List.of(new AdminBirdUpsertRequest.Image(newObjectKey, "https://source.example/updated"))
        );

        AdminBirdDetailResponse updated = service.updateBird(adminUserId, created.id(), updateRequest);

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.habitats()).containsExactly(HabitatType.FOREST);
        assertThat(updated.images()).singleElement().satisfies(image -> {
            assertThat(image.objectKey()).isEqualTo(newObjectKey);
            assertThat(image.originalUrl()).isEqualTo("https://source.example/updated");
        });
        assertThat(updated.residencies()).singleElement().satisfies(residency -> {
            assertThat(residency.residencyType()).isEqualTo(ResidencyType.SUMMER);
            assertThat(residency.rarity()).isEqualTo(RarityType.RARE);
        });
        // 관계는 append가 아니라 전량 교체되어야 함
        assertThat(countRows("bird_habitat", created.id())).isEqualTo(1L);
        assertThat(countRows("bird_residency", created.id())).isEqualTo(1L);
        assertThat(countRows("bird_image", created.id())).isEqualTo(1L);
        // 더 이상 참조되지 않는 이전 이미지 객체는 S3에서 삭제
        assertThat(imageService.deletedObjectKeys).contains(oldObjectKey);

        var auditLogs = adminAuditLogRepository.findAllOrderByCreatedAtDesc();
        assertThat(auditLogs).anySatisfy(log -> {
            assertThat(log.getAction()).isEqualTo(AdminAuditAction.BIRD_UPDATED);
            assertThat(log.getTargetType()).isEqualTo(AdminAuditTargetType.BIRD);
            assertThat(log.getTargetId()).isEqualTo(created.id());
        });
    }

    @Test
    void updateBird_rejectsDuplicateScientificNameOfAnotherBird() {
        Long adminUserId = persistAdminUser();
        String otherScientificName = uniqueScientificName();
        service.createBird(adminUserId, createRequest(otherScientificName, issueUploadedImage()));
        AdminBirdDetailResponse target = service.createBird(
                adminUserId, createRequest(uniqueScientificName(), issueUploadedImage()));

        assertThatThrownBy(() -> service.updateBird(adminUserId, target.id(),
                createRequest(otherScientificName, issueUploadedImage())))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("이미 등록된 학명입니다");
    }

    @Test
    void createBird_rejectsDuplicateScientificNameCaseInsensitively() {
        Long adminUserId = persistAdminUser();
        String scientificName = uniqueScientificName();
        service.createBird(adminUserId, createRequest(scientificName, issueUploadedImage()));

        assertThatThrownBy(() -> service.createBird(
                adminUserId,
                createRequest(" " + scientificName.toUpperCase() + " ", issueUploadedImage())))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("이미 등록된 학명입니다");
    }

    @Test
    void createBird_rejectsMissingHabitatImageAndInvalidMonthBitmask() {
        Long adminUserId = persistAdminUser();
        String objectKey = issueUploadedImage();

        assertThatThrownBy(() -> service.createBird(adminUserId, createRequest(
                uniqueScientificName(),
                List.of(),
                List.of(new AdminBirdUpsertRequest.Residency(ResidencyType.RESIDENT, RarityType.COMMON, 4095)),
                List.of(new AdminBirdUpsertRequest.Image(objectKey, "https://source.example/bird"))
        )))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("서식지");

        assertThatThrownBy(() -> service.createBird(adminUserId, createRequest(
                uniqueScientificName(),
                List.of(HabitatType.RIVER_LAKE),
                List.of(new AdminBirdUpsertRequest.Residency(ResidencyType.RESIDENT, RarityType.COMMON, 4095)),
                List.of()
        )))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("대표 이미지");

        assertThatThrownBy(() -> service.createBird(adminUserId, createRequest(
                uniqueScientificName(),
                List.of(HabitatType.RIVER_LAKE),
                List.of(new AdminBirdUpsertRequest.Residency(ResidencyType.RESIDENT, RarityType.COMMON, 4096)),
                List.of(new AdminBirdUpsertRequest.Image(objectKey, "https://source.example/bird"))
        )))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("월 비트마스크");
    }

    @Test
    void generateImagePresignUrl_returnsDexRawObjectKey() {
        AdminBirdImagePresignResponse response = service.generateImagePresignUrl("image/png; charset=binary");

        assertThat(response.presignedUrl()).startsWith("https://s3.example/raw/");
        assertThat(response.objectKey()).matches("raw/[0-9a-f\\-]{36}\\.png");
    }

    @Test
    void createBird_rejectsInvalidImageRowsUrlsAndUnissuedObjects() {
        Long adminUserId = persistAdminUser();

        List<AdminBirdUpsertRequest.Residency> nullResidency = new ArrayList<>();
        nullResidency.add(null);
        assertThatThrownBy(() -> service.createBird(adminUserId, createRequest(
                uniqueScientificName(),
                List.of(HabitatType.RIVER_LAKE),
                nullResidency,
                List.of(new AdminBirdUpsertRequest.Image(issueUploadedImage(), "https://source.example/bird"))
        )))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("체류/희귀도 정보");

        List<AdminBirdUpsertRequest.Image> nullImage = new ArrayList<>();
        nullImage.add(null);
        assertThatThrownBy(() -> service.createBird(adminUserId, createRequest(
                uniqueScientificName(),
                List.of(HabitatType.RIVER_LAKE),
                List.of(new AdminBirdUpsertRequest.Residency(ResidencyType.RESIDENT, RarityType.COMMON, null)),
                nullImage
        )))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("이미지 정보");

        assertThatThrownBy(() -> service.createBird(adminUserId, createRequest(
                uniqueScientificName(),
                List.of(HabitatType.RIVER_LAKE),
                List.of(new AdminBirdUpsertRequest.Residency(ResidencyType.RESIDENT, RarityType.COMMON, null)),
                List.of(new AdminBirdUpsertRequest.Image("raw/not-uploaded.webp", "https://source.example/bird"))
        )))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("완료되지 않았습니다");

        String objectKey = issueUploadedImage();
        assertThatThrownBy(() -> service.createBird(adminUserId, createRequest(
                uniqueScientificName(),
                List.of(HabitatType.RIVER_LAKE),
                List.of(new AdminBirdUpsertRequest.Residency(ResidencyType.RESIDENT, RarityType.COMMON, null)),
                List.of(new AdminBirdUpsertRequest.Image(objectKey, "javascript:alert(1)"))
        )))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("http 또는 https");
    }

    @Test
    void createBird_rejectsIncompleteUpload() {
        Long adminUserId = persistAdminUser();

        AdminBirdImagePresignResponse incomplete = service.generateImagePresignUrl("image/webp");
        assertThatThrownBy(() -> service.createBird(
                adminUserId,
                createRequest(uniqueScientificName(), incomplete.objectKey())))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("완료되지 않았습니다");
    }

    private Long persistAdminUser() {
        Long adminUserId = new TransactionTemplate(transactionManager).execute(status -> {
            String suffix = Long.toString(System.nanoTime());
            User admin = User.createUser("admin-" + suffix + "@saerok.app");
            admin.setNickname("운영자-" + suffix);
            testEm.persist(admin);
            testEm.flush();
            return admin.getId();
        });
        createdAdminUserIds.add(adminUserId);
        return adminUserId;
    }

    private AdminBirdUpsertRequest createRequest(String scientificName, String objectKey) {
        return createRequest(
                scientificName,
                List.of(HabitatType.RIVER_LAKE),
                List.of(new AdminBirdUpsertRequest.Residency(ResidencyType.RESIDENT, RarityType.COMMON, 4095)),
                List.of(new AdminBirdUpsertRequest.Image(objectKey, "https://source.example/bird"))
        );
    }

    private AdminBirdUpsertRequest createRequest(String scientificName,
                                                 List<HabitatType> habitats,
                                                 List<AdminBirdUpsertRequest.Residency> residencies,
                                                 List<AdminBirdUpsertRequest.Image> images) {
        return new AdminBirdUpsertRequest(
                new AdminBirdUpsertRequest.Name("통합테스트새", scientificName, "Linnaeus", 1758),
                new AdminBirdUpsertRequest.Taxonomy(
                        "Chordata",
                        "척삭동물문",
                        "Aves",
                        "조강",
                        "Pelecaniformes",
                        "사다새목",
                        "Ardeidae",
                        "왜가리과",
                        "Butorides",
                        "해오라기속",
                        "striata",
                        "통합테스트새"
                ),
                new AdminBirdUpsertRequest.Description("설명", "출처", false),
                45.0,
                "https://species.example/bird",
                ConservationGrade.NONE,
                habitats,
                residencies,
                images
        );
    }

    private long countRows(String tableName, Long birdId) {
        return new TransactionTemplate(transactionManager).execute(status -> {
            EntityManager entityManager = testEm.getEntityManager();
            Number count = (Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + tableName + " WHERE bird_id = :birdId")
                    .setParameter("birdId", birdId)
                    .getSingleResult();
            return count.longValue();
        });
    }

    private void deleteBirdRelations(EntityManager entityManager, Long birdId) {
        for (String table : List.of("bird_image", "bird_habitat", "bird_residency")) {
            entityManager.createNativeQuery("DELETE FROM " + table + " WHERE bird_id = :birdId")
                    .setParameter("birdId", birdId)
                    .executeUpdate();
        }
        entityManager.createNativeQuery("DELETE FROM bird WHERE id = :birdId")
                .setParameter("birdId", birdId)
                .executeUpdate();
    }

    private String issueUploadedImage() {
        AdminBirdImagePresignResponse response = service.generateImagePresignUrl("image/webp");
        imageService.markUploaded(response.objectKey());
        return response.objectKey();
    }

    private String uniqueScientificName() {
        return "Codex testus " + System.nanoTime();
    }

    @TestConfiguration
    static class ImageServiceTestConfig {
        @Bean
        TestImageService imageService() {
            return new TestImageService();
        }
    }

    static class TestImageService implements ImageService {
        private final Set<String> uploadedObjectKeys = new HashSet<>();
        private final Set<String> deletedObjectKeys = new HashSet<>();

        @Override
        public String generateUploadUrl(String objectKey, String contentType, long expireMinutes) {
            return "https://s3.example/" + objectKey;
        }

        @Override
        public void delete(String objectKey) {
            deletedObjectKeys.add(objectKey);
            uploadedObjectKeys.remove(objectKey);
        }

        @Override
        public void deleteAll(List<String> objectKeys) {
            deletedObjectKeys.addAll(objectKeys);
            uploadedObjectKeys.removeAll(objectKeys);
        }

        @Override
        public boolean exists(String objectKey) {
            return uploadedObjectKeys.contains(objectKey);
        }

        void markUploaded(String objectKey) {
            uploadedObjectKeys.add(objectKey);
        }

        void reset() {
            uploadedObjectKeys.clear();
            deletedObjectKeys.clear();
        }
    }
}
