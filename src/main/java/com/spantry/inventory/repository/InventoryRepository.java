package com.spantry.inventory.repository;

import com.spantry.inventory.domain.InventoryItem;
import com.spantry.inventory.domain.Location;
import java.util.List;
import java.util.Optional;

/**
 * Interface defining the contract for inventory persistence operations. This abstraction allows
 * decoupling the application logic (service layer) from the specific data storage mechanism.
 */
public interface InventoryRepository {

  /**
   * Saves a new item or updates an existing item in the repository. If an item with the same ID
   * already exists, it should be overwritten.
   *
   * @param item The item to save (must not be null).
   * @return The saved item (potentially with a generated ID if it was new).
   * @throws NullPointerException if the item is null.
   */
  InventoryItem save(InventoryItem item);

  /**
   * Finds an item by its unique identifier.
   *
   * @param itemId The ID of the item to find (must not be null).
   * @return An Optional containing the found item, or an empty Optional if no item with the given
   *     ID exists.
   * @throws NullPointerException if the itemId is null.
   */
  Optional<InventoryItem> findById(String itemId);

  /**
   * Retrieves all items currently stored in the repository.
   *
   * @return A List containing all items. Returns an empty list if the repository is empty. The
   *     returned list should be unmodifiable or a defensive copy to protect the repository's
   *     internal state.
   */
  List<InventoryItem> findAll();

  /**
   * Deletes an item from the repository based on its unique identifier. If no item with the given
   * ID exists, the operation should do nothing.
   *
   * @param itemId The ID of the item to delete (must not be null).
   * @throws NullPointerException if the itemId is null.
   */
  void deleteById(String itemId);

  /**
   * Finds all items stored in a specific location.
   *
   * @param location The location to search for items (must not be null).
   * @return A List containing all items found in the specified location. Returns an empty list if
   *     no items are found. The returned list should be unmodifiable or a defensive copy.
   * @throws NullPointerException if the location is null.
   */
  List<InventoryItem> findByLocation(Location location);
}
