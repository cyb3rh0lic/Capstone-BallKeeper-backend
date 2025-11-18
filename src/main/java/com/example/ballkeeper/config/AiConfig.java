package com.example.ballkeeper.config;

import com.example.ballkeeper.service.ReservationService;
import com.example.ballkeeper.service.ItemService;
import com.example.ballkeeper.api.dto.itemDto.ItemResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.time.LocalDateTime;
import java.util.function.Function;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class AiConfig {

    @Bean
    @Description("사용자를 위해 물품을 예약합니다. 예약을 위해서는 사용자 ID, 물품 ID, 시작 시간, 종료 시간이 반드시 필요합니다.")
    public Function<ReservationRequest, String> createReservation(ReservationService reservationService) {
        return request -> {
            try {
                var reservation = reservationService.create(
                        request.userId, request.itemId, request.startTime, request.endTime);
                return "예약이 성공적으로 생성되었습니다. 예약 ID는 " + reservation.getId() + " 입니다.";
            } catch (Exception e) {
                return "예약 생성에 실패했습니다. 원인: " + e.getMessage();
            }
        };
    }

    @Bean
    @Description("특정 사용자의 모든 예약 목록을 조회합니다. 이 기능을 사용하려면 사용자 ID가 반드시 필요합니다.")
    public Function<MyReservationsRequest, String> myReservations(ReservationService reservationService) {
        return request -> {
            var reservations = reservationService.myReservations(request.userId);
            if (reservations.isEmpty()) {
                return "아직 등록된 예약이 없습니다.";
            }
            String reservationList = reservations.stream()
                    .map(r -> String.format("- 예약번호 %d: %s, 시간: %s ~ %s (%s)",
                            r.getId(), r.getItem().getName(), r.getStartTime(), r.getEndTime(), r.getStatus()))
                    .collect(Collectors.joining("\n"));
            return "총 " + reservations.size() + "개의 예약이 있습니다:\n" + reservationList;
        };
    }

    @Bean
    @Description("사용자의 예약을 취소합니다. 예약을 취소하려면 사용자 ID와 취소할 예약의 ID가 반드시 필요합니다.")
    public Function<CancelRequest, String> cancelReservation(ReservationService reservationService) {
        return request -> {
            try {
                reservationService.cancel(request.userId, request.reservationId);
                return "예약 번호 " + request.reservationId + "번이 성공적으로 취소되었습니다.";
            } catch (Exception e) {
                return "예약 취소에 실패했습니다. 원인: " + e.getMessage();
            }
        };
    }

    @Bean
    @Description("현재 예약 가능한 (active=true) 모든 용품의 목록과 상세 정보를 조회합니다. 사용자가 '어떤 물품이 있어?', '예약 가능한 공 목록 보여줘' 라고 물어볼 때 사용합니다.")
    public Function<MyReservationsRequest, String> getActiveItems(ItemService itemService) {
        return request -> {
            List<ItemResponse> items = itemService.getActiveItems();

            if (items.isEmpty()) {
                return "현재 예약 가능한 물품이 없습니다.";
            }
            String itemList = items.stream()
                    .map(item -> String.format("- 물품 ID %d: %s (설명: %s)",
                            item.getId(), item.getName(), item.getDescription()))
                    .collect(Collectors.joining("\n"));
            return "현재 예약 가능한 물품 목록입니다:\n" + itemList;
        };
    }

    public record ReservationRequest(Long userId, Long itemId, LocalDateTime startTime, LocalDateTime endTime) {}
    public record CancelRequest(Long userId, Long reservationId) {}
    public record MyReservationsRequest(Long userId) {}
}
