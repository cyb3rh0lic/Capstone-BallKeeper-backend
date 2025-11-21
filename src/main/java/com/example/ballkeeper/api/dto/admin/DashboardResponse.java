package com.example.ballkeeper.api.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DashboardResponse {
    // KPI (핵심 지표)
    private long pendingReservationCount; // 승인 대기 중인 예약 수
    private long activeItemCount;         // 활성 물품 수
    private long totalUserCount;          // 전체 회원 수

    // 차트 데이터
    private List<DailyReservationStat> dailyStats; // 최근 7일 예약 수
    private List<PopularItemStat> popularItems;    // 인기 물품 Top 5

    @Getter
    @AllArgsConstructor
    public static class DailyReservationStat {
        private String date; // "MM-dd"
        private long count;
    }

    @Getter
    @AllArgsConstructor
    public static class PopularItemStat {
        private String itemName;
        private long count;
    }
}