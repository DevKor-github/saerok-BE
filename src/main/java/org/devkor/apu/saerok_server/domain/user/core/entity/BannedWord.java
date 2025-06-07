package org.devkor.apu.saerok_server.domain.user.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "banned_words")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BannedWord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "banned_words_seq")
    @SequenceGenerator(name = "banned_words_seq", sequenceName = "banned_words_seq", allocationSize = 50)
    private Long id;

    @Column(name = "word", nullable = false, unique = true, length = 50)
    private String word;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static BannedWord createBannedWord(String word) {
        BannedWord bannedWord = new BannedWord();
        bannedWord.word = word.trim();
        bannedWord.createdAt = LocalDateTime.now();
        return bannedWord;
    }
}
