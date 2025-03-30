package com.spantry.cli.command;

import picocli.CommandLine.Command;

/** Groups subcommands related to inventory item management under the 'item' command. */
@Command(
    name = "item",
    description = "Manage inventory items (add, list, remove).",
    subcommands = {AddItemCommand.class, ListItemsCommand.class, RemoveItemCommand.class
      // Add other item-related commands here (e.g., update)
    })
@SuppressWarnings({
  "PMD.UnusedPrivateMethod", // Constructor is intentionally private utility class pattern
  "PMD.MissingStaticMethodInNonInstantiatableClass" // Also suppress this related rule
})
public final class ItemCommands {

  // Private constructor to prevent instantiation of this grouping class
  private ItemCommands() {
    // Utility classes should not be instantiated
    throw new IllegalStateException("Utility class");
  }

  // This class doesn't need to be runnable itself,
  // it just acts as a container for subcommands.
}
