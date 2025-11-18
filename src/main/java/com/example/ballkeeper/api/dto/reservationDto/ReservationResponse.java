package com.example.ballkeeper.api.dto.reservationDto;

import com.example.ballkeeper.domain.reservation.Reservation;
import com.example.ballkeeper.domain.reservation.ReservationStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReservationResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long itemId;
    private String itemName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ReservationStatus status;
    private String reason;

    public ReservationResponse(Reservation reservation) {
        this.id = reservation.getId();
        this.userId = reservation.getUser().getId();
        this.userName = reservation.getUser().getName();
        this.itemId = reservation.getItem().getId();
        this.itemName = reservation.getItem().getName();
        this.startTime = reservation.getStartTime();
        this.endTime = reservation.getEndTime();
        this.status = reservation.getStatus();
        this.reason = reservation.getReason();
    }
}
