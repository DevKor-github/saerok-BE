package org.devkor.apu.saerok_server.testsupport.builder;

import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostComment;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostCommentReport;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

public class FreeBoardPostCommentReportBuilder {
    private final TestEntityManager em;
    private User reporter;
    private User reportedUser;
    private FreeBoardPostComment comment;

    public FreeBoardPostCommentReportBuilder(TestEntityManager em) {
        this.em = em;
    }

    public FreeBoardPostCommentReportBuilder reporter(User reporter) {
        this.reporter = reporter;
        return this;
    }

    public FreeBoardPostCommentReportBuilder reportedUser(User reportedUser) {
        this.reportedUser = reportedUser;
        return this;
    }

    public FreeBoardPostCommentReportBuilder comment(FreeBoardPostComment comment) {
        this.comment = comment;
        return this;
    }

    public FreeBoardPostCommentReport build() {
        FreeBoardPostCommentReport report = FreeBoardPostCommentReport.of(reporter, reportedUser, comment);
        em.persist(report);
        return report;
    }
}
