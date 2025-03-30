package com.spantry;

import com.spantry.cli.DependencyFactory;
import com.spantry.cli.SpantryCliApp;
import com.spantry.inventory.repository.InMemoryInventoryRepository;
import com.spantry.inventory.repository.InventoryRepository;
import com.spantry.inventory.service.InventoryService;
import com.spantry.inventory.service.InventoryServiceImpl;
import picocli.CommandLine;

/**
 * Main entry point for the Spantry application.
 * Acts as the Composition Root for manual Dependency Injection.
 */
public class Main {
    /**
     * Main method.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        // === Composition Root ===
        // Instantiate concrete implementations
        InventoryRepository repository = new InMemoryInventoryRepository();
        InventoryService service = new InventoryServiceImpl(repository);

        // Create Factory for Dependency Injection
        CommandLine.IFactory factory = new DependencyFactory(service);

        // Instantiate the root CLI command (picocli will use the factory for subcommands)
        SpantryCliApp app = new SpantryCliApp();

        // Execute picocli command structure, providing the factory
        int exitCode = new CommandLine(app, factory).execute(args);
        System.exit(exitCode);
    }
} 