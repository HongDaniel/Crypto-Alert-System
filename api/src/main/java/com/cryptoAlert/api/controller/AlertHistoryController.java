package com.cryptoAlert.api.controller;

import com.cryptoAlert.AlertHistoryService;
import com.cryptoAlert.entity.AlertHistory;
import com.cryptoAlert.entity.User;
import com.cryptoAlert.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alert-history")
@RequiredArgsConstructor
@Slf4j
public class AlertHistoryController {

    private final AlertHistoryService alertHistoryService;
    private final UserRepository userRepository;

    @Operation(summary = "사용자 알림 히스토리 조회", description = "현재 로그인한 사용자의 알림 히스토리를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<AlertHistory>> getAlertHistory(@AuthenticationPrincipal User user) {
        try {
            // user가 null인 경우 기본 사용자 ID 사용 (UserInitializer에서 생성된 사용자)
            Long userId = (user != null) ? user.getId() : 1L;
            
            List<AlertHistory> history = alertHistoryService.getRecentAlertHistory(userId);
            log.info("Alert history retrieved for user: {}, count: {}", userId, history.size());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Failed to retrieve alert history for user: {}, error: {}", 
                    user != null ? user.getId() : "default", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "최근 알림 히스토리 조회", description = "최근 10개의 알림 히스토리를 조회합니다.")
    @GetMapping("/recent")
    public ResponseEntity<List<AlertHistory>> getRecentAlertHistory(@AuthenticationPrincipal User user) {
        try {
            // user가 null인 경우 기본 사용자 ID 사용 (UserInitializer에서 생성된 사용자)
            Long userId = (user != null) ? user.getId() : 1L;
            
            List<AlertHistory> history = alertHistoryService.getRecentAlertHistory(userId);
            log.info("Recent alert history retrieved for user: {}, count: {}", userId, history.size());
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Failed to retrieve recent alert history for user: {}, error: {}", 
                    user != null ? user.getId() : "default", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "알림 히스토리 상세 조회", description = "특정 알림 히스토리의 상세 정보를 조회합니다.")
    @GetMapping("/{historyId}")
    public ResponseEntity<AlertHistory> getAlertHistoryDetail(@PathVariable Long historyId, @AuthenticationPrincipal User user) {
        try {
            // user가 null인 경우 기본 사용자 ID 사용 (UserInitializer에서 생성된 사용자)
            Long userId = (user != null) ? user.getId() : 1L;
            
            AlertHistory alertHistory = alertHistoryService.getAlertHistoryById(historyId);
            
            // 사용자 본인의 알림 히스토리만 조회 가능
            if (!alertHistory.getUserId().equals(userId)) {
                log.warn("다른 사용자의 알림 히스토리 조회 시도: historyId={}, userId={}, ownerId={}", 
                        historyId, userId, alertHistory.getUserId());
                return ResponseEntity.status(403).build();
            }
            
            log.info("Alert history detail retrieved: historyId={}, userId={}", historyId, userId);
            return ResponseEntity.ok(alertHistory);
        } catch (Exception e) {
            log.error("Failed to retrieve alert history detail: historyId={}, userId={}, error: {}", 
                    historyId, user != null ? user.getId() : "default", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @Operation(summary = "알림 히스토리 삭제", description = "특정 알림 히스토리를 삭제합니다.")
    @DeleteMapping("/{historyId}")
    public ResponseEntity<String> deleteAlertHistory(@PathVariable Long historyId, @AuthenticationPrincipal User user) {
        try {
            // user가 null인 경우 기본 사용자 ID 사용 (UserInitializer에서 생성된 사용자)
            Long userId = (user != null) ? user.getId() : 1L;
            
            AlertHistory alertHistory = alertHistoryService.getAlertHistoryById(historyId);
            
            // 사용자 본인의 알림 히스토리만 삭제 가능
            if (!alertHistory.getUserId().equals(userId)) {
                log.warn("다른 사용자의 알림 히스토리 삭제 시도: historyId={}, userId={}, ownerId={}", 
                        historyId, userId, alertHistory.getUserId());
                return ResponseEntity.status(403).build();
            }
            
            alertHistoryService.deleteAlertHistory(historyId);
            log.info("Alert history deleted: historyId={}, userId={}", historyId, userId);
            return ResponseEntity.ok("Alert history deleted successfully");
        } catch (Exception e) {
            log.error("Failed to delete alert history: historyId={}, userId={}, error: {}", 
                    historyId, user != null ? user.getId() : "default", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to delete alert history");
        }
    }
}
