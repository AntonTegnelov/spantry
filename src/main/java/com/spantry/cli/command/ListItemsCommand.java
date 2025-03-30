package com.spantry.cli.command;

import com.spantry.inventory.service.InventoryService; // Depends on the SERVICE INTERFACE
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;
import java.util.Objects;

/**
 * Picocli command to list inventory items.
 * // TODO: Define Options for filtering (e.g., by location) or sorting.
 * // TODO: Inject InventoryService.
 * // TODO: Implement call() method to invoke appropriate InventoryService method(s).
 * // TODO: Format and print the list of items to the console.
 */
@Command(name = "list", description = "List inventory items.")
public class ListItemsCommand implements Callable<Integer> {

    private final InventoryService inventoryService;

    // Constructor for Dependency Injection
    public ListItemsCommand(InventoryService inventoryService) {
        this.inventoryService = Objects.requireNonNull(inventoryService, "inventoryService cannot be null");
    }

    // Define command line options using @Option
    // Example:
    // @Option(names = {"-l", "--location"}, description = "Filter by location (e.g., FRIDGE)")
    // private com.spantry.inventory.domain.Location filterLocation;

    @Override
    public Integer call() throws Exception {
        System.out.println("Executing list items command...");
        // TODO: 1. Check for filter options.
        // TODO: 2. Call inventoryService.getAllItems() or getItemsByLocation().
        // TODO: 3. Format the output (e.g., as a table).
        // TODO: 4. Print the formatted list.
        // TODO: 5. Return exit code 0.
        return 0; // Placeholder
    }
} 