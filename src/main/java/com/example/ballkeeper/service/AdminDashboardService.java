package com.example.ballkeeper.service;

import com.example.ballkeeper.api.dto.admin.DashboardResponse;
import com.example.ballkeeper.domain.reservation.ReservationStatus;
import com.example.ballkeeper.domain.user.UserAccount;
import com.example.ballkeeper.repository.ItemRepository;
import com.example.ballkeeper.repository.ReservationRepository;
import com.example.ballkeeper.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final ReservationRepository reservationRepository;
    private final ItemRepository itemRepository;
    private final UserAccountRepository userAccountRepository;

    private void assertAdmin(Long adminId) {
        UserAccount admin = userAccountRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정 없음"));
        if (!admin.isAdmin()) throw new AccessDeniedException("관리자 권한이 필요합니다.");
    }

    public DashboardResponse getDashboardStats(Long adminId) {
        assertAdmin(adminId);

        // KPI 데이터 조회
        long pendingCount = reservationRepository.countByStatus(ReservationStatus.PENDING);
        long activeItemCount = itemRepository.countByActive(true);
        long totalUserCount = userAccountRepository.count();

        // 일별 차트 데이터 (최근 7일)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> dailyResults = reservationRepository.countReservationsByDay(sevenDaysAgo);
        List<DashboardResponse.DailyReservationStat> dailyStats = dailyResults.stream()
                .map(obj -> new DashboardResponse.DailyReservationStat((String) obj[0], ((Number) obj[1]).longValue()))
                .collect(Collectors.toList());

        // 인기 물품 Top 5
        List<Object[]> popularResults = reservationRepository.findTopPopularItems(PageRequest.of(0, 5));
        List<DashboardResponse.PopularItemStat> popularItems = popularResults.stream()
                .map(obj -> new DashboardResponse.PopularItemStat((String) obj[0], ((Number) obj[1]).longValue()))
                .collect(Collectors.toList());

        return new DashboardResponse(pendingCount, activeItemCount, totalUserCount, dailyStats, popularItems);
    }
}