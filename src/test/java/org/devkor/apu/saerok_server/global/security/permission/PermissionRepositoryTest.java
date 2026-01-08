package org.devkor.apu.saerok_server.global.security.permission;

import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(PermissionRepository.class)
@ActiveProfiles("test")
class PermissionRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired PermissionRepository repo;

    /* ------------------------------------------------------------------ */
    @Test @DisplayName("findByKey")
    void findByKey() {
        Optional<Permission> found = repo.findByKey(PermissionKey.ADMIN_LOGIN);
        assertThat(found).isPresent();
        assertThat(found.get().getKey()).isEqualTo(PermissionKey.ADMIN_LOGIN);
    }

    @Test @DisplayName("findAll - key asc")
    void findAll_ordersByKey() {
        List<Permission> results = repo.findAll();

        assertThat(results).isNotEmpty();
        for (int i = 1; i < results.size(); i++) {
            String prev = results.get(i - 1).getKey().name();
            String curr = results.get(i).getKey().name();
            assertThat(prev.compareTo(curr)).isLessThanOrEqualTo(0);
        }
    }

    @Test @DisplayName("findByKeys returns subset")
    void findByKeys_returnsSubset() {
        List<Permission> results = repo.findByKeys(List.of(
                PermissionKey.ADMIN_LOGIN,
                PermissionKey.ADMIN_AD_READ
        ));

        assertThat(results).extracting(Permission::getKey)
                .containsExactly(PermissionKey.ADMIN_AD_READ, PermissionKey.ADMIN_LOGIN);
    }

    @Test @DisplayName("findByKeys - empty input")
    void findByKeys_emptyInput() {
        List<Permission> results = repo.findByKeys(List.of());
        assertThat(results).isEmpty();
    }
}
