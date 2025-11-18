package com.example.ballkeeper.repository;

import com.example.ballkeeper.domain.reservation.Reservation;
import com.example.ballkeeper.domain.reservation.ReservationStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("""
       select r from Reservation r
       where r.item.id = :equipmentId
         and r.status <> com.example.ballkeeper.domain.reservation.ReservationStatus.CANCELLED
         and ( (r.startTime < :end) and (r.endTime > :start) )
       """)
    List<Reservation> findOverlaps(Long equipmentId, LocalDateTime start, LocalDateTime end);

    List<Reservation> findByUserIdOrderByStartTimeDesc(Long userId);

    List<Reservation> findByStatusOrderByStartTimeAsc(ReservationStatus status);
    List<Reservation> findByUserIdAndStatusNotOrderByStartTimeDesc(Long userId, ReservationStatus status);
    // 'PENDING' 상태의 예약을 시작 시간 오름차순으로 정렬
    List<Reservation> findByStatus(ReservationStatus status, Sort sort);
    // 'CANCELLED'가 아닌 모든 예약을 시작 시간 내림차순으로 정렬
    List<Reservation> findByStatusNot(ReservationStatus status, Sort sort);
    // 모든 예약을 시작 시간 내림차순으로 정렬
    List<Reservation> findAll(Sort sort);
    List<Reservation> findAllByOrderByStartTimeDesc();
    // (allList 용)

    boolean existsByItemIdAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
            Long itemId, ReservationStatus status,
            LocalDateTime end, LocalDateTime start);
}
