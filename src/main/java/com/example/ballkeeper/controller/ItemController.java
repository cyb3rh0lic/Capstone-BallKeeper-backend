package com.example.ballkeeper.controller;

import com.example.ballkeeper.api.dto.itemDto.ItemCreateRequest;
import com.example.ballkeeper.api.dto.itemDto.ItemResponse;
import com.example.ballkeeper.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    // 관리자용: 새 물품 생성
    @PostMapping("/api/item")
    public ItemResponse createItem(@RequestBody ItemCreateRequest itemCreateRequest) {
        return itemService.createItem(itemCreateRequest);
    }

    // 사용자용: 활성화된 물품 목록 조회
    @GetMapping("/api/items")
    public List<ItemResponse> getActiveItems() {
        return itemService.getActiveItems();
    }
}
