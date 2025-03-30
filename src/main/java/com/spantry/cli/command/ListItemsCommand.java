package com.spantry.cli.command;

// import com.spantry.inventory.domain.Item;
import com.spantry.inventory.domain.InventoryItem;
import com.spantry.inventory.domain.Location;
import com.spantry.inventory.service.InventoryService;
import java.util.List;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** Command to list inventory items. */
@Command(
    name = "list",
    description = "Lists inventory items, optionally filtered by location.",
    mixinStandardHelpOptions = true)
public class ListItemsCommand implements Callable<Integer> {

  // Logger instance
  private static final Logger LOG = LoggerFactory.getLogger(ListItemsCommand.class);

  private final InventoryService inventoryService;

  @Option(
      names = {"-l", "--location"},
      description = "Filter items by location (e.g., PANTRY, FRIDGE, FREEZER). Optional.")
  private Location location;

  /**
   * Constructor for Dependency Injection.
   *
   * @param inventoryService The service to retrieve inventory data.
   */
  public ListItemsCommand(final InventoryService inventoryService) {
    this.inventoryService = inventoryService;
  }

  @Override
  @SuppressWarnings({"PMD.SystemPrintln", "PMD.AvoidCatchingGenericException"})
  public Integer call() {
    int exitCode = 0; // Default to success
    try {
      List<InventoryItem> items;
      if (location == null) {
        if (LOG.isInfoEnabled()) {
          LOG.info("Listing all inventory items:");
        }
        items = inventoryService.getAllItems();
      } else {
        if (LOG.isInfoEnabled()) {
          LOG.info("Listing items in location: {}", location);
        }
        items = inventoryService.getItemsByLocation(location);
      }

      if (items.isEmpty()) {
        System.out.println("  No items found.");
      } else {
        System.out.printf(
            "  %-38s %-15s %-10s %-10s %s%n", "ID", "Name", "Quantity", "Location", "Expires");
        System.out.println("  " + "-".repeat(90));

        for (final InventoryItem item : items) {
          final String expiryStr = item.getExpirationDate().map(Object::toString).orElse("N/A");
          System.out.printf(
              "  %-38s %-15s %-10d %-10s %s%n",
              item.getItemId(),
              truncate(item.getName(), 15),
              item.getQuantity(),
              item.getLocation(),
              expiryStr);
        }
      }
      // exitCode remains 0 if successful
    } catch (RuntimeException e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Error listing items: {}", e.getMessage(), e);
      }
      exitCode = 1; // Set error code
    }
    return exitCode; // Single return point
  }

  // Helper to truncate long names for display
  private String truncate(final String text, final int maxLength) {
    String result;
    if (text == null) {
      result = "";
    } else {
      result = text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }
    return result;
  }
}
