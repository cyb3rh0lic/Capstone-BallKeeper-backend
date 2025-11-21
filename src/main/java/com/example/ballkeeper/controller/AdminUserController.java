package com.example.ballkeeper.controller;

import com.example.ballkeeper.api.dto.reservationDto.ReservationResponse;
import com.example.ballkeeper.api.dto.userDto.UserResponse;
import com.example.ballkeeper.service.AdminReservationService;
import com.example.ballkeeper.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;
    private final AdminReservationService adminReservationService;

    // 전체 사용자 목록 조회
    @GetMapping
    public List<UserResponse> getAllUsers(@RequestParam Long adminId) {
        return userService.getAllUsers(adminId);
    }

    // 사용자 권한 변경
    @PatchMapping("/{userId}/role")
    public UserResponse toggleAdminRole(
            @PathVariable Long userId,
            @RequestParam Long adminId) {
        return userService.toggleAdminAuthority(adminId, userId);
    }

    // 특정 사용자의 예약 내역 조회
    @GetMapping("/{userId}/reservations")
    public List<ReservationResponse> getUserReservations(
            @PathVariable Long userId,
            @RequestParam Long adminId) {
        return adminReservationService.getUserReservations(userId);
    }
}