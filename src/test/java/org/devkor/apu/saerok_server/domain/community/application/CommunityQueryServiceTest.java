package org.devkor.apu.saerok_server.domain.community.application;

import org.devkor.apu.saerok_server.domain.community.application.dto.CommunityQueryCommand;
import org.devkor.apu.saerok_server.domain.community.core.repository.CommunityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CommunityQueryServiceTest {

    CommunityQueryService communityQueryService;

    @Mock CommunityRepository communityRepository;
    @Mock CommunityDataAssembler dataAssembler;

    @BeforeEach
    void setUp() {
        communityQueryService = new CommunityQueryService(
                communityRepository,
                dataAssembler
        );
    }

    @Test
    @DisplayName("페이지네이션에서 page나 size 중 하나만 null인 경우 유효하지 않음")
    void invalidPagination_isNotValid() {
        // Given
        CommunityQueryCommand pageOnlyCommand = new CommunityQueryCommand(1, null, "새");
        CommunityQueryCommand sizeOnlyCommand = new CommunityQueryCommand(null, 10, "새");
        
        // When & Then
        assertFalse(pageOnlyCommand.hasValidPagination());
        assertFalse(sizeOnlyCommand.hasValidPagination());
    }

    @Test
    @DisplayName("Repository에서 RuntimeException 발생 시 예외가 전파됨")
    void repositoryException_isPropagated() {
        // Given
        Long userId = 1L;
        RuntimeException repositoryException = new RuntimeException("Database connection failed");
        
        given(communityRepository.findRecentPublicCollections(org.mockito.ArgumentMatchers.any()))
                .willThrow(repositoryException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> communityQueryService.getCommunityMain(userId));
        
        assertEquals("Database connection failed", exception.getMessage());
        verifyNoInteractions(dataAssembler);
    }

    @Test
    @DisplayName("DataAssembler에서 예외 발생 시 예외가 전파됨")
    void dataAssemblerException_isPropagated() {
        // Given
        Long userId = 1L;
        
        given(communityRepository.findRecentPublicCollections(org.mockito.ArgumentMatchers.any()))
                .willReturn(List.of());
        given(communityRepository.findPopularCollections(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyInt()))
                .willReturn(List.of());
        given(communityRepository.findPendingBirdIdCollections(org.mockito.ArgumentMatchers.any()))
                .willReturn(List.of());
        
        RuntimeException assemblerException = new RuntimeException("Data assembly failed");
        given(dataAssembler.toCollectionInfos(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .willThrow(assemblerException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> communityQueryService.getCommunityMain(userId));
        
        assertEquals("Data assembly failed", exception.getMessage());
    }
}
