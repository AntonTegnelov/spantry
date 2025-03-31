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
  // Suppress warnings as they're now handled at the class level
  public Integer call() {
    // Initialize to failure by default - safer
    int exitCode = 1;
    final LocalDate expDate = parseExpirationDate(); // Now returns LocalDate or null

    // Check if parsing failed (indicated by non-null input string but null result date)
    if (!hasDateParsingFailed(expDate)) {
      exitCode = processValidCommand(expDate);
    }

    return exitCode; // Single return point
  }

  /**
   * Checks if date parsing failed.
   *
   * @param parsedDate the parsed date result
   * @return true if parsing failed, false otherwise
   */
  private boolean hasDateParsingFailed(final LocalDate parsedDate) {
    return expirationDateStr != null && !expirationDateStr.isBlank() && parsedDate == null;
  }

  /**
   * Processes a command with valid date (or no date).
   *
   * @param expDate the expiration date (may be null)
   * @return exit code (0 for success, 1 for error)
   */
  private int processValidCommand(final LocalDate expDate) {
    // Create DTO with potentially null LocalDate
    final AddItemCommandDto commandDto = new AddItemCommandDto(name, quantity, location, expDate);

    // Validate the DTO
    final Set<ConstraintViolation<AddItemCommandDto>> violations = VALIDATOR.validate(commandDto);

    int exitCode = 1; // Default to error
    if (violations.isEmpty()) {
      exitCode = addItemWithValidatedCommand(commandDto);
    } else {
      // Validation failed
      logValidationErrors(violations);
    }
    return exitCode;
  }

  /**
   * Adds an item using a validated command.
   *
   * @param commandDto the validated command
   * @return exit code (0 for success, 1 for error)
   */
  private int addItemWithValidatedCommand(final AddItemCommandDto commandDto) {
    int exitCode = 1; // Default to error
    try {
      final InventoryItem addedItem = inventoryService.addItem(commandDto);
      logSuccessfulAdd(addedItem);
      exitCode = 0; // Success!
    } catch (RuntimeException e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("An unexpected error occurred while adding the item: {}", e.getMessage(), e);
      }
    }
    return exitCode;
  }

  /**
   * Logs validation errors.
   *
   * @param violations the validation constraint violations
   */
  private void logValidationErrors(final Set<ConstraintViolation<AddItemCommandDto>> violations) {
    if (LOG.isErrorEnabled()) {
      LOG.error("Error: Invalid item data:");
      for (final ConstraintViolation<AddItemCommandDto> violation : violations) {
        LOG.error("  - {}", violation.getMessage());
      }
    }
  }

  /**
   * Logs successful item addition.
   *
   * @param addedItem the added inventory item
   */
  private void logSuccessfulAdd(final InventoryItem addedItem) {
    if (LOG.isInfoEnabled()) {
      LOG.info("Successfully added item:");
      LOG.info("  ID: {}", addedItem.itemId());
      LOG.info("  Name: {}", addedItem.name());
      LOG.info("  Quantity: {}", addedItem.quantity());
      LOG.info("  Location: {}", addedItem.location());
      // Use the optional getter for logging
      addedItem.getExpirationDateOptional().ifPresent(date -> LOG.info("  Expires: {}", date));
    }
  }

  /**
   * Parses the expiration date string. Returns null on error or if input is null/blank.
   *
   * @return Parsed {@link LocalDate} or null.
   */
  private LocalDate parseExpirationDate() { // Return type changed
    LocalDate resultExpDate = null; // Default to null
    if (expirationDateStr != null && !expirationDateStr.isBlank()) {
      try {
        resultExpDate = LocalDate.parse(expirationDateStr);
      } catch (DateTimeParseException e) {
        if (LOG.isErrorEnabled()) {
          LOG.error("Error: Invalid date format '{}'. Please use YYYY-MM-DD.", expirationDateStr);
        }
        // result remains null
      }
    }
    return resultExpDate;
  }
}
