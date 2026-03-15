package org.devkor.apu.saerok_server.testsupport.builder;

import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPost;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostComment;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

public class FreeBoardPostCommentBuilder {
    private final TestEntityManager em;
    private User user;
    private FreeBoardPost post;
    private String content = "테스트 댓글 내용";
    private FreeBoardPostComment parent;

    public FreeBoardPostCommentBuilder(TestEntityManager em) {
        this.em = em;
    }

    public FreeBoardPostCommentBuilder user(User user) {
        this.user = user;
        return this;
    }

    public FreeBoardPostCommentBuilder post(FreeBoardPost post) {
        this.post = post;
        return this;
    }

    public FreeBoardPostCommentBuilder content(String content) {
        this.content = content;
        return this;
    }

    public FreeBoardPostCommentBuilder parent(FreeBoardPostComment parent) {
        this.parent = parent;
        return this;
    }

    public FreeBoardPostComment build() {
        FreeBoardPostComment comment = parent != null
                ? FreeBoardPostComment.of(user, post, content, parent)
                : FreeBoardPostComment.of(user, post, content);
        em.persist(comment);
        return comment;
    }
}
