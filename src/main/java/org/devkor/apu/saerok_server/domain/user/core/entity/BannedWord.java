package org.devkor.apu.saerok_server.domain.user.core.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.devkor.apu.saerok_server.global.entity.CreatedAtOnly;

@Entity
@Table(name = "banned_words")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BannedWord extends CreatedAtOnly {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "word", nullable = false, unique = true, length = 50)
    private String word;

    public static BannedWord createBannedWord(String word) {
        BannedWord bannedWord = new BannedWord();
        bannedWord.word = word.trim();
        return bannedWord;
    }
}
