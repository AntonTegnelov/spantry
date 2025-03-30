package com.spantry;

import com.spantry.cli.SpantryCliApp;
import com.spantry.inventory.repository.InMemoryInventoryRepository;
import com.spantry.inventory.repository.InventoryRepository;
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
        // InventoryService service = new InventoryServiceImpl(repository); // Instantiate service in next step

        // Instantiate the CLI application
        SpantryCliApp app = new SpantryCliApp();
        // TODO: Inject dependencies into commands using a Factory or similar

        // Execute picocli command structure
        int exitCode = new CommandLine(app).execute(args);
        System.exit(exitCode);
    }
} 