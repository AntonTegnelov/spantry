package com.spantry.inventory.service;

import com.spantry.inventory.domain.Item; // Assuming Item might be returned directly or used in DTOs
import com.spantry.inventory.domain.Location;
import com.spantry.inventory.service.dto.AddItemCommand; // Reference to the DTO
import com.spantry.inventory.service.exception.ItemNotFoundException; // Reference to custom exception

import java.util.List;

/**
 * Defines the use cases (application logic) for managing inventory.
 * This interface decouples the presentation layer (e.g., CLI) from the core logic.
 */
public interface InventoryService {

    /**
     * Adds a new item to the inventory based on the provided command details.
     * @param cmd The command object containing details for the new item.
     * @return The newly created Item (or a DTO representation).
     * @throws IllegalArgumentException if the command data is invalid.
     */
    Item addItem(AddItemCommand cmd);

    /**
     * Retrieves all items currently in the inventory.
     * @return A list of all items (or DTO representations).
     */
    List<Item> getAllItems(); // Or List<ItemDTO> if preferred

    /**
     * Retrieves items stored in a specific location.
     * @param location The location to filter by.
     * @return A list of items found in the specified location.
     */
    List<Item> getItemsByLocation(Location location);

    /**
     * Removes an item from the inventory by its ID.
     * @param itemId The ID of the item to remove.
     * @throws ItemNotFoundException if no item with the given ID exists.
     */
    void removeItem(String itemId) throws ItemNotFoundException;
} 