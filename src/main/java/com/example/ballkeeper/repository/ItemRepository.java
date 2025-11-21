package com.example.ballkeeper.repository;

import com.example.ballkeeper.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByActiveTrue();

    // 활성화된 아이템만 찾는 메서드 (기존 /api/items 용도)
    List<Item> findByActive(boolean active);

    // 관리자가 모든 아이템을 ID 오름차순으로 조회
    List<Item> findAllByOrderByIdAsc();

    // 활성 물품 수를 세는 메서드
    long countByActive(boolean active);
}
