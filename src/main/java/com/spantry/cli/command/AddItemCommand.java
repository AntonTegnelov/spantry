package com.spantry.cli.command;

import com.spantry.exception.InitializationException; // Added import
import com.spantry.inventory.domain.InventoryItem; // Correct import
import com.spantry.inventory.domain.Location; // Import Location enum
import com.spantry.inventory.service.InventoryService;
import com.spantry.inventory.service.dto.AddItemCommandDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate; // Import LocalDate
import java.time.format.DateTimeParseException; // Import for date parsing errors
import java.util.Objects;
import java.util.Optional; // Import Optional
import java.util.Set;
import java.util.concurrent.Callable; // Using Callable for potential return codes
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * Picocli command to add a new item to the inventory. // TODO: Define required Options (name,
 * quantity, location, etc.) // TODO: Inject InventoryService (constructor injection preferred via
 * Picocli factory). // TODO: Implement the call() method to invoke InventoryService.addItem. //
 * TODO: Add user feedback (success/error messages).
 */
@Command(
    name = "add",
    description = "Adds a new item to the inventory.",
    mixinStandardHelpOptions = true)
// Suppress specific PMD rule for the static initializer catch block
@SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.CognitiveComplexity"})
public class AddItemCommand implements Callable<Integer> {

  // Logger instance
  private static final Logger LOG = LoggerFactory.getLogger(AddItemCommand.class);

  private static final Validator VALIDATOR; // Renamed from validator
  private final InventoryService inventoryService;

  // --- Moved Command Line Options to the top ---
  @Option(
      names = {"-n", "--name"},
      required = true,
      description = "Name of the item.")
  private String name;

  @Option(
      names = {"-q", "--quantity"},
      required = true,
      description = "Quantity of the item.")
  private int quantity;

  @Option(
      names = {"-l", "--location"},
      required = true,
      description = "Storage location (e.g., PANTRY, FRIDGE, FREEZER).")
  private Location location; // Picocli automatically converts String to Enum

  @Option(
      names = {"-e", "--expires"},
      description = "Expiration date (YYYY-MM-DD). Optional.")
  private String expirationDateStr;

  // --- End Moved Fields ---

  // Static initializer block for the validator
  static {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      VALIDATOR = factory.getValidator();
    } catch (jakarta.validation.ValidationException e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Critical: Failed to initialize validator: {}", e.getMessage(), e);
      }
      throw new InitializationException("Failed to initialize validation framework", e);
    } catch (RuntimeException e) {
      if (LOG.isErrorEnabled()) {
        LOG.error(
            "Critical: Unexpected runtime error initializing validator: {}", e.getMessage(), e);
      }
      throw new InitializationException("Unexpected error initializing validation framework", e);
    }
  }

  /**
   * Constructor for Dependency Injection (used by Picocli factory).
   *
   * @param inventoryService The service instance to inject.
   */
  public AddItemCommand(final InventoryService inventoryService) {
    this.inventoryService =
        Objects.requireNonNull(inventoryService, "inventoryService cannot be null");
    // Validator initialization is now handled in the static block
  }

  // --- Command Line Options (Moved Above) ---

  // --- Callable Implementation ---

  @Override
  // Remove suppression from here as it's now on the class
  // @SuppressWarnings({"PMD.CognitiveComplexity", "PMD.AvoidCatchingGenericException"})
  public Integer call() {
    int exitCode = 0; // Default to success
    final Optional<LocalDate> expDate = parseExpirationDate();

    if (expirationDateStr != null && expDate.isEmpty()) {
      // Error already logged by parseExpirationDate()
      exitCode = 1;
    } else {
      // Create DTO only if date parsing was successful (or date wasn't provided)
      final AddItemCommandDto commandDto = new AddItemCommandDto(name, quantity, location, expDate);

      // Validate the DTO
      final Set<ConstraintViolation<AddItemCommandDto>> violations = VALIDATOR.validate(commandDto);
      if (violations.isEmpty()) {
        // Proceed only if validation passed
        try {
          final InventoryItem addedItem = inventoryService.addItem(commandDto);
          if (LOG.isInfoEnabled()) {
            LOG.info("Successfully added item:");
            LOG.info("  ID: {}", addedItem.getItemId());
            LOG.info("  Name: {}", addedItem.getName());
            LOG.info("  Quantity: {}", addedItem.getQuantity());
            LOG.info("  Location: {}", addedItem.getLocation());
            addedItem.getExpirationDate().ifPresent(date -> LOG.info("  Expires: {}", date));
          }
          // exitCode remains 0 (success)
        } catch (RuntimeException e) { // Catching RuntimeException at command boundary is okay
          if (LOG.isErrorEnabled()) {
            LOG.error("An unexpected error occurred while adding the item: {}", e.getMessage(), e);
          }
          exitCode = 1;
        }
      } else {
        // Validation failed
        if (LOG.isErrorEnabled()) {
          LOG.error("Error: Invalid item data:");
          for (final ConstraintViolation<AddItemCommandDto> violation : violations) {
            LOG.error("  - {}", violation.getMessage());
          }
        }
        exitCode = 1;
      }
    }
    return exitCode; // Single return point
  }

  /**
   * Parses the expiration date string. Returns empty Optional on error.
   *
   * @return Optional containing the parsed {@link LocalDate} or empty.
   */
  private Optional<LocalDate> parseExpirationDate() {
    Optional<LocalDate> resultExpDate = Optional.empty();
    if (expirationDateStr != null && !expirationDateStr.isBlank()) {
      try {
        resultExpDate = Optional.of(LocalDate.parse(expirationDateStr));
      } catch (DateTimeParseException e) {
        if (LOG.isErrorEnabled()) {
          LOG.error("Error: Invalid date format '{}'. Please use YYYY-MM-DD.", expirationDateStr);
        }
        // result remains empty
      }
    }
    return resultExpDate; // Single return point
  }
}
