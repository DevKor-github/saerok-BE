package org.devkor.apu.saerok_server.domain.notification.application.port;

import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Target;

import java.util.Map;

/**
 * Target에 맞는 메타데이터(extras)를 조립/보강해 주는 포트.
 * - DSL은 이 인터페이스만 의존하고, 실제 구현은 어댑터가 담당한다.
 */
public interface TargetMetadataPort {

    /**
     * @param target     알림의 대상으로 지정된 Target (예: COLLECTION)
     * @param baseExtras DSL에서 누적한 기본 extras (comment, suggestedName 등)
     * @return target에 맞춰 보강된 extras 맵
     */
    Map<String, Object> enrich(Target target, Map<String, Object> baseExtras);
}
