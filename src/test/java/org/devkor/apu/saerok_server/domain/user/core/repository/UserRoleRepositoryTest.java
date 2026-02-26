package org.devkor.apu.saerok_server.domain.user.core.repository;

import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRole;
import org.devkor.apu.saerok_server.global.security.permission.Role;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.devkor.apu.saerok_server.testsupport.builder.UserBuilder;
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
@Import(UserRoleRepository.class)
@ActiveProfiles("test")
class UserRoleRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired UserRoleRepository repo;

    private User user() {
        return new UserBuilder(em).build();
    }

    private Role role(String code) {
        Role role = Role.custom(code, code + " NAME", code + " DESC");
        em.persist(role);
        return role;
    }

    private UserRole userRole(User user, Role role) {
        UserRole userRole = UserRole.createUserRole(user, role);
        repo.save(userRole);
        return userRole;
    }

    /* ------------------------------------------------------------------ */
    @Test @DisplayName("findByUser")
    void findByUser_ordersByRoleCode() {
        User user = user();
        Role roleB = role("ROLE_B");
        Role roleA = role("ROLE_A");
        userRole(user, roleB);
        userRole(user, roleA);
        em.flush(); em.clear();

        User managed = em.find(User.class, user.getId());
        List<UserRole> roles = repo.findByUser(managed);

        assertThat(roles).hasSize(2);
        assertThat(roles.get(0).getRole().getCode()).isEqualTo("ROLE_A");
        assertThat(roles.get(1).getRole().getCode()).isEqualTo("ROLE_B");
    }

    @Test @DisplayName("existsByRole / existsByUserIdAndRoleCode")
    void existsByRole_and_existsByUserIdAndRoleCode() {
        User user = user();
        Role roleA = role("ROLE_EXISTS_A");
        Role roleB = role("ROLE_EXISTS_B");
        userRole(user, roleA);
        em.flush(); em.clear();

        Role managedRoleA = em.find(Role.class, roleA.getId());
        Role managedRoleB = em.find(Role.class, roleB.getId());

        assertThat(repo.existsByRole(managedRoleA)).isTrue();
        assertThat(repo.existsByRole(managedRoleB)).isFalse();
        assertThat(repo.existsByUserIdAndRoleCode(user.getId(), "ROLE_EXISTS_A")).isTrue();
        assertThat(repo.existsByUserIdAndRoleCode(user.getId(), "ROLE_EXISTS_B")).isFalse();
    }

    @Test @DisplayName("findByUserIdAndRoleCode")
    void findByUserIdAndRoleCode() {
        User user = user();
        Role role = role("ROLE_FIND");
        userRole(user, role);
        em.flush(); em.clear();

        Optional<UserRole> found = repo.findByUserIdAndRoleCode(user.getId(), "ROLE_FIND");
        Optional<UserRole> missing = repo.findByUserIdAndRoleCode(user.getId(), "ROLE_MISSING");

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(found.get().getRole().getCode()).isEqualTo("ROLE_FIND");
        assertThat(missing).isEmpty();
    }

    @Test @DisplayName("deleteByUserId")
    void deleteByUserId_removesAll() {
        User user = user();
        Role roleA = role("ROLE_DELETE_A");
        Role roleB = role("ROLE_DELETE_B");
        userRole(user, roleA);
        userRole(user, roleB);

        User other = user();
        Role otherRole = role("ROLE_OTHER");
        userRole(other, otherRole);
        em.flush(); em.clear();

        int deleted = repo.deleteByUserId(user.getId());
        em.flush(); em.clear();

        assertThat(deleted).isEqualTo(2);
        assertThat(repo.findByUserIdAndRoleCode(user.getId(), "ROLE_DELETE_A")).isEmpty();
        assertThat(repo.findByUserIdAndRoleCode(user.getId(), "ROLE_DELETE_B")).isEmpty();
        assertThat(repo.findByUserIdAndRoleCode(other.getId(), "ROLE_OTHER")).isPresent();
    }

    @Test @DisplayName("delete")
    void delete_removesSingleRole() {
        User user = user();
        Role role = role("ROLE_DELETE_ONE");
        UserRole userRole = userRole(user, role);
        em.flush(); em.clear();

        UserRole managed = em.find(UserRole.class, userRole.getId());
        repo.delete(managed);
        em.flush(); em.clear();

        Optional<UserRole> found = repo.findByUserIdAndRoleCode(user.getId(), "ROLE_DELETE_ONE");
        assertThat(found).isEmpty();
    }

    @Test @DisplayName("findUserIdsByRoleCode - 삭제된 유저 제외")
    void findUserIdsByRoleCode_excludesDeletedUsers() {
        Role role = role("ROLE_ACTIVE_ONLY");
        User active1 = user();
        User active2 = user();
        User deleted = user();
        userRole(active1, role);
        userRole(active2, role);
        userRole(deleted, role);
        em.flush();

        deleted.softDelete();
        em.flush(); em.clear();

        List<Long> userIds = repo.findUserIdsByRoleCode("ROLE_ACTIVE_ONLY");

        assertThat(userIds).containsExactly(active1.getId(), active2.getId());
    }
}
