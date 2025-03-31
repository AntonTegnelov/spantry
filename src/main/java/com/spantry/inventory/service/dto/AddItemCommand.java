package com.spantry.inventory.service.dto;

import com.spantry.inventory.domain.Location;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Data Transfer Object (DTO) representing the command to add a new item. Using a DTO helps decouple
 * the service layer input from the domain model and provides a clear contract for the `addItem` use
 * case.
 *
 * <p>We use a record for simplicity and immutability.
 */
public record AddItemCommand(
    String name, // Non-null
    int quantity, // Non-negative
    Location location, // Non-null
    Optional<LocalDate> expirationDate // Optional, Non-null Optional object itself
    ) {
  /**
   * Compact constructor for validation that enforces the constraints for the AddItemCommand.
   *
   * @throws IllegalArgumentException if name is null or blank
   * @throws IllegalArgumentException if quantity is negative
   * @throws IllegalArgumentException if location is null
   * @throws IllegalArgumentException if expirationDate Optional is null
   */
  public AddItemCommand {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Item name cannot be null or blank");
    }
    if (quantity < 0) {
      throw new IllegalArgumentException("Quantity cannot be negative: " + quantity);
    }
    if (location == null) {
      throw new IllegalArgumentException("Location cannot be null");
    }
    if (expirationDate == null) {
      throw new IllegalArgumentException("Expiration date Optional cannot be null");
    }
  }
}
