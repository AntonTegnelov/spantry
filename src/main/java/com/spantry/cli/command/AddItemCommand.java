package com.spantry.cli.command;

import com.spantry.inventory.domain.Item;
import com.spantry.inventory.domain.Location; // Import Location enum
import com.spantry.inventory.service.InventoryService; // Depends on the SERVICE INTERFACE
import com.spantry.inventory.service.dto.AddItemCommandDto; // Import the DTO
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.time.LocalDate; // Import LocalDate
import java.time.format.DateTimeParseException; // Import for date parsing errors
import java.util.Optional; // Import Optional
import java.util.Set;
import java.util.concurrent.Callable; // Using Callable for potential return codes
import java.util.Objects;

/**
 * Picocli command to add a new item to the inventory.
 * // TODO: Define required Options (name, quantity, location, etc.)
 * // TODO: Inject InventoryService (constructor injection preferred via Picocli factory).
 * // TODO: Implement the call() method to invoke InventoryService.addItem.
 * // TODO: Add user feedback (success/error messages).
 */
@Command(name = "add",
         description = "Adds a new item to the inventory.",
         mixinStandardHelpOptions = true)
public class AddItemCommand implements Callable<Integer> {

    private final InventoryService inventoryService;
    private final Validator validator;

    // Constructor for Dependency Injection (used by Picocli factory)
    public AddItemCommand(InventoryService inventoryService) {
        this.inventoryService = Objects.requireNonNull(inventoryService, "inventoryService cannot be null");
        // Initialize the validator
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            this.validator = factory.getValidator();
        } catch (Exception e) {
            // Handle validator initialization error (log or rethrow)
            System.err.println("Error initializing validation framework: " + e.getMessage());
            throw new RuntimeException("Failed to initialize validator", e);
        }
    }

    // --- Command Line Options ---

    @Option(names = {"-n", "--name"}, required = true, description = "Name of the item.")
    private String name;

    @Option(names = {"-q", "--quantity"}, required = true, description = "Quantity of the item.")
    private int quantity;

    @Option(names = {"-l", "--location"}, required = true, description = "Storage location (e.g., PANTRY, FRIDGE, FREEZER).")
    private Location location; // Picocli automatically converts String to Enum

    @Option(names = {"-e", "--expires"}, description = "Expiration date (YYYY-MM-DD). Optional.")
    private String expirationDateStr;

    // --- Callable Implementation ---

    @Override
    public Integer call() {
        Optional<LocalDate> expirationDate = parseExpirationDate();
        if (expirationDateStr != null && expirationDate.isEmpty()) {
            // Error message already printed by parseExpirationDate
            return 1; // Indicate error
        }

        // Create DTO from command line args
        AddItemCommandDto commandDto = new AddItemCommandDto(name, quantity, location, expirationDate);

        // Validate the DTO
        Set<ConstraintViolation<AddItemCommandDto>> violations = validator.validate(commandDto);
        if (!violations.isEmpty()) {
            System.err.println("Error: Invalid item data:");
            for (ConstraintViolation<AddItemCommandDto> violation : violations) {
                System.err.println("  - " + violation.getMessage());
            }
            return 1; // Indicate validation error
        }

        try {
            // Call the service layer
            Item addedItem = inventoryService.addItem(commandDto);

            // Provide user feedback
            System.out.println("Successfully added item:");
            System.out.println("  ID: " + addedItem.getId());
            System.out.println("  Name: " + addedItem.getName());
            System.out.println("  Quantity: " + addedItem.getQuantity());
            System.out.println("  Location: " + addedItem.getLocation());
            addedItem.getExpirationDate().ifPresent(date -> System.out.println("  Expires: " + date));

            return 0; // Indicate success
        } catch (Exception e) {
            // Catch any unexpected errors during service call or processing
            System.err.println("An unexpected error occurred while adding the item:");
            e.printStackTrace(); // Print stack trace for debugging
            return 1; // Indicate failure
        }
    }

    /**
     * Parses the expiration date string into an Optional<LocalDate>.
     * Prints error message and returns empty Optional if parsing fails.
     */
    private Optional<LocalDate> parseExpirationDate() {
        if (expirationDateStr == null || expirationDateStr.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(expirationDateStr));
        } catch (DateTimeParseException e) {
            System.err.println("Error: Invalid date format for expiration date. Please use YYYY-MM-DD.");
            return Optional.empty();
        }
    }
} 