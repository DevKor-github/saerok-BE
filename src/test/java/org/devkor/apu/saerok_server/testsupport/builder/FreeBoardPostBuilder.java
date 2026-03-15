package org.devkor.apu.saerok_server.testsupport.builder;

import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPost;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

public class FreeBoardPostBuilder {
    private final TestEntityManager em;
    private User user;
    private String content = "테스트 게시글 내용";

    public FreeBoardPostBuilder(TestEntityManager em) {
        this.em = em;
    }

    public FreeBoardPostBuilder user(User user) {
        this.user = user;
        return this;
    }

    public FreeBoardPostBuilder content(String content) {
        this.content = content;
        return this;
    }

    public FreeBoardPost build() {
        FreeBoardPost post = FreeBoardPost.of(user, content);
        em.persist(post);
        return post;
    }
}
