package com.spantry.cli;

import com.spantry.cli.command.ItemCommands;
// import com.spantry.inventory.service.InventoryService; // No longer needed here
// import java.util.Objects; // No longer needed here
import picocli.CommandLine;
import picocli.CommandLine.Command;

// import com.spantry.cli.command.ItemCommands; // Will be added later

/**
 * The main command-line application class for Spantry. It serves as the entry point and aggregates
 * subcommands.
 */
@Command(
    name = "spantry",
    mixinStandardHelpOptions = true,
    version = "Spantry CLI 1.0",
    description = "Manages your pantry inventory.",
    subcommands = {
      ItemCommands.class // Register the item command group
      // ItemCommands.class // Add inventory commands later
    })
public class SpantryCliApp implements Runnable {

  // Field to hold the injected service (if needed directly by root command)
  // private final InventoryService inventoryService;

  // Constructor accepting InventoryService is removed.
  // Picocli will now use the default no-arg constructor for the root command.
  // The DependencyFactory handles injection for subcommands.

  /** Default constructor. Required by PMD rule, otherwise compiler-generated is fine. */
  public SpantryCliApp() {
    // No initialization needed here for the root command currently.
  }

  @Override
  public void run() {
    // If the command is run without subcommands, print help
    CommandLine.usage(this, System.out);
  }

  // We might add dependency injection setup here later if not using a factory
}

// Example Picocli Factory for DI (can be inner class or separate)
/*
class DependencyFactory implements CommandLine.IFactory {
    private final InventoryService inventoryService;

    public DependencyFactory(InventoryService inventoryService) {
        this.inventoryService = Objects.requireNonNull(inventoryService);
    }

    @Override
    public <K> K create(Class<K> cls) throws Exception {
        try {
            // Try to find a constructor that accepts InventoryService
            Constructor<K> constructor = cls.getDeclaredConstructor(InventoryService.class);
            return constructor.newInstance(inventoryService);
        } catch (NoSuchMethodException e) {
            // If no such constructor, fallback to default factory (zero-arg constructor)
            return CommandLine.defaultFactory().create(cls);
        }
    }
}
*/
