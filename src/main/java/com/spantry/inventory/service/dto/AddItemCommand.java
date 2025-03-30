package com.spantry.inventory.service.dto;

import com.spantry.inventory.domain.Location;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Data Transfer Object (DTO) or Command object for adding a new item.
 * Used to pass data to the InventoryService#addItem method,
 * decoupling the service API from the internal domain model (Item).
 * // TODO: Define fields (e.g., name, quantity, location, expirationDate)
 * // Consider adding validation annotations if using a validation framework.
 */
public class AddItemCommand {
    // Empty class definition for now
} 