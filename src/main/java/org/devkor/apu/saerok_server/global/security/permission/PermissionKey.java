package org.devkor.apu.saerok_server.global.security.permission;

/***
 * source of truth: permissions.md
 * 새로운 Permission을 추가할 때는:
 * 1. 문서를 먼저 업데이트하고
 * 2. 그 다음 PermissionKey에 상수를 추가한다
 */
public enum PermissionKey {
    ADMIN_REPORT_READ,
    ADMIN_REPORT_WRITE,
    ADMIN_AUDIT_READ,
    ADMIN_STAT_READ,
    ADMIN_STAT_WRITE,
    ADMIN_AD_READ,
    ADMIN_AD_WRITE,
    ADMIN_SLOT_WRITE
}
