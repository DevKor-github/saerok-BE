package org.devkor.apu.saerok_server.domain.collection.core.entity;

import jakarta.persistence.*;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.entity.Auditable;
import org.locationtech.jts.geom.Point;

import java.time.LocalDate;

@Entity
public class UserBirdCollection extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "bird_id", nullable = true)
    private Bird bird;

    @Column(name = "temp_bird_name")
    private String tempBirdName;

    @Column(name = "discovered_date", nullable = false)
    private LocalDate discoveredDate;

    @Column(name = "location", columnDefinition = "geometry(Point,4326)", nullable = false)
    private Point location;

    @Column(name = "location_alias")
    private String locationAlias;

    @Column(name = "note")
    private String note;

    @Column(name = "is_pinned")
    private boolean isPinned;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false)
    private AccessLevelType accessLevel;
}
