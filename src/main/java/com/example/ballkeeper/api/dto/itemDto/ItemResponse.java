package com.example.ballkeeper.api.dto.itemDto;

import com.example.ballkeeper.domain.item.Item;
import lombok.Getter;

@Getter
public class ItemResponse{
    private Long id;
    private String name;
    private String description;
    private boolean active;

    public ItemResponse(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.description = item.getDescription();
        this.active = item.isActive();
    }
}
