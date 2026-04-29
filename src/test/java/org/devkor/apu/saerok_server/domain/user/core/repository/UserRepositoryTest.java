package org.devkor.apu.saerok_server.domain.user.core.repository;

import org.devkor.apu.saerok_server.domain.user.core.entity.SignupStatusType;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
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
@Import(UserRepository.class)
@ActiveProfiles("test")
class UserRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired UserRepository repo;

    private User user() {
        return new UserBuilder(em).build();
    }

    private User user(String email) {
        return new UserBuilder(em).email(email).build();
    }

    private User user(String email, String nickname) {
        return new UserBuilder(em).email(email).nickname(nickname).build();
    }

    /* ------------------------------------------------------------------ */
    @Test @DisplayName("save / findById")
    void save_findById() {
        User u = user("test@example.com", "testUser");
        em.flush(); em.clear();

        Optional<User> found = repo.findById(u.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getNickname()).isEqualTo("testUser");
        assertThat(found.get().getSignupStatus()).isEqualTo(SignupStatusType.PROFILE_REQUIRED);
    }

    @Test @DisplayName("findById - 삭제된 사용자는 조회되지 않음")
    void findById_notIncludeDeleted() {
        User u = user("deleted@example.com");
        em.flush();

        u.softDelete();
        em.flush(); em.clear();

        Optional<User> found = repo.findById(u.getId());
        assertThat(found).isEmpty();
    }

    @Test @DisplayName("findDeletedUserById - 삭제된 사용자만 조회")
    void findDeletedUserById() {
        User active = user("active@example.com");
        User deleted = user("deleted@example.com");
        em.flush();

        deleted.softDelete();
        em.flush(); em.clear();

        Optional<User> foundDeleted = repo.findDeletedUserById(deleted.getId());
        Optional<User> foundActive = repo.findDeletedUserById(active.getId());

        assertThat(foundDeleted).isPresent();
        assertThat(foundDeleted.get().getEmail()).isEqualTo("deleted@example.com");
        assertThat(foundActive).isEmpty();
    }

    @Test @DisplayName("findByNickname")
    void findByNickname() {
        User u1 = user("user1@example.com", "uniqueNick");
        User u2 = user("user2@example.com", "anotherNick");
        em.flush(); em.clear();

        Optional<User> found = repo.findByNickname("uniqueNick");
        Optional<User> notFound = repo.findByNickname("nonExistentNick");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("user1@example.com");
        assertThat(notFound).isEmpty();
    }

    @Test @DisplayName("findByNickname - 삭제된 사용자도 조회됨")
    void findByNickname_includeDeleted() {
        User u = user("deleted@example.com", "deletedNick");
        em.flush();

        u.softDelete();
        em.flush(); em.clear();

        Optional<User> found = repo.findByNickname("deletedNick");
        assertThat(found).isPresent();
        assertThat(found.get().getDeletedAt()).isNotNull();
    }

    @Test @DisplayName("findActiveUserIds")
    void findActiveUserIds() {
        User active1 = user("active1@example.com");
        active1.setSignupStatus(SignupStatusType.COMPLETED);
        User active2 = user("active2@example.com");
        active2.setSignupStatus(SignupStatusType.PROFILE_REQUIRED);
        User withdrawn = user("withdrawn@example.com");
        withdrawn.setSignupStatus(SignupStatusType.WITHDRAWN);
        User deleted = user("deleted@example.com");
        em.flush();

        deleted.softDelete();
        em.flush(); em.clear();

        List<Long> activeIds = repo.findActiveUserIds();

        assertThat(activeIds).hasSize(2);
        assertThat(activeIds).contains(active1.getId(), active2.getId());
        assertThat(activeIds).doesNotContain(withdrawn.getId(), deleted.getId());
    }

    @Test @DisplayName("findActiveUserIds - 빈 리스트")
    void findActiveUserIds_empty() {
        User withdrawn = user("withdrawn@example.com");
        withdrawn.setSignupStatus(SignupStatusType.WITHDRAWN);
        em.flush(); em.clear();

        List<Long> activeIds = repo.findActiveUserIds();
        assertThat(activeIds).isEmpty();
    }

    @Test @DisplayName("findActiveNicknameUsers - 활성 닉네임 사용자만 닉네임순으로 조회")
    void findActiveNicknameUsers() {
        User charlie = user("charlie@example.com", "charlie");
        charlie.setSignupStatus(SignupStatusType.COMPLETED);
        User bravo = user("bravo@example.com", "bravo");
        bravo.setSignupStatus(SignupStatusType.COMPLETED);
        User withdrawn = user("withdrawn-nickname@example.com", "alpha");
        withdrawn.setSignupStatus(SignupStatusType.WITHDRAWN);
        User deleted = user("deleted-nickname@example.com", "beta");
        User noNickname = user("no-nickname@example.com", null);
        noNickname.setSignupStatus(SignupStatusType.COMPLETED);
        User blankNickname = user("blank-nickname@example.com", "");
        blankNickname.setSignupStatus(SignupStatusType.COMPLETED);
        em.flush();

        deleted.softDelete();
        em.flush(); em.clear();

        List<User> users = repo.findActiveNicknameUsers(null, 0, 20);

        assertThat(users)
                .extracting(User::getNickname)
                .containsExactly("bravo", "charlie");
        assertThat(repo.countActiveNicknameUsers(null)).isEqualTo(2);
    }

    @Test @DisplayName("findActiveNicknameUsers - 닉네임 검색과 페이징")
    void findActiveNicknameUsers_queryAndPagination() {
        User alpha = user("alpha@example.com", "alpha");
        alpha.setSignupStatus(SignupStatusType.COMPLETED);
        User alpine = user("alpine@example.com", "alpine");
        alpine.setSignupStatus(SignupStatusType.COMPLETED);
        User bravo = user("bravo-query@example.com", "bravo");
        bravo.setSignupStatus(SignupStatusType.COMPLETED);
        em.flush(); em.clear();

        List<User> firstPage = repo.findActiveNicknameUsers("alp", 0, 1);
        List<User> secondPage = repo.findActiveNicknameUsers("alp", 1, 1);

        assertThat(firstPage)
                .extracting(User::getNickname)
                .containsExactly("alpha");
        assertThat(secondPage)
                .extracting(User::getNickname)
                .containsExactly("alpine");
        assertThat(repo.countActiveNicknameUsers("alp")).isEqualTo(2);
    }

    @Test @DisplayName("findActiveNicknameUsers - 닉네임 중간 문자열 검색")
    void findActiveNicknameUsers_containsQuery() {
        User duli = user("duli@example.com", "둘리");
        duli.setSignupStatus(SignupStatusType.COMPLETED);
        User pigeon = user("pigeon@example.com", "비둘기");
        pigeon.setSignupStatus(SignupStatusType.COMPLETED);
        User magpie = user("magpie@example.com", "까치");
        magpie.setSignupStatus(SignupStatusType.COMPLETED);
        em.flush(); em.clear();

        List<User> users = repo.findActiveNicknameUsers("둘", 0, 20);

        assertThat(users)
                .extracting(User::getNickname)
                .containsExactly("둘리", "비둘기");
        assertThat(repo.countActiveNicknameUsers("둘")).isEqualTo(2);
    }

    @Test @DisplayName("save - 중복 닉네임은 제약조건 위반")
    void save_duplicateNickname() {
        User u1 = user("user1@example.com");
        u1.setNickname("duplicateNick");
        em.flush();

        User u2 = user("user2@example.com");
        u2.setNickname("duplicateNick");

        assertThatThrownBy(() -> em.flush())
                .isInstanceOf(Exception.class);
    }

    @Test @DisplayName("anonymizeForWithdrawal")
    void anonymizeForWithdrawal() {
        User u = user("withdraw@example.com");
        u.setNickname("withdrawUser");
        em.flush();

        u.anonymizeForWithdrawal();
        em.flush(); em.clear();

        Optional<User> found = repo.findDeletedUserById(u.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isNull();
        assertThat(found.get().getPhone()).isNull();
        assertThat(found.get().getGender()).isNull();
        assertThat(found.get().getBirthDate()).isNull();
        assertThat(found.get().getDefaultProfileImageVariant()).isNull();
        assertThat(found.get().getSignupStatus()).isEqualTo(SignupStatusType.WITHDRAWN);
        assertThat(found.get().getDeletedAt()).isNotNull();
    }

    @Test @DisplayName("restoreForRejoin")
    void restoreForRejoin() {
        User u = user("rejoin@example.com");
        em.flush();

        u.anonymizeForWithdrawal();
        em.flush(); em.clear();

        User deleted = repo.findDeletedUserById(u.getId()).orElseThrow();

        deleted.restoreForRejoin();
        em.flush(); em.clear();

        Optional<User> restored = repo.findById(u.getId());
        assertThat(restored).isPresent();
        assertThat(restored.get().getDeletedAt()).isNull();
        assertThat(restored.get().getSignupStatus()).isEqualTo(SignupStatusType.PROFILE_REQUIRED);
        assertThat(restored.get().getJoinedAt()).isNotNull();
    }
}
