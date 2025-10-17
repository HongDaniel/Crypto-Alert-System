package com.cryptoAlert.api.controller;

import com.cryptoAlert.AlertSettingService;
import com.cryptoAlert.dto.request.AlertSettingRequest;
import com.cryptoAlert.entity.AlertSetting;
import com.cryptoAlert.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Alert Setting Controller", description = "알림 설정 API")
@RestController
@RequestMapping("/api/alerts")
public class AlertSettingController {

    private final AlertSettingService alertSettingService;

    public AlertSettingController(AlertSettingService alertSettingService) {
        this.alertSettingService = alertSettingService;
    }


    // CREATE
    @Operation(summary = "알림 등록", description = "fear & greed index에 대한 알림 설정을 추가")
    @PostMapping
    public ResponseEntity<?> createAlert(@RequestBody AlertSettingRequest request, @AuthenticationPrincipal User user) {
        try {
            // user가 null인 경우 기본 사용자 ID 사용 (UserInitializer에서 생성된 사용자)
            Long userId = (user != null) ? user.getId() : 1L;
            AlertSetting created = alertSettingService.createAlertSetting(request, userId);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            // 중복 체크 실패 시 400 Bad Request 반환
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 기타 예외 시 500 Internal Server Error 반환
            return ResponseEntity.status(500).body("Alert creation failed: " + e.getMessage());
        }
    }

    // READ (전체 조회)
    @Operation(summary = "모든 알림 조회", description = "모든 알림에 대한 조회")
    @GetMapping
    public ResponseEntity<List<AlertSetting>> getAllAlerts() {
        List<AlertSetting> alerts = alertSettingService.getAllAlertSettings();
        return ResponseEntity.ok(alerts);
    }

    // READ (단건 조회)
    @Operation(summary = "단건 알림 조회", description = "알림에 대한 단건 조회")
    @GetMapping("/{id}")
    public ResponseEntity<AlertSetting> getAlert(@PathVariable Long id) {
        return alertSettingService.getAlertSettingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // UPDATE
    @Operation(summary = "알림 수정", description = "알림 세부 내용 수정")
    @PutMapping("/{id}")
    public ResponseEntity<AlertSetting> updateAlert(
            @PathVariable Long id,
            @RequestBody AlertSettingRequest request
    ) {
        AlertSetting updated = alertSettingService.updateAlertSetting(id, request);
        return ResponseEntity.ok(updated);
    }


    // DELETE
    @Operation(summary = "알림 삭제", description = "알림 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        alertSettingService.deleteAlertSetting(id);
        return ResponseEntity.noContent().build();
    }

}