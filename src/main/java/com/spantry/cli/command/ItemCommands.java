package com.spantry.cli.command;

import picocli.CommandLine.Command;

/** Groups subcommands related to inventory item management under the 'item' command. */
@Command(
    name = "item",
    description = "Manage inventory items (add, list, remove).",
    subcommands = {AddItemCommand.class, ListItemsCommand.class, RemoveItemCommand.class
      // Add other item-related commands here (e.g., update)
    })
public final class ItemCommands {

  // No constructor needed. The default public constructor is sufficient
  // for Picocli to instantiate this grouping command.

  // This class doesn't need to be runnable itself,
  // it just acts as a container for subcommands.
}
