package org.devkor.apu.saerok_server.global.util;

import jakarta.servlet.http.HttpServletRequest;
import org.devkor.apu.saerok_server.global.util.dto.ClientInfo;
import org.springframework.stereotype.Component;

@Component
public class ClientInfoExtractor {

    public ClientInfo extract(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");

        String ip = request.getHeader("X-Forward-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }

        return new ClientInfo(userAgent, ip);
    }
}
