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
  @SuppressWarnings({"PMD.AvoidCatchingGenericException"})
  public Integer call() {
    int exitCode = 0; // Default to success
    try {
      // Fetch items based on filter
      final List<InventoryItem> items = fetchItems();

      // Display items
      displayItems(items);

      // exitCode remains 0 if successful
    } catch (RuntimeException e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Error listing items: {}", e.getMessage(), e);
      }
      exitCode = 1; // Set error code
    }
    return exitCode; // Single return point
  }

  /**
   * Fetches items from the inventory service based on location filter.
   *
   * @return List of inventory items
   */
  private List<InventoryItem> fetchItems() {
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
    return items;
  }

  /**
   * Displays the list of items to the log.
   *
   * @param items List of inventory items to display
   */
  private void displayItems(final List<InventoryItem> items) {
    if (items.isEmpty()) {
      if (LOG.isInfoEnabled()) {
        LOG.info("No items found in inventory.");
      }
    } else {
      if (LOG.isInfoEnabled()) {
        // Use simpler format matching test expectations
        for (final InventoryItem item : items) {
          // Use direct field and handle null for expiry
          final String expiryStr =
              item.expirationDate() != null ? item.expirationDate().toString() : "N/A";
          LOG.info(
              String.format(
                  "ID: %s, Name: %s, Qty: %d, Loc: %s, Exp: %s",
                  item.itemId(), item.name(), item.quantity(), item.location(), expiryStr));
        }
      }
    }
  }

  // Helper to truncate long names for display - Removed as it interferes with tests
  // private String truncate(final String text, final int maxLength) {
  //   String result;
  //   if (text == null) {
  //     result = "";
  //   } else {
  //     result = text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
  //   }
  //   return result;
  // }
}
