package com.example.ballkeeper.scheduler;

import com.example.ballkeeper.domain.reservation.Reservation;
import com.example.ballkeeper.domain.reservation.ReservationStatus;
import com.example.ballkeeper.repository.ReservationRepository;
import com.example.ballkeeper.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationRepository reservationRepository;
    private final NotificationService notificationService;

    // 1분마다 실행
    @Scheduled(cron = "0 * * * * *")
    @Transactional(readOnly = true)
    public void checkUpcomingReturns() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tenMinutesLaterStart = now.plusMinutes(10).minusSeconds(30); // 10분 전 범위 시작
        LocalDateTime tenMinutesLaterEnd = now.plusMinutes(10).plusSeconds(30);   // 10분 전 범위 끝

        List<Reservation> upcomingReturns = reservationRepository.findAllByStatusAndEndTimeBetween(
                ReservationStatus.APPROVED, tenMinutesLaterStart, tenMinutesLaterEnd);

        for (Reservation r : upcomingReturns) {
            String msg = String.format("⏰ 반납 10분 전입니다! (%s)", r.getItem().getName());
            notificationService.sendToUser(r.getUser().getId(), msg);
        }
    }
}