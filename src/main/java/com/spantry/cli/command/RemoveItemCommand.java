package com.spantry.cli.command;

import com.spantry.inventory.service.InventoryService;
import com.spantry.inventory.service.exception.ItemNotFoundException;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/** Command to remove an item from the inventory by its ID. */
@Command(
    name = "remove",
    aliases = {"rm"}, // Alias for convenience
    description = "Removes an item from the inventory by its ID.",
    mixinStandardHelpOptions = true)
public class RemoveItemCommand implements Callable<Integer> {

  // Logger instance
  private static final Logger LOG = LoggerFactory.getLogger(RemoveItemCommand.class);

  private final InventoryService inventoryService;

  @Parameters(index = "0", description = "The unique ID of the item to remove.")
  private String itemId;

  /**
   * Constructor for Dependency Injection.
   *
   * @param inventoryService The service to interact with inventory.
   */
  public RemoveItemCommand(final InventoryService inventoryService) {
    this.inventoryService = inventoryService;
  }

  @Override
  // Suppress CognitiveComplexity for now, consider refactoring later
  // Suppress AvoidCatchingGenericException for the main command boundary catch block
  @SuppressWarnings({"PMD.CognitiveComplexity", "PMD.AvoidCatchingGenericException"})
  public Integer call() {
    int exitCode = 0; // Default to success
    if (itemId == null || itemId.isBlank()) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Error: Item ID must be provided.");
      }
      exitCode = 1;
    } else {
      try {
        inventoryService.removeItem(itemId);
        if (LOG.isInfoEnabled()) {
          LOG.info("Successfully removed item with ID: {}", itemId);
        }
        // exitCode remains 0 (success)
      } catch (ItemNotFoundException e) {
        if (LOG.isErrorEnabled()) {
          LOG.error("Error: {}", e.getMessage());
        }
        exitCode = 1;
      } catch (RuntimeException e) {
        if (LOG.isErrorEnabled()) {
          LOG.error("An unexpected error occurred while removing the item: {}", e.getMessage(), e);
        }
        exitCode = 1;
      }
    }
    return exitCode; // Single return point
  }
}
