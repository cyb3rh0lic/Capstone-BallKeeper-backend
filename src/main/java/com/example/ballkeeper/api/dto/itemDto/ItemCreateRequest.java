package com.example.ballkeeper.api.dto.itemDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemCreateRequest {
    private Long adminUserId; // 관리자 인증
    private String name;
    private String description;
}