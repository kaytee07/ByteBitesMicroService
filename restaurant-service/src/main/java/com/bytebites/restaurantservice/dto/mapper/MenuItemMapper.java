package com.bytebites.restaurantservice.dto.mapper;

import com.bytebites.restaurantservice.dto.MenuItemDto;
import com.bytebites.restaurantservice.model.MenuItem;

public class MenuItemMapper {

    public static MenuItemDto toDto(MenuItem menuItem) {
        if (menuItem == null) {
            return null;
        }
        return new MenuItemDto(
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getPrice()
        );
    }
}