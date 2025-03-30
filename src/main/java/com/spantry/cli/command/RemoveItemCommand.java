package com.spantry.cli.command;

import com.spantry.inventory.service.InventoryService; // Depends on the SERVICE INTERFACE
import com.spantry.inventory.service.exception.ItemNotFoundException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;
import java.util.Objects;

/**
 * Picocli command to remove an item from the inventory by its ID.
 * // TODO: Define positional parameter for the item ID.
 * // TODO: Inject InventoryService.
 * // TODO: Implement call() method to invoke InventoryService.removeItem.
 * // TODO: Handle ItemNotFoundException and provide user feedback.
 */
@Command(name = "remove", description = "Remove an item from the inventory by ID.")
public class RemoveItemCommand implements Callable<Integer> {

    private final InventoryService inventoryService;

    // Constructor for Dependency Injection
    public RemoveItemCommand(InventoryService inventoryService) {
        this.inventoryService = Objects.requireNonNull(inventoryService, "inventoryService cannot be null");
    }

    // Define command line parameters using @Parameters
    @Parameters(index = "0", description = "The ID of the item to remove.")
    private String itemId;

    @Override
    public Integer call() throws Exception {
        System.out.printf("Executing remove item command for ID: %s...\n", itemId);
        try {
            // TODO: 1. Call inventoryService.removeItem(itemId).
            inventoryService.removeItem(itemId); // Example call
            System.out.printf("Item with ID '%s' removed successfully.\n", itemId);
            // TODO: 2. Print success message.
            return 0; // Success
        } catch (ItemNotFoundException e) {
            // TODO: 3. Catch ItemNotFoundException.
            System.err.printf("Error: Could not remove item. %s\n", e.getMessage());
            // TODO: 4. Print user-friendly error message.
            return 1; // Indicate error
        } catch (Exception e) {
            // TODO: 5. Catch other potential errors.
            System.err.printf("An unexpected error occurred: %s\n", e.getMessage());
            return 1; // Indicate error
        }
    }
} 