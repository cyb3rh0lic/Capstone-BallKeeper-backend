package com.example.ballkeeper.repository;

import com.example.ballkeeper.domain.reservation.Reservation;
import com.example.ballkeeper.domain.reservation.ReservationStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    List<Reservation> findByStatusOrderByStartTimeDesc(ReservationStatus status);
    List<Reservation> findByUserIdAndStatusNotOrderByStartTimeDesc(Long userId, ReservationStatus status);
    List<Reservation> findAllByStatusAndEndTimeBetween(ReservationStatus status, LocalDateTime start, LocalDateTime end);
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

    // 승인 대기 중인 예약 수 (KPI)
    long countByStatus(ReservationStatus status);

    // 최근 7일간 일별 예약 수 (Native Query 사용이 간편함)
    @Query(value = "SELECT DATE_FORMAT(start_time, '%m-%d') as dateStr, COUNT(*) as cnt " +
            "FROM reservation " +
            "WHERE start_time >= :sevenDaysAgo " +
            "GROUP BY dateStr " +
            "ORDER BY dateStr ASC", nativeQuery = true)
    List<Object[]> countReservationsByDay(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

    // 가장 많이 예약된 물품 Top 5
    @Query("SELECT r.item.name, COUNT(r) FROM Reservation r " +
            "GROUP BY r.item.name " +
            "ORDER BY COUNT(r) DESC")
    List<Object[]> findTopPopularItems(org.springframework.data.domain.Pageable pageable);
}
