package com.example.ballkeeper.controller;

import com.example.ballkeeper.api.dto.itemDto.ItemResponse;
import com.example.ballkeeper.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/items")
@RequiredArgsConstructor
public class AdminItemController {

    private final ItemService itemService;

    // 관리자용: 모든 물품 목록 조회
    @GetMapping("/all")
    public List<ItemResponse> getAllItems(@RequestParam Long adminId) {
        // TODO: @AuthenticationPrincipal 로 adminId를 가져오도록 리팩토링
        return itemService.getAllItems(adminId);
    }

    // 관리자용: 물품 활성/비활성 토글
    @PatchMapping("/{itemId}/active")
    public ItemResponse toggleItemActive(
            @PathVariable Long itemId,
            @RequestParam boolean active,
            @RequestParam Long adminId) {
        // TODO: @AuthenticationPrincipal 로 adminId를 가져오도록 리팩토링
        return itemService.toggleItemActive(adminId, itemId, active);
    }
}