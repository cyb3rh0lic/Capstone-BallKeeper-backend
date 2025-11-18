package com.example.ballkeeper.service;

import com.example.ballkeeper.api.dto.reservationDto.ReservationResponse;
import com.example.ballkeeper.domain.reservation.Reservation;
import com.example.ballkeeper.domain.reservation.ReservationStatus;
import com.example.ballkeeper.repository.ReservationRepository;
import com.example.ballkeeper.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminReservationService {

    private final ReservationRepository reservationRepository;
    private final UserAccountRepository userAccountRepository;

    private void assertAdmin(Long adminId) {
        var admin = userAccountRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정 없음"));
        if (!admin.isAdmin()) throw new AccessDeniedException("관리자만 가능합니다.");
    }

    public ReservationResponse approve(Long adminId, Long reservationId) {
        assertAdmin(adminId);

        var r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 없음"));
        if (r.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태만 승인할 수 있습니다.");
        }

        // 최종 중복 체크(같은 아이템, APPROVED 와 겹치면 불가)
        boolean conflict = reservationRepository
                .existsByItemIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        r.getItem().getId(), ReservationStatus.APPROVED,
                        r.getEndTime(), r.getStartTime());
        if (conflict) throw new IllegalStateException("이미 승인된 겹치는 예약이 있습니다.");

        r.setStatus(ReservationStatus.APPROVED);
        r.setReason(null); // 예전 반려사유가 있었다면 정리
        return new ReservationResponse(r); // @Transactional 이므로 flush 시 저장, DTO로 변환하여 반환
    }

    public ReservationResponse reject(Long adminId, Long reservationId, String reason) {
        assertAdmin(adminId);

        var r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약 없음"));
        if (r.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("PENDING 상태만 반려할 수 있습니다.");
        }
        r.setStatus(ReservationStatus.REJECTED);
        r.setReason(reason);
        return new ReservationResponse(r);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> pendingList() {
        return reservationRepository.findByStatusOrderByStartTimeAsc(ReservationStatus.PENDING)
                .stream()
                .map(ReservationResponse::new) // DTO로 변환
                .collect(Collectors.toList());
    }

    /**
     * '승인 완료'된 예약 목록을 시작 시간 오름차순으로 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> approvedList() {
        return reservationRepository.findByStatusOrderByStartTimeAsc(ReservationStatus.APPROVED)
                .stream()
                .map(ReservationResponse::new) // DTO로 변환
                .collect(Collectors.toList());
    }

    /**
     * '전체' 예약 내역을 시작 시간 최신순(내림차순)으로 조회합니다.
     */
    @Transactional(readOnly = true)
    public List<ReservationResponse> allList() {
        return reservationRepository.findAllByOrderByStartTimeDesc()
                .stream()
                .map(ReservationResponse::new) // DTO로 변환
                .collect(Collectors.toList());
    }
}
