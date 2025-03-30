package com.spantry.inventory.service;

// import com.spantry.inventory.domain.Item;
import com.spantry.inventory.domain.InventoryItem;
import com.spantry.inventory.domain.Location;
import com.spantry.inventory.service.dto.AddItemCommandDto; // DTO for adding items
import java.util.List;

/**
 * Interface defining the application logic (use cases) for managing the inventory. This layer
 * orchestrates the domain objects and repository interactions.
 */
public interface InventoryService {

  /**
   * Adds a new item to the inventory based on the provided command data.
   *
   * @param command The command object containing details of the item to add (must not be null).
   * @return The newly created InventoryItem.
   * @throws NullPointerException if the command is null. // Consider adding more specific
   *     exceptions for validation failures // (e.g., InvalidItemDataException)
   */
  InventoryItem addItem(AddItemCommandDto command);

  /**
   * Retrieves all items currently in the inventory.
   *
   * @return A List containing all inventory items. Returns an empty list if none exist.
   */
  List<InventoryItem> getAllItems();

  /**
   * Retrieves all items stored in a specific location.
   *
   * @param location The location to filter by (must not be null).
   * @return A List containing items found in the specified location. Returns an empty list if none
   *     are found.
   * @throws NullPointerException if the location is null.
   */
  List<InventoryItem> getItemsByLocation(Location location);

  /**
   * Removes an item from the inventory by its unique identifier.
   *
   * @param itemId The ID of the item to remove (must not be null).
   * @throws NullPointerException if the itemId is null. // Consider adding an ItemNotFoundException
   *     if the ID doesn't exist, // as specified in a later TODO
   */
  void removeItem(String itemId);
}
