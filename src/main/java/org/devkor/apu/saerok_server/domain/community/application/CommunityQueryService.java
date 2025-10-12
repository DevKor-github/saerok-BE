package org.devkor.apu.saerok_server.domain.community.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.community.api.dto.response.GetCommunityCollectionsResponse;
import org.devkor.apu.saerok_server.domain.community.api.dto.response.GetCommunityMainResponse;
import org.devkor.apu.saerok_server.domain.community.api.dto.response.GetCommunitySearchResponse;
import org.devkor.apu.saerok_server.domain.community.api.dto.response.GetCommunitySearchUsersResponse;
import org.devkor.apu.saerok_server.domain.community.application.dto.CommunityQueryCommand;
import org.devkor.apu.saerok_server.domain.community.core.repository.CommunityRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommunityQueryService {

    private final CommunityRepository communityRepository;
    private final CommunityDataAssembler dataAssembler;

    public GetCommunityMainResponse getCommunityMain(Long userId) {
        // 메인 페이지용으로 각각 3개씩 조회
        CommunityQueryCommand mainCommand = new CommunityQueryCommand(1, 3, null);

        List<UserBirdCollection> recentCollections = communityRepository.findRecentPublicCollections(mainCommand);
        List<UserBirdCollection> popularCollections = communityRepository.findPopularCollections(mainCommand);
        List<UserBirdCollection> pendingCollections = communityRepository.findPendingBirdIdCollections(mainCommand);

        return new GetCommunityMainResponse(
                dataAssembler.toCollectionInfos(recentCollections, userId),
                dataAssembler.toCollectionInfos(popularCollections, userId),
                dataAssembler.toCollectionInfos(pendingCollections, userId)
        );
    }

    public GetCommunityCollectionsResponse getRecentCollections(Long userId, CommunityQueryCommand command) {
        List<UserBirdCollection> collections = communityRepository.findRecentPublicCollections(command);
        return new GetCommunityCollectionsResponse(dataAssembler.toCollectionInfos(collections, userId));
    }

    public GetCommunityCollectionsResponse getPopularCollections(Long userId, CommunityQueryCommand command) {
        List<UserBirdCollection> collections = communityRepository.findPopularCollections(command);
        return new GetCommunityCollectionsResponse(dataAssembler.toCollectionInfos(collections, userId));
    }

    public GetCommunityCollectionsResponse getPendingBirdIdCollections(Long userId, CommunityQueryCommand command) {
        List<UserBirdCollection> collections = communityRepository.findPendingBirdIdCollections(command);
        return new GetCommunityCollectionsResponse(dataAssembler.toCollectionInfos(collections, userId));
    }

    public GetCommunitySearchResponse searchAll(String query, Long userId) {
        // 검색 결과 미리보기용으로 각각 3개씩 조회
        CommunityQueryCommand searchCommand = new CommunityQueryCommand(1, 3, query);

        List<UserBirdCollection> collections = communityRepository.searchCollectionsByBirdName(searchCommand);
        long collectionsCount = communityRepository.countCollectionsByBirdName(query);

        List<User> users = communityRepository.searchUsersByNickname(searchCommand);
        long usersCount = communityRepository.countUsersByNickname(query);

        return new GetCommunitySearchResponse(
                collectionsCount,
                dataAssembler.toCollectionInfos(collections, userId),
                usersCount,
                dataAssembler.toUserInfos(users)
        );
    }

    public GetCommunityCollectionsResponse searchCollections(Long userId, CommunityQueryCommand command) {
        List<UserBirdCollection> collections = communityRepository.searchCollectionsByBirdName(command);
        return new GetCommunityCollectionsResponse(dataAssembler.toCollectionInfos(collections, userId));
    }

    public GetCommunitySearchUsersResponse searchUsers(CommunityQueryCommand command) {
        List<User> users = communityRepository.searchUsersByNickname(command);
        return new GetCommunitySearchUsersResponse(dataAssembler.toUserInfos(users));
    }
}
