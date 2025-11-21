package com.example.ballkeeper.api.dto.itemDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemUpdateRequest {
    private String name;
    private String description;
}