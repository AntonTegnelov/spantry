package com.spantry.inventory.service.dto;

import com.spantry.inventory.domain.Location;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Data Transfer Object (DTO) carrying the information needed to add a new item.
 * Using a record for immutability and conciseness.
 * Includes basic validation annotations.
 *
 * @param name           The name of the item (must not be blank).
 * @param quantity       The quantity of the item (must be positive).
 * @param location       The storage location of the item (must not be null).
 * @param expirationDate The optional expiration date of the item.
 */
public record AddItemCommandDto(
        @NotBlank(message = "Item name cannot be blank.")
        String name,

        @Positive(message = "Item quantity must be positive.")
        int quantity,

        @NotNull(message = "Item location cannot be null.")
        Location location,

        @NotNull(message = "Expiration date container cannot be null, "
                           + "use Optional.empty() for no date.")
        Optional<LocalDate> expirationDate
) {
    // Compact constructor for potential future validation/normalization, if needed.
    // Basic validation is handled by annotations for now.
} 