package org.devkor.apu.saerok_server.domain.notification.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;

import java.util.Map;

/** nested 구조: subject별로 groupEnabled + actions 맵을 가진다 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "NotificationSettingsResponse",
        description = """
        디바이스별 알림 설정 응답.
        subjects는 NotificationSubject를 키로 하는 맵이며,
        각 값은 해당 subject에 대한 그룹 토글 및 action별 on/off를 담는다.
        """,
        example = """
        {
          "deviceId": "ios-fcm-xxxxxxxx",
          "subjects": {
            "COLLECTION": {
              "groupEnabled": true,
              "actions": {
                "LIKE": true,
                "COMMENT": false,
                "SUGGEST_BIRD_ID": true
              }
            }
          }
        }
        """
)
public record NotificationSettingsResponse(
        @Schema(description = "디바이스 식별자", example = "ios-fcm-xxxxxxxx")
        String deviceId,

        @Schema(
                description = """
                subject별 설정 맵. 키는 NotificationSubject의 문자열값(예: "COLLECTION").
                값은 SubjectSettings(그룹 토글 + action별 on/off)이다.
                """,
                example = """
                {
                  "COLLECTION": {
                    "groupEnabled": true,
                    "actions": {
                      "LIKE": true,
                      "COMMENT": false,
                      "SUGGEST_BIRD_ID": true
                    }
                  }
                }
                """
        )
        Map<NotificationSubject, SubjectSettings> subjects
) {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(
            name = "SubjectSettings",
            description = """
            특정 subject에 대한 설정.
            groupEnabled는 해당 subject의 그룹 토글(action=null)에 해당하며,
            actions는 NotificationAction(예: LIKE, COMMENT 등)을 키로 하는 on/off 맵이다.
            """,
            example = """
            {
              "groupEnabled": true,
              "actions": {
                "LIKE": true,
                "COMMENT": false
              }
            }
            """
    )
    public record SubjectSettings(
            @Schema(description = "subject 그룹 토글 (action=null)", example = "true")
            Boolean groupEnabled,

            @Schema(
                    description = "action별 on/off 맵. 키는 NotificationAction의 문자열값.",
                    example = """
                    {
                      "LIKE": true,
                      "COMMENT": false,
                      "SUGGEST_BIRD_ID": true
                    }
                    """
            )
            Map<NotificationAction, Boolean> actions
    ) {}
}
