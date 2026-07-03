package org.devkor.apu.saerok_server.domain.admin.dex.application;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditAction;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditLog;
import org.devkor.apu.saerok_server.domain.admin.audit.core.entity.AdminAuditTargetType;
import org.devkor.apu.saerok_server.domain.admin.audit.core.repository.AdminAuditLogRepository;
import org.devkor.apu.saerok_server.domain.admin.dex.api.dto.request.AdminBirdUpsertRequest;
import org.devkor.apu.saerok_server.domain.admin.dex.application.dto.AdminBirdListCommand;
import org.devkor.apu.saerok_server.domain.admin.dex.api.dto.response.AdminBirdDetailResponse;
import org.devkor.apu.saerok_server.domain.admin.dex.api.dto.response.AdminBirdImagePresignResponse;
import org.devkor.apu.saerok_server.domain.admin.dex.api.dto.response.AdminBirdListResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdDescription;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdHabitat;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdImage;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdName;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdTaxonomy;
import org.devkor.apu.saerok_server.domain.dex.bird.core.enums.HabitatType;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdHabitatRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdImageRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.query.repository.BirdProfileViewRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.query.view.BirdProfileView;
import org.devkor.apu.saerok_server.domain.dex.residency.entity.BirdResidency;
import org.devkor.apu.saerok_server.domain.dex.residency.entity.RarityType;
import org.devkor.apu.saerok_server.domain.dex.residency.entity.RarityTypeEntity;
import org.devkor.apu.saerok_server.domain.dex.residency.entity.ResidencyType;
import org.devkor.apu.saerok_server.domain.dex.residency.entity.ResidencyTypeEntity;
import org.devkor.apu.saerok_server.domain.dex.residency.repository.BirdResidencyRepository;
import org.devkor.apu.saerok_server.domain.dex.residency.repository.RarityTypeRepository;
import org.devkor.apu.saerok_server.domain.dex.residency.repository.ResidencyTypeRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.ConflictException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminBirdService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final Map<String, String> IMAGE_EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/jpg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif"
    );

    private final BirdRepository birdRepository;
    private final BirdImageRepository birdImageRepository;
    private final BirdHabitatRepository birdHabitatRepository;
    private final BirdResidencyRepository birdResidencyRepository;
    private final ResidencyTypeRepository residencyTypeRepository;
    private final RarityTypeRepository rarityTypeRepository;
    private final BirdProfileViewRepository birdProfileViewRepository;
    private final ImageService imageService;
    private final ImageDomainService imageDomainService;
    private final UserRepository userRepository;
    private final AdminAuditLogRepository adminAuditLogRepository;
    private final PlatformTransactionManager transactionManager;

    @Transactional(readOnly = true)
    public AdminBirdListResponse listBirds(AdminBirdListCommand command) {
        int normalizedPage = normalizePage(command.page());
        int normalizedSize = normalizeSize(command.size());
        String query = command.q();
        long totalElements = birdProfileViewRepository.countAdmin(query);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / normalizedSize);

        List<AdminBirdListResponse.Item> birds = birdProfileViewRepository
                .findAdminPage(query, normalizedPage, normalizedSize)
                .stream()
                .map(this::toListItem)
                .toList();

        return new AdminBirdListResponse(birds, normalizedPage, normalizedSize, totalElements, totalPages);
    }

    @Transactional(readOnly = true)
    public AdminBirdDetailResponse getBird(Long birdId) {
        BirdProfileView view = birdProfileViewRepository.findById(birdId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 도감이 존재하지 않아요."));
        return toDetailResponse(view);
    }

    public AdminBirdDetailResponse createBird(Long adminUserId, AdminBirdUpsertRequest request) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        Long birdId = transaction.execute(status -> persistBird(adminUserId, request));
        if (birdId == null) {
            throw new IllegalStateException("도감 등록 트랜잭션 결과가 없습니다.");
        }

        birdProfileViewRepository.refreshMaterializedView();
        return getBird(birdId);
    }

    public AdminBirdDetailResponse updateBird(Long adminUserId, Long birdId, AdminBirdUpsertRequest request) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        List<String> removedObjectKeys = transaction.execute(status -> applyUpdate(adminUserId, birdId, request));

        birdProfileViewRepository.refreshMaterializedView();
        if (removedObjectKeys != null && !removedObjectKeys.isEmpty()) {
            imageService.deleteAll(removedObjectKeys);
        }
        return getBird(birdId);
    }

    private List<String> applyUpdate(Long adminUserId, Long birdId, AdminBirdUpsertRequest request) {
        validateBirdRequest(request);
        Bird bird = birdRepository.findById(birdId)
                .orElseThrow(() -> new NotFoundException("해당 ID의 도감이 존재하지 않아요."));

        String scientificName = requireText(request.name().scientificName(), "학명을 입력해 주세요.");
        if (birdRepository.existsActiveByScientificNameExcludingId(scientificName, birdId)) {
            throw new ConflictException("이미 등록된 학명입니다: " + scientificName);
        }

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("관리자 정보를 찾을 수 없어요."));

        validateImages(request.images());

        Map<String, Object> previous = new LinkedHashMap<>();
        previous.put("koreanName", bird.getName().getKoreanName());
        previous.put("scientificName", bird.getName().getScientificName());
        previous.put("conservationGrade", bird.getConservationGrade());

        List<String> oldObjectKeys = birdImageRepository.findObjectKeysByBirdId(birdId);

        bird.update(
                toName(request.name()),
                toTaxonomy(request.taxonomy()),
                toDescription(request.description()),
                request.bodyLengthCm(),
                optionalHttpUrl(request.nibrUrl(), "NIBR URL은 http 또는 https URL이어야 합니다."),
                request.conservationGrade()
        );

        deleteBirdChildren(birdId);

        persistHabitats(bird, request);
        persistResidencies(bird, request);
        persistImages(bird, request);

        recordAudit(admin, bird, request, AdminAuditAction.BIRD_UPDATED, Map.of("previous", previous));

        List<String> newObjectKeys = request.images().stream()
                .map(image -> requireText(image.objectKey(), "이미지를 업로드해 주세요."))
                .toList();
        return oldObjectKeys.stream()
                .filter(key -> !newObjectKeys.contains(key))
                .distinct()
                .toList();
    }

    private void deleteBirdChildren(Long birdId) {
        birdImageRepository.deleteByBirdId(birdId);
        birdResidencyRepository.deleteByBirdId(birdId);
        birdHabitatRepository.deleteByBirdId(birdId);
    }

    private Long persistBird(Long adminUserId, AdminBirdUpsertRequest request) {
        validateBirdRequest(request);
        String scientificName = requireText(request.name().scientificName(), "학명을 입력해 주세요.");
        if (birdRepository.existsActiveByScientificName(scientificName)) {
            throw new ConflictException("이미 등록된 학명입니다: " + scientificName);
        }

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new NotFoundException("관리자 정보를 찾을 수 없어요."));

        validateImages(request.images());
        Bird bird = Bird.create(
                toName(request.name()),
                toTaxonomy(request.taxonomy()),
                toDescription(request.description()),
                request.bodyLengthCm(),
                optionalHttpUrl(request.nibrUrl(), "NIBR URL은 http 또는 https URL이어야 합니다."),
                request.conservationGrade()
        );

        birdRepository.save(bird);

        persistHabitats(bird, request);
        persistResidencies(bird, request);
        persistImages(bird, request);

        recordAudit(admin, bird, request, AdminAuditAction.BIRD_CREATED, null);
        return bird.getId();
    }

    private void validateBirdRequest(AdminBirdUpsertRequest request) {
        if (request == null) {
            throw new BadRequestException("도감 정보를 입력해 주세요.");
        }
        if (request.name() == null) {
            throw new BadRequestException("이름 정보를 입력해 주세요.");
        }
        if (request.taxonomy() == null) {
            throw new BadRequestException("분류 정보를 입력해 주세요.");
        }
        if (request.conservationGrade() == null) {
            throw new BadRequestException("보호등급을 선택해 주세요.");
        }
        if (request.bodyLengthCm() != null && request.bodyLengthCm() <= 0) {
            throw new BadRequestException("체장은 0보다 커야 합니다.");
        }
    }

    public AdminBirdImagePresignResponse generateImagePresignUrl(String contentType) {
        String normalizedContentType = normalizeContentType(contentType);
        String extension = IMAGE_EXTENSIONS.get(normalizedContentType);
        if (extension == null) {
            throw new BadRequestException("지원하지 않는 이미지 타입입니다.");
        }

        String objectKey = "raw/" + UUID.randomUUID() + "." + extension;
        String presignedUrl = imageService.generateUploadUrl(objectKey, normalizedContentType, 10);
        return new AdminBirdImagePresignResponse(presignedUrl, objectKey);
    }

    private void persistHabitats(Bird bird, AdminBirdUpsertRequest request) {
        LinkedHashSet<HabitatType> uniqueHabitats = new LinkedHashSet<>();
        if (request.habitats() != null) {
            request.habitats().stream()
                    .filter(Objects::nonNull)
                    .forEach(uniqueHabitats::add);
        }
        if (uniqueHabitats.isEmpty()) {
            throw new BadRequestException("서식지를 하나 이상 선택해 주세요.");
        }
        uniqueHabitats.stream()
                .filter(Objects::nonNull)
                .forEach(habitatType -> birdHabitatRepository.save(BirdHabitat.of(bird, habitatType)));
    }

    private void persistResidencies(Bird bird, AdminBirdUpsertRequest request) {
        if (request.residencies() == null || request.residencies().isEmpty()) {
            throw new BadRequestException("체류/희귀도 정보를 하나 이상 입력해 주세요.");
        }
        for (AdminBirdUpsertRequest.Residency residency : request.residencies()) {
            if (residency == null) {
                throw new BadRequestException("체류/희귀도 정보를 입력해 주세요.");
            }
            ResidencyTypeEntity residencyType = loadResidencyType(residency.residencyType());
            RarityTypeEntity rarityType = loadRarityType(residency.rarity());
            birdResidencyRepository.save(BirdResidency.of(bird, residencyType, rarityType, normalizeMonthBitmask(residency.monthBitmask())));
        }
    }

    private void persistImages(Bird bird, AdminBirdUpsertRequest request) {
        int orderIndex = 0;
        for (AdminBirdUpsertRequest.Image image : request.images()) {
            String objectKey = requireText(image.objectKey(), "이미지를 업로드해 주세요.");
            String originalUrl = requireHttpUrl(image.originalUrl(), "이미지 원본 출처 URL은 http 또는 https URL이어야 합니다.");
            birdImageRepository.save(BirdImage.of(bird, objectKey, originalUrl, orderIndex, orderIndex == 0));
            orderIndex++;
        }
    }

    private void validateImages(List<AdminBirdUpsertRequest.Image> images) {
        if (images == null || images.isEmpty()) {
            throw new BadRequestException("대표 이미지를 업로드해 주세요.");
        }

        LinkedHashSet<String> uniqueObjectKeys = new LinkedHashSet<>();
        for (AdminBirdUpsertRequest.Image image : images) {
            if (image == null) {
                throw new BadRequestException("이미지 정보를 입력해 주세요.");
            }
            String objectKey = requireText(image.objectKey(), "이미지를 업로드해 주세요.");
            requireHttpUrl(image.originalUrl(), "이미지 원본 출처 URL은 http 또는 https URL이어야 합니다.");
            if (!uniqueObjectKeys.add(objectKey)) {
                throw new BadRequestException("같은 이미지를 중복으로 등록할 수 없어요.");
            }
            if (!imageService.exists(objectKey)) {
                throw new BadRequestException("이미지 업로드가 완료되지 않았습니다.");
            }
        }
    }

    private ResidencyTypeEntity loadResidencyType(ResidencyType residencyType) {
        if (residencyType == null) {
            throw new BadRequestException("체류 형태를 선택해 주세요.");
        }
        return residencyTypeRepository.findByCode(residencyType)
                .orElseThrow(() -> new BadRequestException("체류 형태 값이 유효하지 않아요."));
    }

    private RarityTypeEntity loadRarityType(RarityType rarityType) {
        if (rarityType == null) {
            throw new BadRequestException("희귀도를 선택해 주세요.");
        }
        return rarityTypeRepository.findByCode(rarityType)
                .orElseThrow(() -> new BadRequestException("희귀도 값이 유효하지 않아요."));
    }

    private BirdName toName(AdminBirdUpsertRequest.Name request) {
        BirdName name = new BirdName();
        name.setKoreanName(requireText(request.koreanName(), "국문명을 입력해 주세요."));
        name.setScientificName(requireText(request.scientificName(), "학명을 입력해 주세요."));
        name.setScientificAuthor(trimToNull(request.scientificAuthor()));
        name.setScientificYear(request.scientificYear());
        return name;
    }

    private BirdTaxonomy toTaxonomy(AdminBirdUpsertRequest.Taxonomy request) {
        BirdTaxonomy taxonomy = new BirdTaxonomy();
        taxonomy.setPhylumEng(requireText(request.phylumEng(), "문 영문명을 입력해 주세요."));
        taxonomy.setPhylumKor(requireText(request.phylumKor(), "문 국문명을 입력해 주세요."));
        taxonomy.setClassEng(requireText(request.classEng(), "강 영문명을 입력해 주세요."));
        taxonomy.setClassKor(requireText(request.classKor(), "강 국문명을 입력해 주세요."));
        taxonomy.setOrderEng(requireText(request.orderEng(), "목 영문명을 입력해 주세요."));
        taxonomy.setOrderKor(requireText(request.orderKor(), "목 국문명을 입력해 주세요."));
        taxonomy.setFamilyEng(requireText(request.familyEng(), "과 영문명을 입력해 주세요."));
        taxonomy.setFamilyKor(requireText(request.familyKor(), "과 국문명을 입력해 주세요."));
        taxonomy.setGenusEng(requireText(request.genusEng(), "속 영문명을 입력해 주세요."));
        taxonomy.setGenusKor(requireText(request.genusKor(), "속 국문명을 입력해 주세요."));
        taxonomy.setSpeciesEng(requireText(request.speciesEng(), "종 영문명을 입력해 주세요."));
        taxonomy.setSpeciesKor(requireText(request.speciesKor(), "종 국문명을 입력해 주세요."));
        return taxonomy;
    }

    private BirdDescription toDescription(AdminBirdUpsertRequest.Description request) {
        BirdDescription description = new BirdDescription();
        if (request == null) {
            return description;
        }
        description.setDescription(trimToNull(request.description()));
        description.setSource(trimToNull(request.source()));
        description.setIsAiGenerated(request.isAiGenerated());
        return description;
    }

    private AdminBirdListResponse.Item toListItem(BirdProfileView view) {
        return new AdminBirdListResponse.Item(
                view.getId(),
                view.getName().getKoreanName(),
                view.getName().getScientificName(),
                view.getConservationGrade(),
                view.getBodyLengthCm(),
                view.getHabitats() == null ? List.of() : view.getHabitats(),
                representativeImageUrl(view),
                view.getUpdatedAt()
        );
    }

    private AdminBirdDetailResponse toDetailResponse(BirdProfileView view) {
        var name = view.getName();
        var taxonomy = view.getTaxonomy();
        var description = view.getDescription();

        return new AdminBirdDetailResponse(
                view.getId(),
                new AdminBirdDetailResponse.BirdName(
                        name.getKoreanName(),
                        name.getScientificName(),
                        name.getScientificAuthor(),
                        name.getScientificYear()
                ),
                new AdminBirdDetailResponse.BirdTaxonomy(
                        taxonomy.getPhylumEng(),
                        taxonomy.getPhylumKor(),
                        taxonomy.getClassEng(),
                        taxonomy.getClassKor(),
                        taxonomy.getOrderEng(),
                        taxonomy.getOrderKor(),
                        taxonomy.getFamilyEng(),
                        taxonomy.getFamilyKor(),
                        taxonomy.getGenusEng(),
                        taxonomy.getGenusKor(),
                        taxonomy.getSpeciesEng(),
                        taxonomy.getSpeciesKor()
                ),
                new AdminBirdDetailResponse.BirdDescription(
                        description == null ? null : description.getDescription(),
                        description == null ? null : description.getSource(),
                        description == null ? null : description.getIsAiGenerated()
                ),
                view.getBodyLengthCm(),
                view.getNibrUrl(),
                view.getConservationGrade(),
                view.getHabitats() == null ? List.of() : view.getHabitats(),
                findResidencies(view.getId()),
                view.getSeasonsWithRarity() == null ? List.of() : view.getSeasonsWithRarity().stream()
                        .map(season -> new AdminBirdDetailResponse.SeasonWithRarity(
                                season.getSeason(),
                                season.getRarity(),
                                season.getPriority()
                        ))
                        .toList(),
                view.getImages() == null ? List.of() : view.getImages().stream()
                        .map(image -> new AdminBirdDetailResponse.Image(
                                image.getObjectKey(),
                                image.getObjectKey() == null ? null : imageDomainService.toDexImageUrl(image.getObjectKey()),
                                image.getOriginalUrl(),
                                image.getOrderIndex(),
                                image.getIsThumb()
                        ))
                        .toList(),
                view.getCreatedAt(),
                view.getUpdatedAt()
        );
    }

    private List<AdminBirdDetailResponse.Residency> findResidencies(Long birdId) {
        return birdResidencyRepository.findByBirdIdWithTypes(birdId)
                .stream()
                .map(residency -> {
                    Integer monthBitmask = residency.getMonthBitmask();
                    int effectiveMonthBitmask = monthBitmask != null
                            ? monthBitmask
                            : residency.getResidencyTypeEntity().getMonthBitmask();
                    return new AdminBirdDetailResponse.Residency(
                            residency.getResidencyTypeEntity().getCode(),
                            residency.getRarityTypeEntity().getCode(),
                            monthBitmask,
                            effectiveMonthBitmask
                    );
                })
                .toList();
    }

    private String representativeImageUrl(BirdProfileView view) {
        if (view.getImages() == null || view.getImages().isEmpty()) {
            return null;
        }
        return view.getImages().stream()
                .filter(image -> Boolean.TRUE.equals(image.getIsThumb()))
                .findFirst()
                .or(() -> view.getImages().stream().findFirst())
                .map(BirdProfileView.Image::getObjectKey)
                .filter(StringUtils::hasText)
                .map(imageDomainService::toDexImageUrl)
                .orElse(null);
    }

    private void recordAudit(User admin,
                             Bird bird,
                             AdminBirdUpsertRequest request,
                             AdminAuditAction action,
                             Map<String, Object> extraMetadata) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("koreanName", bird.getName().getKoreanName());
        metadata.put("scientificName", bird.getName().getScientificName());
        metadata.put("conservationGrade", bird.getConservationGrade());
        metadata.put("habitats", request.habitats());
        metadata.put("imageCount", request.images() == null ? 0 : request.images().size());
        if (extraMetadata != null) {
            metadata.putAll(extraMetadata);
        }

        adminAuditLogRepository.save(AdminAuditLog.of(
                admin,
                action,
                AdminAuditTargetType.BIRD,
                bird.getId(),
                null,
                metadata
        ));
    }

    private int normalizePage(Integer page) {
        if (page == null) {
            return 1;
        }
        if (page < 1) {
            throw new BadRequestException("page 값이 유효하지 않아요.");
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_PAGE_SIZE;
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("size 값이 유효하지 않아요.");
        }
        return size;
    }

    private String requireText(String value, String message) {
        String trimmed = trimToNull(value);
        if (trimmed == null) {
            throw new BadRequestException(message);
        }
        return trimmed;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private Integer normalizeMonthBitmask(Integer monthBitmask) {
        if (monthBitmask == null) {
            return null;
        }
        if (monthBitmask < 0 || monthBitmask > 4095) {
            throw new BadRequestException("월 비트마스크는 0 이상 4095 이하로 입력해 주세요.");
        }
        return monthBitmask;
    }

    private String normalizeContentType(String contentType) {
        String trimmed = requireText(contentType, "contentType을 입력해 주세요.");
        int semicolon = trimmed.indexOf(';');
        if (semicolon >= 0) {
            trimmed = trimmed.substring(0, semicolon);
        }
        return trimmed.trim().toLowerCase(Locale.ROOT);
    }

    private String optionalHttpUrl(String value, String message) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : requireHttpUrl(trimmed, message);
    }

    private String requireHttpUrl(String value, String message) {
        String trimmed = requireText(value, message);
        try {
            URI uri = new URI(trimmed);
            String scheme = uri.getScheme();
            if (("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                    && StringUtils.hasText(uri.getHost())) {
                return trimmed;
            }
        } catch (URISyntaxException ignored) {
            // Fall through to the domain error below.
        }
        throw new BadRequestException(message);
    }
}
