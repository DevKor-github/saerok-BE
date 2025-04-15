package org.devkor.apu.saerok_server.domain.user.core.entity;

import jakarta.persistence.*;
import org.devkor.apu.saerok_server.global.entity.SoftDeletableAuditable;

import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User extends SoftDeletableAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "nickname", nullable = false)
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
}
