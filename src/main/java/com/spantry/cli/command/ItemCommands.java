package com.spantry.cli.command;

import picocli.CommandLine.Command;

/** Groups subcommands related to inventory item management under the 'item' command. */
@Command(
    name = "item",
    description = "Manage inventory items (add, list, remove).",
    subcommands = {AddItemCommand.class, ListItemsCommand.class, RemoveItemCommand.class
      // Add other item-related commands here (e.g., update)
    })
@SuppressWarnings("PMD.AtLeastOneConstructor")
public final class ItemCommands {

  // This class is just a command container so we can omit the constructor.
  // PMD might ask for a constructor but that would violate another rule
  // about unnecessary constructors.

  // This class doesn't need to be runnable itself,
  // it just acts as a container for subcommands.
}
