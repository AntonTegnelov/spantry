package com.spantry.inventory.domain;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Represents an item in the inventory.
 *
 * <p>Made immutable: fields are final, initialized via constructor.
 *
 * <p>Using record for concise immutable data carrier.
 *
 * <p>Implements Serializable for file-based persistence in E2E tests.
 */
public record InventoryItem(
    // ID is nullable initially, assigned by repository
    String itemId,
    @NotBlank(message = "Item name cannot be blank.") String name,
    @Min(value = 1, message = "Item quantity must be positive.") int quantity,
    @NotNull(message = "Item location cannot be null.") Location location,
    // Store LocalDate directly (can be null), it is Serializable
    LocalDate expirationDate)
    implements Serializable { // Implement Serializable

  // Static factory method or alternative constructors could be added if needed
  // for different creation scenarios (e.g., without ID before saving).

  /**
   * Canonical constructor generated by the record.
   *
   * @param itemId ID (can be null before saving)
   * @param name Name (required)
   * @param quantity Quantity (required, positive)
   * @param location Location (required)
   * @param expirationDate Expiration date (can be null)
   */
  public InventoryItem {
    // No explicit validation needed here now for expirationDate nullness
  }

  /**
   * Provides the expiration date as an Optional for convenience.
   *
   * @return Optional containing the expiration date, or empty if null.
   */
  public Optional<LocalDate> getExpirationDateOptional() {
    return Optional.ofNullable(this.expirationDate);
  }

  // Custom constructor if needed (e.g., for creating an item before it has an ID)
  // public InventoryItem(String name, int quantity, Location location, Optional<LocalDate>
  // expirationDate) {
  //   this(null, name, quantity, location, expirationDate);
  // }

  // Getters are automatically generated by the record.
}
