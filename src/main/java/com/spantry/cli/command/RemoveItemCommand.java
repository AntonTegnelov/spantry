package com.spantry.cli.command;

import com.spantry.inventory.service.InventoryService;
import com.spantry.inventory.service.exception.ItemNotFoundException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

/**
 * Command to remove an item from the inventory by its ID.
 */
@Command(name = "remove",
         aliases = {"rm"}, // Alias for convenience
         description = "Removes an item from the inventory by its ID.",
         mixinStandardHelpOptions = true)
public class RemoveItemCommand implements Callable<Integer> {

    private final InventoryService inventoryService;

    @Parameters(index = "0", description = "The unique ID of the item to remove.")
    private String itemId;

    /**
     * Constructor for Dependency Injection.
     *
     * @param inventoryService The service to manage inventory.
     */
    public RemoveItemCommand(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public Integer call() {
        if (itemId == null || itemId.isBlank()) {
            System.err.println("Error: Item ID must be provided.");
            return 1; // Error
        }

        try {
            inventoryService.removeItem(itemId);
            System.out.println("Successfully removed item with ID: " + itemId);
            return 0; // Success
        } catch (ItemNotFoundException e) {
            System.err.println("Error: " + e.getMessage());
            return 1; // Error
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while removing the item: " + e.getMessage());
            // Log stack trace for debugging if needed
            // e.printStackTrace();
            return 1; // Error
        }
    }
} 