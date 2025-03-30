package com.spantry.inventory.repository;

import com.spantry.inventory.domain.Item;
import com.spantry.inventory.domain.Location;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * An in-memory implementation of the {@link InventoryRepository}.
 * Stores items in a ConcurrentHashMap. Data is lost when the application stops.
 * This implementation handles ID generation using UUID.
 */
public class InMemoryInventoryRepository implements InventoryRepository {

    // Using ConcurrentHashMap for thread safety, although not strictly needed for initial CLI
    private final Map<String, Item> inventory = new ConcurrentHashMap<>();

    @Override
    public Item save(Item item) {
        Objects.requireNonNull(item, "Item cannot be null for saving");

        String id = item.getId();
        Item itemToSave;

        // If item has no ID, generate one. If it has one, use it (update scenario).
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
            // Create a new Item instance with the generated ID
            itemToSave = new Item(id, item.getName(), item.getQuantity(), item.getLocation(), item.getExpirationDate());
        } else {
            // Use the existing item instance if ID was provided (update)
            itemToSave = item;
        }

        inventory.put(itemToSave.getId(), itemToSave);
        return itemToSave;
    }

    @Override
    public Optional<Item> findById(String id) {
        Objects.requireNonNull(id, "ID cannot be null for findById");
        return Optional.ofNullable(inventory.get(id));
    }

    @Override
    public List<Item> findAll() {
        // Return an unmodifiable list to prevent external modification
        return inventory.values().stream()
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public void deleteById(String id) {
        Objects.requireNonNull(id, "ID cannot be null for deleteById");
        inventory.remove(id);
    }

    @Override
    public List<Item> findByLocation(Location location) {
        Objects.requireNonNull(location, "Location cannot be null for findByLocation");
        // Return an unmodifiable list
        return inventory.values().stream()
                .filter(item -> item.getLocation() == location)
                .collect(Collectors.toUnmodifiableList());
    }
} 