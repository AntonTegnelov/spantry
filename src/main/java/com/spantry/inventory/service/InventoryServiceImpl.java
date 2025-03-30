package com.spantry.inventory.service;

import com.spantry.inventory.domain.Item;
import com.spantry.inventory.domain.Location;
import com.spantry.inventory.repository.InventoryRepository;
import com.spantry.inventory.service.dto.AddItemCommand;
import com.spantry.inventory.service.exception.ItemNotFoundException;

import java.util.List;
import java.util.Objects;

/**
 * Implementation of the {@link InventoryService} interface.
 * Contains the core application logic for inventory management.
 * // TODO: Implement the interface methods using the injected repository.
 * // TODO: Add validation logic.
 * // TODO: Map domain objects to DTOs if necessary for the return types.
 */
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    /**
     * Constructs an InventoryServiceImpl with the required repository dependency.
     *
     * @param inventoryRepository The repository used for data persistence (must not be null).
     * @throws NullPointerException if inventoryRepository is null.
     */
    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository, "InventoryRepository cannot be null");
    }

    @Override
    public Item addItem(AddItemCommand command) {
        Objects.requireNonNull(command, "AddItemCommand cannot be null");

        // Create domain Item from command DTO (ID is generated by repository)
        // Note: The DTO already performed basic validation in its constructor
        Item newItem = new Item(
            null, // ID will be generated by the repository
            command.name(),
            command.quantity(),
            command.location(),
            command.expirationDate()
        );

        System.out.println("[DEBUG] InventoryServiceImpl: Calling save with Item: " + newItem);
        return inventoryRepository.save(newItem);
    }

    @Override
    public List<Item> getAllItems() {
        return inventoryRepository.findAll();
    }

    @Override
    public List<Item> getItemsByLocation(Location location) {
        Objects.requireNonNull(location, "Location cannot be null");
        return inventoryRepository.findByLocation(location);
    }

    @Override
    public void removeItem(String itemId) {
        Objects.requireNonNull(itemId, "Item ID cannot be null for removal");

        // Check if item exists before attempting deletion to provide specific feedback
        if (inventoryRepository.findById(itemId).isEmpty()) {
            throw new ItemNotFoundException("Cannot remove item. No item found with ID: " + itemId);
        }
        inventoryRepository.deleteById(itemId);
    }
} 