package org.devkor.apu.saerok_server.domain.collection.core.entity;

import jakarta.persistence.*;
import lombok.*;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.shared.entity.Auditable;
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
    private Bird bird;

    /**
     * 임시 새 이름 — 현재 미사용
     */
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

    /** 단순 변경: 동정 요청 기록 열고/닫기는 별도 Recorder가 처리 */
    public void changeBird(Bird newBird) {
        this.bird = newBird;
    }

    public double getLongitude() { return location.getX(); }

    public double getLatitude() { return location.getY(); }
}
