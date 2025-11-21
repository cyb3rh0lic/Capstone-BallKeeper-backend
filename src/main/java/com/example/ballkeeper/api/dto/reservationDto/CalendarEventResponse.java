package com.example.ballkeeper.api.dto.reservationDto;

import com.example.ballkeeper.domain.reservation.Reservation;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CalendarEventResponse {
    private Long id;
    private String title; // 예약됨 / 승인 대기
    private LocalDateTime start;
    private LocalDateTime end;
    private String status; // 색상 구분용

    public CalendarEventResponse(Reservation r) {
        this.id = r.getId();
        this.title = (r.getStatus().name().equals("PENDING")) ? "승인 대기" : "예약 완료";
        this.start = r.getStartTime();
        this.end = r.getEndTime();
        this.status = r.getStatus().name();
    }
}