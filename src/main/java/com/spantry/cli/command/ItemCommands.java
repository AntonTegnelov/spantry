package com.spantry.cli.command;

import picocli.CommandLine.Command;

/**
 * Container command for all item-related subcommands (add, list, remove).
 * This groups related commands under the 'item' subcommand.
 */
@Command(name = "item",
        description = "Manage inventory items.",
        subcommands = {
                AddItemCommand.class,
                ListItemsCommand.class,
                RemoveItemCommand.class
                // Add more item-related subcommands here
        })
public class ItemCommands {
    // This class doesn't need implementation, it just groups subcommands.
} 