package org.devkor.apu.saerok_server.domain.collection.core.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.entity.Auditable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class UserBirdCollection extends Auditable {

    public static final int NOTE_MAX_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bird_id", nullable = true)
    @Setter
    private Bird bird;

    @Column(name = "temp_bird_name")
    private String tempBirdName;

    @Column(name = "discovered_date", nullable = false)
    @Setter
    private LocalDate discoveredDate;

    @Column(name = "location", columnDefinition = "geometry(Point,4326)", nullable = false)
    @Setter
    private Point location;

    @Column(name = "location_alias")
    @Setter
    private String locationAlias;

    @Column(name = "address", length = 512)
    @Setter
    private String address;

    @Column(name = "note")
    @Setter
    private String note;

    @Column(name = "is_pinned")
    private boolean isPinned;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false)
    @Setter
    private AccessLevelType accessLevel;

    @Builder
    public UserBirdCollection(User user, Bird bird, String tempBirdName, LocalDate discoveredDate, Point location, String locationAlias, String address, String note, boolean isPinned, AccessLevelType accessLevel) {

        if (user == null) throw new IllegalArgumentException("user는 null일 수 없습니다.");
        if (discoveredDate == null) throw new IllegalArgumentException("discoveredDate는 null일 수 없습니다.");
        if (location == null) throw new IllegalArgumentException("location은 null일 수 없습니다.");

        this.user = user;
        this.bird = bird;
        this.tempBirdName = tempBirdName;
        this.discoveredDate = discoveredDate;
        this.location = location;
        this.locationAlias = locationAlias;
        this.address = address;
        this.note = note;
        this.isPinned = isPinned;
        this.accessLevel = accessLevel == null ? AccessLevelType.PUBLIC : accessLevel;
    }

    public double getLongitude() {
        return location.getX();
    }

    public double getLatitude() {
        return location.getY();
    }

    public String getBirdKoreanName() { return bird != null ? bird.getName().getKoreanName() : null; }

    public String getBirdScientificName() { return bird != null ? bird.getName().getScientificName() : null; }

    public Long getBirdIdOrNull() {
        return bird != null ? bird.getId() : null;
    }
}
