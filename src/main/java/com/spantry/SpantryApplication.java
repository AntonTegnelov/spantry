package com.spantry;

import com.spantry.cli.SpantryCliApp;
import com.spantry.inventory.repository.InMemoryInventoryRepository;
import com.spantry.inventory.repository.InventoryRepository;
import com.spantry.inventory.service.InventoryService;
import com.spantry.inventory.service.InventoryServiceImpl;
import picocli.CommandLine;

/**
 * Main entry point for the Spantry application. Sets up dependencies and launches the command-line
 * interface.
 */
public final class SpantryApplication {

  /** Private constructor to prevent instantiation of utility class. */
  private SpantryApplication() {
    // Prevent instantiation
  }

  /**
   * Main method.
   *
   * @param args Command line arguments.
   */
  public static void main(final String[] args) {
    // --- Dependency Injection Setup (Composition Root) ---
    // Rename variable: inventoryRepository -> repository
    final InventoryRepository repository = new InMemoryInventoryRepository();

    // Pass shortened variable
    final InventoryService inventoryService = new InventoryServiceImpl(repository);

    // Pass the service (interface type) to the CLI app
    final SpantryCliApp cliApp = new SpantryCliApp(inventoryService);
    // ----------------------------------------------------

    // Execute the CLI application using PicoCLI
    final CommandLine cmd = new CommandLine(cliApp);
    final int exitCode = cmd.execute(args);
    System.exit(exitCode);
  }
}
