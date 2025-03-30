package com.spantry.inventory.domain;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents an item stored in the pantry inventory.
 * This class is immutable and focuses on representing the state of an item.
 */
public final class Item {

    private final String id;
    private final String name;
    private final int quantity;
    private final Location location; // Assumes Location enum will be created in this package
    private final Optional<LocalDate> expirationDate;

    /**
     * Constructs a new immutable Item.
     *
     * @param id             The unique identifier for the item (non-null).
     * @param name           The name of the item (non-null).
     * @param quantity       The quantity of the item (must be non-negative).
     * @param location       The storage location of the item (non-null).
     * @param expirationDate An Optional containing the expiration date, if applicable (non-null Optional).
     * @throws NullPointerException     if id, name, location, or expirationDate is null.
     * @throws IllegalArgumentException if quantity is negative.
     */
    public Item(String id, String name, int quantity, Location location, Optional<LocalDate> expirationDate) {
        this.id = Objects.requireNonNull(id, "Item ID cannot be null");
        this.name = Objects.requireNonNull(name, "Item name cannot be null");
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative: " + quantity);
        }
        this.quantity = quantity;
        this.location = Objects.requireNonNull(location, "Item location cannot be null");
        // The Optional object itself should not be null, though it can be empty.
        this.expirationDate = Objects.requireNonNull(expirationDate, "Expiration date Optional cannot be null");
    }

    /**
     * Gets the unique identifier of the item.
     *
     * @return The non-null item ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the name of the item.
     *
     * @return The non-null item name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the quantity of the item.
     *
     * @return The non-negative item quantity.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Gets the storage location of the item.
     *
     * @return The non-null item location.
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Gets the expiration date of the item, if present.
     *
     * @return A non-null Optional containing the expiration date.
     */
    public Optional<LocalDate> getExpirationDate() {
        return expirationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return quantity == item.quantity &&
               id.equals(item.id) &&
               name.equals(item.name) &&
               location == item.location && // Enum comparison using == is safe
               expirationDate.equals(item.expirationDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, quantity, location, expirationDate);
    }

    @Override
    public String toString() {
        return "Item{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", quantity=" + quantity +
               ", location=" + location +
               ", expirationDate=" + expirationDate +
               '}';
    }
} 