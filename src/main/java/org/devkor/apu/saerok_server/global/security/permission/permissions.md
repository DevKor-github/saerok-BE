# Admin Permissions 명세

## 신고 (Report)

- KEY: ADMIN_REPORT_READ
    - 설명: 신고된 콘텐츠 내용 조회
        - 현재 허용된 Role: ADMIN_VIEWER, ADMIN_EDITOR
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
        - 현재 허용된 Role: ADMIN_VIEWER, ADMIN_EDITOR
        - 관련 메서드:
            - AdminAuditLogController.listAuditLogs()

## 통계 (Stats)

- KEY: ADMIN_STAT_READ
    - 설명: 서비스 통계 조회
        - 현재 허용된 Role: ADMIN_VIEWER, ADMIN_EDITOR
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
        - 현재 허용된 Role: ADMIN_VIEWER, ADMIN_EDITOR
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

- KEY: ADMIN_SLOT_WRITE
    - 설명: 광고 위치 삭제
        - 현재 허용된 Role: ADMIN_EDITOR
        - 관련 메서드:
            - AdminAdController.deleteSlot()