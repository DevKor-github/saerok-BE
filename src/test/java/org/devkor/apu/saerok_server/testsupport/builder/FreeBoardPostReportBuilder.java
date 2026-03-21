package org.devkor.apu.saerok_server.testsupport.builder;

import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPost;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostReport;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.util.ReflectionTestUtils;

public class FreeBoardPostReportBuilder {
    private final TestEntityManager em;
    private User reporter;
    private User reportedUser;
    private FreeBoardPost post;

    public FreeBoardPostReportBuilder(TestEntityManager em) {
        this.em = em;
    }

    public FreeBoardPostReportBuilder reporter(User reporter) {
        this.reporter = reporter;
        return this;
    }

    public FreeBoardPostReportBuilder reportedUser(User reportedUser) {
        this.reportedUser = reportedUser;
        return this;
    }

    public FreeBoardPostReportBuilder post(FreeBoardPost post) {
        this.post = post;
        return this;
    }

    public FreeBoardPostReport build() {
        FreeBoardPostReport report = FreeBoardPostReport.of(reporter, reportedUser, post);
        em.persist(report);
        return report;
    }
}
