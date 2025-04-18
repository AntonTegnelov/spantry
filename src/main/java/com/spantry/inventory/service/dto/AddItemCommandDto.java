package com.spantry.inventory.service.dto;

import com.spantry.inventory.domain.Location;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) or Command object carrying the necessary information to add an item.
 * Used as input for the InventoryService.addItem method.
 *
 * <p>Using record for concise immutable data carrier.
 *
 * <p>Fields are validated using Jakarta Bean Validation annotations.
 */
public record AddItemCommandDto(
    @NotBlank(message = "Item name cannot be blank.") String name,
    @Min(value = 1, message = "Item quantity must be positive.") int quantity,
    @NotNull(message = "Item location cannot be null.") Location location,
    LocalDate expirationDate) {

  /**
   * Canonical constructor generated by the record.
   *
   * @param name Name (required)
   * @param quantity Quantity (required, positive)
   * @param location Location (required)
   * @param expirationDate Expiration date (can be null)
   */
  public AddItemCommandDto {
    // Validation handled by annotations + validator call in the command.
    // No need for explicit checks here unless for normalization.
  }
}
