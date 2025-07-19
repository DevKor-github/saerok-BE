package org.devkor.apu.saerok_server.testsupport.builder;

import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;

/**
 * Builder for creating and persisting UserBirdCollection fixtures in tests.
 */
public class CollectionBuilder {
    private final TestEntityManager em;
    private User owner;
    private AccessLevelType accessLevel = AccessLevelType.PUBLIC;
    private LocalDate discoveredDate = LocalDate.now();
    private Point location = new GeometryFactory()
            .createPoint(new Coordinate(126.9780, 37.5665));

    public CollectionBuilder(TestEntityManager em) {
        this.em = em;
    }

    public CollectionBuilder owner(User owner) {
        this.owner = owner;
        return this;
    }

    public CollectionBuilder accessLevel(AccessLevelType level) {
        this.accessLevel = level;
        return this;
    }

    public CollectionBuilder discoveredDate(LocalDate date) {
        this.discoveredDate = date;
        return this;
    }

    public CollectionBuilder location(Point location) {
        this.location = location;
        return this;
    }

    /**
     * Builds and persists the UserBirdCollection.
     */
    public UserBirdCollection build() {
        UserBirdCollection coll = new UserBirdCollection();
        // inject owner
        ReflectionTestUtils.setField(coll, "user", owner);
        coll.setAccessLevel(accessLevel);
        coll.setDiscoveredDate(discoveredDate);
        coll.setLocation(location);

        em.persist(coll);
        em.flush();
        return coll;
    }
}
