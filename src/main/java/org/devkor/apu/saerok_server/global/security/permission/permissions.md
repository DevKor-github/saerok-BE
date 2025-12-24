# Admin Permissions 명세

## 어드민 로그인 (Admin Login)

- KEY: ADMIN_LOGIN
  - 설명: 어드민에 로그인
    - 현재 허용된 Role: TEAM_MEMBER
    - 관련 메서드:
      - AdminKakaoLoginService.login()

## 신고 (Report)

- KEY: ADMIN_REPORT_READ
    - 설명: 신고된 콘텐츠 내용 조회
        - 현재 허용된 Role: TEAM_MEMBER, ADMIN_EDITOR
        - 관련 메서드:
            - AdminReportController.listCollectionReports()
            - AdminReportController.getCollectionReportDetail()
            - AdminReportController.listCommentReports()
            - AdminReportController.getCommentReportDetail()

- KEY: ADMIN_REPORT_WRITE
    - 설명: 신고된 콘텐츠에 대한 모든 조치
        - 현재 허용된 Role: ADMIN_EDITOR
        - 관련 메서드:
            - AdminReportController.deleteCollectionByReport()
            - AdminReportController.ignoreCollectionReport()
            - AdminReportController.ignoreCommentReport()
            - AdminReportController.deleteCommentByReport()

## 관리자 활동 로그 (Audit)

- KEY: ADMIN_AUDIT_READ
    - 설명: 관리자 활동 로그 조회
        - 현재 허용된 Role: TEAM_MEMBER, ADMIN_EDITOR
        - 관련 메서드:
            - AdminAuditLogController.listAuditLogs()

## 통계 (Stats)

- KEY: ADMIN_STAT_READ
    - 설명: 서비스 통계 조회
        - 현재 허용된 Role: TEAM_MEMBER, ADMIN_EDITOR
        - 관련 메서드:
            - AdminStatController.getSeries()

- KEY: ADMIN_STAT_WRITE
    - 설명: 서비스 통계 수동 집계
        - 현재 허용된 Role: ADMIN_EDITOR
        - 관련 메서드:
            - AdminStatController.aggregateYesterday()

## 광고 (Ad)

- KEY: ADMIN_AD_READ
    - 설명: 광고, 광고 위치, 광고 스케줄 조회
        - 현재 허용된 Role: TEAM_MEMBER, ADMIN_EDITOR
        - 관련 메서드:
            - AdminAdController.listAds()
            - AdminAdController.listSlots()
            - AdminAdController.listPlacements()


- KEY: ADMIN_AD_WRITE
    - 설명: 광고, 광고 위치, 광고 스케줄 생성/수정/삭제 (단, 광고 위치 삭제는 불가)
        - 현재 허용된 Role: ADMIN_EDITOR
        - 관련 메서드:
            - AdminAdController.createAd()
            - AdminAdController.updateAd()
            - AdminAdController.deleteAd()
            - AdminAdController.generateAdImagePresignUrl()
            - AdminAdController.createSlot()
            - AdminAdController.updateSlot()
            - AdminAdController.createPlacement()
            - AdminAdController.updatePlacement()
            - AdminAdController.deletePlacement()

- KEY: ADMIN_SLOT_DELETE
    - 설명: 광고 위치 삭제
        - 현재 허용된 Role: ADMIN_EDITOR
        - 관련 메서드:
            - AdminAdController.deleteSlot()

## 공지사항 (Announcement)

- KEY: ADMIN_ANNOUNCEMENT_READ
    - 설명: 관리자 공지사항 조회
        - 현재 허용된 Role: TEAM_MEMBER, ADMIN_EDITOR
        - 관련 메서드:
            - AdminAnnouncementController.listAnnouncements()

- KEY: ADMIN_ANNOUNCEMENT_WRITE
    - 설명: 관리자 공지사항 생성/수정/삭제
        - 현재 허용된 Role: ADMIN_EDITOR
        - 관련 메서드:
            - AdminAnnouncementController.createAnnouncement()
            - AdminAnnouncementController.updateAnnouncement()
            - AdminAnnouncementController.deleteAnnouncement()
            - AdminAnnouncementController.generateImagePresignUrl()

## 관리자 역할 관리 (Role Management)

- KEY: ADMIN_ROLE_MY_READ
    - 설명: 로그인한 관리자의 역할/권한 조회
        - 현재 허용된 Role: TEAM_MEMBER, ADMIN_EDITOR
        - 관련 메서드:
            - AdminRoleController.getMyRoles()

- KEY: ADMIN_ROLE_READ
    - 설명: 모든 관리자(TEAM_MEMBER 기준)의 역할과 권한 조회
        - 현재 허용된 Role: TEAM_MEMBER, ADMIN_EDITOR
        - 관련 메서드:
            - AdminRoleController.listAdminUsers()
            - AdminRoleController.listRoles()

- KEY: ADMIN_ROLE_WRITE
    - 설명: 역할 생성/삭제, 권한 편집 및 사용자 역할 부여/회수
        - 현재 허용된 Role: ADMIN_EDITOR
        - 관련 메서드:
            - AdminRoleController.createRole()
            - AdminRoleController.deleteRole()
            - AdminRoleController.updateRolePermissions()
            - AdminRoleController.grantRoleToUser()
            - AdminRoleController.revokeRoleFromUser()
