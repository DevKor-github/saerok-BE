package org.devkor.apu.saerok_server.domain.user.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.global.config.ReservedNicknamesConfig;
import org.springframework.stereotype.Service;

/**
 * 예약어 검증을 담당하는 서비스
 * - 서비스 특화 예약어 (정확 매칭)
 * - 관리자용 계정명, 서비스명 등을 보호
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReservedWordService {

    private final ReservedNicknamesConfig reservedNicknamesConfig;

    public boolean isReservedWord(String text) {
        return reservedNicknamesConfig.getItems().contains(text);
    }
}
