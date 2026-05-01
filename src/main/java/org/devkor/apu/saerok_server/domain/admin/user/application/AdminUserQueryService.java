package org.devkor.apu.saerok_server.domain.admin.user.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.user.api.dto.response.AdminUserListResponse;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminUserQueryService {

    private final UserRepository userRepository;

    public AdminUserListResponse listUsers(String query, int page, int size) {
        String normalizedQuery = normalizeQuery(query);
        int offset = (page - 1) * size;

        List<User> users = userRepository.findActiveNicknameUsers(normalizedQuery, offset, size);
        long totalElements = userRepository.countActiveNicknameUsers(normalizedQuery);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        List<AdminUserListResponse.Item> items = users.stream()
                .map(user -> new AdminUserListResponse.Item(user.getId(), user.getNickname()))
                .toList();

        return new AdminUserListResponse(items, page, size, totalElements, totalPages);
    }

    private String normalizeQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }
        return query.trim();
    }
}
