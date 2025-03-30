package com.spantry.cli.command;

import com.spantry.inventory.domain.Item;
import com.spantry.inventory.domain.Location;
import com.spantry.inventory.service.InventoryService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Command to list inventory items.
 */
@Command(name = "list",
         description = "Lists inventory items, optionally filtered by location.",
         mixinStandardHelpOptions = true)
public class ListItemsCommand implements Callable<Integer> {

    private final InventoryService inventoryService;

    @Option(names = {"-l", "--location"}, description = "Filter items by location (e.g., PANTRY, FRIDGE, FREEZER). Optional.")
    private Location location;

    /**
     * Constructor for Dependency Injection.
     *
     * @param inventoryService The service to retrieve inventory data.
     */
    public ListItemsCommand(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @Override
    public Integer call() {
        try {
            List<Item> items;
            if (location == null) {
                System.out.println("Listing all inventory items:");
                items = inventoryService.getAllItems();
            } else {
                System.out.println("Listing items in location: " + location);
                items = inventoryService.getItemsByLocation(location);
            }

            if (items.isEmpty()) {
                System.out.println("  No items found.");
            } else {
                // Simple tabular format
                System.out.printf("  %-38s %-15s %-10s %-10s %s%n",
                        "ID", "Name", "Quantity", "Location", "Expires");
                System.out.println("  " + "-".repeat(90)); // Separator line
                for (Item item : items) {
                    String expiryStr = item.getExpirationDate().map(Object::toString).orElse("N/A");
                    System.out.printf("  %-38s %-15s %-10d %-10s %s%n",
                            item.getId(),
                            truncate(item.getName(), 15),
                            item.getQuantity(),
                            item.getLocation(),
                            expiryStr);
                }
            }
            return 0; // Success
        } catch (Exception e) {
            System.err.println("Error listing items: " + e.getMessage());
            return 1; // Error
        }
    }

    // Helper to truncate long names for display
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }
} 