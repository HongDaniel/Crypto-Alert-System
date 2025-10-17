package com.cryptoAlert.api.controller;

import com.cryptoAlert.UserService;
import com.cryptoAlert.dto.request.UserRequest;
import com.cryptoAlert.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "새 사용자를 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<UserResponse> signUp(@RequestBody UserRequest request) {
        UserResponse response = userService.create(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사용자 생성", description = "새로운 사용자를 등록합니다")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        UserResponse response = userService.create(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "모든 사용자 조회", description = "등록된 모든 사용자 목록을 반환합니다")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAll());
    }

    @Operation(summary = "사용자 단건 조회", description = "특정 ID의 사용자 정보를 반환합니다")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @Operation(summary = "사용자 삭제", description = "특정 ID의 사용자를 삭제합니다")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
