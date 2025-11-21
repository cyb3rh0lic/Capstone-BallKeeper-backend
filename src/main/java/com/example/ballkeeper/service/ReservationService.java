package com.example.ballkeeper.service;

import com.example.ballkeeper.domain.reservation.Reservation;
import com.example.ballkeeper.domain.reservation.ReservationStatus;
import com.example.ballkeeper.repository.*;
import com.example.ballkeeper.event.ReservationCreatedEvent;
import com.example.ballkeeper.api.dto.reservationDto.CalendarEventResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ItemRepository itemRepository;
    private final UserAccountRepository userAccountRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;

    @Transactional
    public Reservation create(Long userId, Long itemId, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        if (!end.isAfter(start)) throw new IllegalArgumentException("예약 종료 시각은 반드시 예약 시작 시각보다 이후여야 합니다.");
        if (Duration.between(start, end).toMinutes() < 30) throw new IllegalArgumentException("최소 30분 이상 예약 해주세요!");

        var equipment = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("물품이 없습니다."));
        if (!equipment.isActive()) throw new IllegalStateException("현재 물품이 비활성 상태입니다.");

        var overlaps = reservationRepository.findOverlaps(itemId, start, end);
        if (!overlaps.isEmpty()) throw new IllegalStateException("이미 겹치는 예약이 있습니다.");

        var user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        var r = Reservation.builder()
                .user(user).item(equipment)
                .startTime(start).endTime(end)
                .status(ReservationStatus.PENDING)
                .build();

        reservationRepository.save(r);

        String msg = String.format("📢 새 예약 요청: %s님이 %s을(를) 예약했습니다.",
                user.getName(), equipment.getName());
        notificationService.sendToAdmins(msg);

        // (기존 이벤트 발행 코드는 유지)
        eventPublisher.publishEvent(new ReservationCreatedEvent(this, r));

        return r;
    }

    @Transactional(readOnly = true)
    public List<Reservation> myReservations(Long userId) {
        return reservationRepository.findByUserIdAndStatusNotOrderByStartTimeDesc(userId, ReservationStatus.CANCELLED);
    }

    @Transactional
    public void cancel(Long userId, Long reservationId) {
        var r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("예약이 없습니다."));
        if (!r.getUser().getId().equals(userId)) throw new IllegalStateException("본인 예약만 취소 가능합니다!");
        r.setStatus(ReservationStatus.CANCELLED);
    }

    // 캘린더 조회
    @Transactional(readOnly = true)
    public List<CalendarEventResponse> getCalendarEvents(Long itemId, LocalDateTime start, LocalDateTime end) {
        return reservationRepository.findCalendarEvents(itemId, start, end)
                .stream()
                .map(CalendarEventResponse::new)
                .collect(Collectors.toList());
    }
}