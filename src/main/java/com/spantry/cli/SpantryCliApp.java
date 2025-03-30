package com.spantry.cli;

import com.spantry.cli.command.ItemCommands;
import picocli.CommandLine;
import picocli.CommandLine.Command;

/**
 * Main entry point for the Spantry CLI application using picocli.
 * This class acts as the root command and defines subcommands.
 * It will also be part of the Composition Root, responsible for
 * wiring dependencies (like InventoryService) into the commands.
 * // TODO: Implement dependency wiring (manual DI).
 * // TODO: Instantiate and pass InventoryService to commands.
 */
@Command(name = "spantry", mixinStandardHelpOptions = true,
        version = "Spantry CLI 1.0",
        description = "Manages your pantry inventory.",
        subcommands = {
                ItemCommands.class
                // Add other top-level command groups here (e.g., RecipeCommands)
        })
public class SpantryCliApp implements Runnable { // Or Callable<Integer>

    // Picocli injects options and subcommands here

    @Override
    public void run() {
        // If the user runs the command without any subcommands,
        // picocli automatically shows the usage help message
        // because mixinStandardHelpOptions = true.
        // You could add default behavior here if needed.
        System.out.println("Use 'spantry help' for command list.");
    }

    // The Main method will likely instantiate this class, wire dependencies,
    // and execute it using picocli's CommandLine.
    // Example (in Main.java):
    /*
    public static void main(String[] args) {
        // 1. Instantiate Dependencies (Composition Root)
        InventoryRepository repository = new InMemoryInventoryRepository();
        InventoryService service = new InventoryServiceImpl(repository);

        // 2. Create Command Instance (potentially using a factory for DI)
        SpantryCliApp app = new SpantryCliApp(); // Needs modification for DI
        // A Picocli factory can be used to inject 'service' into subcommands
        CommandLine cmd = new CommandLine(app, new DependencyFactory(service));

        // 3. Execute
        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
    */
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