package com.example.ballkeeper.service;

import com.example.ballkeeper.api.dto.itemDto.ItemCreateRequest;
import com.example.ballkeeper.api.dto.itemDto.ItemResponse;
import com.example.ballkeeper.domain.item.Item;
import com.example.ballkeeper.domain.user.UserAccount;
import com.example.ballkeeper.repository.ItemRepository;
import com.example.ballkeeper.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserAccountRepository userAccountRepository;

    private UserAccount assertAdmin(Long adminId) {
        var admin = userAccountRepository.findById(adminId)
                .orElseThrow(() -> new IllegalArgumentException("관리자 계정 없음"));
        if (!admin.isAdmin()) throw new AccessDeniedException("관리자만 가능합니다.");
        return admin;
    }

    // 새 물품 생성
    public ItemResponse createItem(ItemCreateRequest req) {
        assertAdmin(req.getAdminUserId()); // 관리자 확인
        Item item = Item.builder()
                .name(req.getName())
                .description(req.getDescription())
                .active(true) // 기본값은 활성
                .build();
        itemRepository.save(item);
        return new ItemResponse(item);
    }

    // 사용자용: 활성화된 물품만 조회
    @Transactional(readOnly = true)
    public List<ItemResponse> getActiveItems() {
        return itemRepository.findByActive(true)
                .stream()
                .map(ItemResponse::new)
                .collect(Collectors.toList());
    }

    // 관리자용: 모든 물품 조회
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems(Long adminId) {
        assertAdmin(adminId);
        return itemRepository.findAllByOrderByIdAsc()
                .stream()
                .map(ItemResponse::new)
                .collect(Collectors.toList());
    }

    // 관리자용: 물품 활성/비활성 토글
    public ItemResponse toggleItemActive(Long adminId, Long itemId, boolean active) {
        assertAdmin(adminId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("물품을 찾을 수 없습니다."));

        item.setActive(active);
        return new ItemResponse(item); // 변경된 상태 DTO로 반환
    }
}
