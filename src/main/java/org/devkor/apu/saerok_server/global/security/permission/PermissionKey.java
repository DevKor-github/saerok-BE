package org.devkor.apu.saerok_server.global.security.permission;

/***
 * source of truth: permissions.md
 * 새로운 Permission을 추가할 때는:
 * 1. 문서를 먼저 업데이트하고
 * 2. PermissionKey에 상수를 추가한다
 * 3. Permission 테이블에 해당 레코드를 추가한다
 */
public enum PermissionKey {
    ADMIN_LOGIN,
    ADMIN_REPORT_READ,
    ADMIN_REPORT_WRITE,
    ADMIN_AUDIT_READ,
    ADMIN_STAT_READ,
    ADMIN_STAT_WRITE,
    ADMIN_AD_READ,
    ADMIN_AD_WRITE,
    ADMIN_SLOT_DELETE,
    ADMIN_ROLE_MY_READ,
    ADMIN_ROLE_READ,
    ADMIN_ROLE_WRITE,
    ADMIN_ANNOUNCEMENT_READ,
    ADMIN_ANNOUNCEMENT_WRITE
}
