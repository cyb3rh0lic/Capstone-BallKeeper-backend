package com.example.ballkeeper.controller;

import com.example.ballkeeper.api.dto.reservationDto.ReservationCreateRequest;
import com.example.ballkeeper.api.dto.reservationDto.ReservationResponse;
import com.example.ballkeeper.domain.reservation.Reservation;
import com.example.ballkeeper.repository.ItemRepository;
import com.example.ballkeeper.service.ReservationService;
import com.example.ballkeeper.repository.UserAccountRepository;
import com.example.ballkeeper.api.dto.reservationDto.CalendarEventResponse;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;
    private final ItemRepository itemRepository;
    private final UserAccountRepository userAccountRepository;

    private static final DateTimeFormatter F = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // 임시 로그인 대체: email로 유저 찾거나 자동 생성 (개발 편의)
    @PostMapping("/ensure-user")
    public Object ensureUser(@RequestParam String email, @RequestParam String name) {
        var existing = userAccountRepository.findByEmail(email);
        if (existing != null) return existing;
        var u = com.example.ballkeeper.domain.user.UserAccount.builder()
                .email(email).name(name).password("temp").admin(false).build();
        return userAccountRepository.save(u);
    }

    // 예약 생성
    @PostMapping("/reservations")
    public ReservationResponse create(@Valid @RequestBody ReservationCreateRequest req) {
        var r = reservationService.create(req.userId(), req.itemId(), req.startTime(), req.endTime());
        return toDto(r);
    }

    // 내 예약
    @GetMapping("/reservations/my")
    public List<ReservationResponse> my(@RequestParam Long userId) {
        return reservationService.myReservations(userId).stream().map(this::toDto).toList();
    }

    // 취소
    @PostMapping("/reservations/{id}/cancel")
    public void cancel(@RequestParam Long userId, @PathVariable Long id) {
        reservationService.cancel(userId, id);
    }

    // 캘린더 데이터 조회 API
    @GetMapping("/reservations/calendar")
    public List<CalendarEventResponse> getCalendarEvents(
            @RequestParam Long itemId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return reservationService.getCalendarEvents(itemId, start, end);
    }

    private ReservationResponse toDto(Reservation r) {
        return new ReservationResponse(r);
    }
}
