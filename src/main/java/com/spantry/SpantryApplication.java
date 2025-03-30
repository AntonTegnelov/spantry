package com.spantry;

import com.spantry.cli.SpantryCliApp;
import com.spantry.exception.DependencyCreationException;
import com.spantry.inventory.repository.InMemoryInventoryRepository;
import com.spantry.inventory.repository.InventoryRepository;
import com.spantry.inventory.service.InventoryService;
import com.spantry.inventory.service.InventoryServiceImpl;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

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
    final InventoryRepository repository = new InMemoryInventoryRepository();
    final InventoryService inventoryService = new InventoryServiceImpl(repository);

    // Create the factory with the service instance
    final IFactory factory = new DependencyFactory(inventoryService);

    // Instantiate the root command using the default constructor (factory handles subcommands)
    final SpantryCliApp cliApp = new SpantryCliApp();
    // ----------------------------------------------------

    // Execute the CLI application using PicoCLI with the custom factory
    final CommandLine cmd = new CommandLine(cliApp, factory);
    final int exitCode = cmd.execute(args);
    System.exit(exitCode);
  }

  /**
   * Picocli Factory for Dependency Injection. Injects InventoryService into command constructors.
   */
  private static class DependencyFactory implements IFactory {
    private final InventoryService inventoryService;

    /**
     * Creates a factory that injects the provided service.
     *
     * @param inventoryService The service instance to inject. Must not be null.
     */
    /* package */ DependencyFactory(final InventoryService inventoryService) {
      this.inventoryService =
          Objects.requireNonNull(inventoryService, "inventoryService cannot be null");
    }

    @Override
    public <K> K create(final Class<K> cls) {
      K instance;
      try {
        // Try to find a constructor that accepts InventoryService
        final Constructor<K> constructor = cls.getDeclaredConstructor(InventoryService.class);
        instance = constructor.newInstance(inventoryService);
      } catch (NoSuchMethodException e) {
        // If no such constructor, fallback to default factory (zero-arg constructor)
        try {
          // Attempt to create using the default constructor
          instance = CommandLine.defaultFactory().create(cls);
        } catch (Exception fallbackEx) {
          // Catch Exception from default factory creation attempt
          // Log the original NoSuchMethodException as context
          // Consider logging fallbackEx as well if needed
          // Throw a specific exception indicating both attempts failed
          throw new DependencyCreationException(
              "Failed to create instance for: "
                  + cls.getName()
                  + ". No constructor(InventoryService) found, and default constructor failed.",
              fallbackEx); // Chain the fallback exception
        }
      } catch (InstantiationException
          | IllegalAccessException
          | InvocationTargetException
          | SecurityException ex) {
        // Wrap specific reflection exceptions using specific exception
        throw new DependencyCreationException(
            "Failed to create instance for: " + cls.getName(), ex);
      }
      return instance; // Single exit point
    }
  }
}
