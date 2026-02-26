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
@Import(RoleRepository.class)
@ActiveProfiles("test")
class RoleRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired RoleRepository repo;

    private Role role(String code) {
        return Role.custom(code, code + " NAME", code + " DESC");
    }

    /* ------------------------------------------------------------------ */
    @Test @DisplayName("save / findByCode")
    void save_findByCode() {
        Role role = role("ROLE_TEST_" + System.nanoTime());
        repo.save(role);
        em.flush(); em.clear();

        Optional<Role> found = repo.findByCode(role.getCode());

        assertThat(found).isPresent();
        assertThat(found.get().getDisplayName()).isEqualTo(role.getCode() + " NAME");
        assertThat(found.get().getDescription()).isEqualTo(role.getCode() + " DESC");
        assertThat(found.get().isBuiltin()).isFalse();
    }

    @Test @DisplayName("findAll")
    void findAll_ordersByCode() {
        String suffix = String.valueOf(System.nanoTime());
        Role roleA = role("ZZ_TEST_ROLE_A_" + suffix);
        Role roleB = role("ZZ_TEST_ROLE_B_" + suffix);
        repo.save(roleB);
        repo.save(roleA);
        em.flush(); em.clear();

        List<Role> roles = repo.findAll();

        int indexA = indexOfCode(roles, roleA.getCode());
        int indexB = indexOfCode(roles, roleB.getCode());

        assertThat(indexA).isNotNegative();
        assertThat(indexB).isNotNegative();
        assertThat(indexA).isLessThan(indexB);
    }

    @Test @DisplayName("delete")
    void delete() {
        Role role = role("ROLE_DELETE_" + System.nanoTime());
        repo.save(role);
        em.flush(); em.clear();

        Role managed = repo.findByCode(role.getCode()).orElseThrow();
        repo.delete(managed);
        em.flush(); em.clear();

        Optional<Role> found = repo.findByCode(role.getCode());
        assertThat(found).isEmpty();
    }

    private int indexOfCode(List<Role> roles, String code) {
        for (int i = 0; i < roles.size(); i++) {
            if (code.equals(roles.get(i).getCode())) {
                return i;
            }
        }
        return -1;
    }
}
