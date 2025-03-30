package com.spantry.inventory.repository;

import com.spantry.inventory.domain.Item;
import com.spantry.inventory.domain.Location;
import java.util.List;
import java.util.Optional;

/**
 * Defines the contract for inventory persistence operations.
 * This decouples the application logic from the specific data storage mechanism.
 */
public interface InventoryRepository {

    /**
     * Saves (creates or updates) an item.
     * @param item The item to save.
     * @return The saved item (potentially with generated ID or updated state).
     */
    Item save(Item item);

    /**
     * Finds an item by its unique ID.
     * @param id The ID of the item to find.
     * @return An Optional containing the item if found, otherwise empty.
     */
    Optional<Item> findById(String id);

    /**
     * Retrieves all items.
     * @return A list of all items, which may be empty.
     */
    List<Item> findAll();

    /**
     * Deletes an item by its unique ID.
     * @param id The ID of the item to delete.
     */
    void deleteById(String id);

    /**
     * Finds all items stored in a specific location.
     * @param location The location to search for.
     * @return A list of items found in the specified location, which may be empty.
     */
    List<Item> findByLocation(Location location);
} 