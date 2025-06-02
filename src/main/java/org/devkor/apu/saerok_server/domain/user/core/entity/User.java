package org.devkor.apu.saerok_server.domain.user.core.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.devkor.apu.saerok_server.global.entity.SoftDeletableAuditable;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
public class User extends SoftDeletableAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "nickname")
    @Setter
    private String nickname;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private GenderType gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "signup_status")
    @Setter
    private SignupStatusType signupStatus;

    public static User createUser(String email) {
        User user = new User();
        user.email = email;
        user.signupStatus = SignupStatusType.PROFILE_REQUIRED;
        return user;
    }
}
