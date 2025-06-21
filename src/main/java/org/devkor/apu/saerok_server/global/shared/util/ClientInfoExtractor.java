package org.devkor.apu.saerok_server.global.shared.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.global.shared.util.dto.ClientInfo;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClientInfoExtractor {

    public ClientInfo extract(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");

        String ip = request.getHeader("X-Forwarded-For");
        log.info("X-Forwarded-For Header: {}", ip);

        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }

        return new ClientInfo(userAgent, ip);
    }
}
