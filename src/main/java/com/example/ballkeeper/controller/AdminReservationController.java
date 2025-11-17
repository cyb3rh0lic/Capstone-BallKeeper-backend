package com.example.ballkeeper.controller;

import com.example.ballkeeper.api.dto.reservationDto.ReservationResponse;
import com.example.ballkeeper.service.AdminReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    // '대기 중' 목록 (기존) - 반환 타입 DTO로 변경
    @GetMapping("/pending")
    public List<ReservationResponse> getPendingReservations() {
        return adminReservationService.pendingList();
    }

    // --- ▼ 1. 신규 API 추가 ▼ ---
    @GetMapping("/approved")
    public List<ReservationResponse> getApprovedReservations() {
        return adminReservationService.approvedList();
    }

    @GetMapping("/all")
    public List<ReservationResponse> getAllReservations() {
        return adminReservationService.allList();
    }

    // '승인' (기존) - 반환 타입 DTO로 변경
    @PostMapping("/{reservationId}/approve")
    public ReservationResponse approveReservation(@PathVariable Long reservationId, @RequestParam Long adminId) {
        return adminReservationService.approve(adminId, reservationId);
    }

    // '반려' (기존) - 반환 타입 DTO로 변경
    @PostMapping("/{reservationId}/reject")
    public ReservationResponse rejectReservation(@PathVariable Long reservationId, @RequestParam Long adminId, @RequestParam String reason) {
        return adminReservationService.reject(adminId, reservationId, reason);
    }
}