package com.spantry.cli.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.spantry.inventory.domain.InventoryItem;
import com.spantry.inventory.domain.Location;
import com.spantry.inventory.service.InventoryService;
import com.spantry.inventory.service.dto.AddItemCommandDto;
import com.spantry.testsupport.ListAppender;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

// Needed for reflection setting

@ExtendWith(MockitoExtension.class)
class AddItemCommandTest {

  @Mock private InventoryService mockInventoryService;

  // Use AddItemCommand directly, InjectMocks doesn't work well with picocli's private fields +
  // validator init
  private AddItemCommand addItemCommand;

  @Captor private ArgumentCaptor<AddItemCommandDto> dtoCaptor;

  // --- Log Capture Setup ---
  private ListAppender listAppender;
  private Logger commandLogger;

  @BeforeEach
  void setUp() {
    // Instantiate the command with the mock service
    addItemCommand = new AddItemCommand(mockInventoryService);

    // Setup Logback capture
    commandLogger = (Logger) LoggerFactory.getLogger(AddItemCommand.class);
    listAppender = new ListAppender();
    listAppender.start();
    commandLogger.addAppender(listAppender);
  }

  @AfterEach
  void tearDown() {
    // Detach appender and stop it
    if (commandLogger != null && listAppender != null) {
      commandLogger.detachAppender(listAppender);
      listAppender.stop();
    }
  }

  // Helper to check if log messages contain specific text
  private boolean logsContain(String text) {
    return listAppender.getEvents().stream()
        .map(ILoggingEvent::getFormattedMessage)
        .anyMatch(msg -> msg != null && msg.contains(text));
  }

  // Helper to get log messages as a list of strings
  private List<String> getLogMessages() {
    return listAppender.getEvents().stream()
        .map(ILoggingEvent::getFormattedMessage)
        .collect(Collectors.toList());
  }

  // Helper to set private fields using reflection
  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  void call_ValidInput_ShouldCallServiceAndSucceed() throws Exception {
    // Arrange
    String itemName = "Milk";
    int quantity = 1;
    Location location = Location.FRIDGE;
    String expiryStr = "2024-12-31";
    LocalDate expiryDate = LocalDate.parse(expiryStr);
    String generatedId = UUID.randomUUID().toString();

    setField(addItemCommand, "name", itemName);
    setField(addItemCommand, "quantity", quantity);
    setField(addItemCommand, "location", location);
    setField(addItemCommand, "expirationDateStr", expiryStr);

    InventoryItem expectedItem =
        new InventoryItem(generatedId, itemName, quantity, location, Optional.of(expiryDate));
    when(mockInventoryService.addItem(any(AddItemCommandDto.class))).thenReturn(expectedItem);

    // Act
    int exitCode = addItemCommand.call();

    // Assert
    assertEquals(0, exitCode, "Exit code should be 0 for success.");
    verify(mockInventoryService).addItem(dtoCaptor.capture());
    AddItemCommandDto capturedDto = dtoCaptor.getValue();
    assertEquals(itemName, capturedDto.name());
    assertEquals(quantity, capturedDto.quantity());
    assertEquals(location, capturedDto.location());
    assertTrue(capturedDto.expirationDate().isPresent());
    assertEquals(expiryDate, capturedDto.expirationDate().get());

    // Assert that success messages are logged
    assertTrue(logsContain("Successfully added item:"), "Success message expected in logs.");
    assertTrue(logsContain("ID: " + generatedId), "Item ID should be in logs.");
    assertTrue(logsContain("Name: " + itemName), "Item name should be in logs.");
    assertTrue(logsContain("Expires: " + expiryStr), "Expiry date should be in logs.");
  }

  @Test
  void call_ValidInputNoExpiry_ShouldCallServiceAndSucceed() throws Exception {
    // Arrange
    String itemName = "Bread";
    int quantity = 1;
    Location location = Location.PANTRY;

    setField(addItemCommand, "name", itemName);
    setField(addItemCommand, "quantity", quantity);
    setField(addItemCommand, "location", location);
    setField(addItemCommand, "expirationDateStr", null); // No expiry provided

    InventoryItem expectedItem =
        new InventoryItem(
            UUID.randomUUID().toString(), itemName, quantity, location, Optional.empty());
    when(mockInventoryService.addItem(any(AddItemCommandDto.class))).thenReturn(expectedItem);

    // Act
    int exitCode = addItemCommand.call();

    // Assert
    assertEquals(0, exitCode);
    verify(mockInventoryService).addItem(dtoCaptor.capture());
    assertFalse(
        dtoCaptor.getValue().expirationDate().isPresent(), "Expiration date should be empty.");
    // Assert success message is logged
    assertTrue(logsContain("Successfully added item:"), "Success message expected in logs.");
    // Assert 'Expires' is NOT logged
    assertFalse(logsContain("Expires:"), "Expires line should not be logged.");
  }

  @Test
  void call_InvalidQuantity_ShouldFailValidationAndReturnErrorCode() throws Exception {
    // Arrange
    setField(addItemCommand, "name", "Eggs");
    setField(addItemCommand, "quantity", 0); // Invalid quantity
    setField(addItemCommand, "location", Location.FRIDGE);
    setField(addItemCommand, "expirationDateStr", null);

    // Act
    int exitCode = addItemCommand.call();

    // Assert
    assertEquals(1, exitCode, "Exit code should be 1 for validation error.");
    verify(mockInventoryService, never()).addItem(any()); // Service should not be called
    assertTrue(
        logsContain("Error: Invalid item data:"), "Validation error header expected in logs.");
    assertTrue(
        logsContain("Item quantity must be positive."),
        "Specific validation message expected in logs.");
  }

  @Test
  void call_BlankName_ShouldFailValidationAndReturnErrorCode() throws Exception {
    // Arrange
    setField(addItemCommand, "name", " "); // Invalid name
    setField(addItemCommand, "quantity", 12);
    setField(addItemCommand, "location", Location.FRIDGE);
    setField(addItemCommand, "expirationDateStr", null);

    // Act
    int exitCode = addItemCommand.call();

    // Assert
    assertEquals(1, exitCode);
    verify(mockInventoryService, never()).addItem(any());
    assertTrue(logsContain("Error: Invalid item data:"));
    assertTrue(
        logsContain("Item name cannot be blank."), "Specific validation message expected in logs.");
  }

  @Test
  void call_InvalidDateFormat_ShouldFailParsingAndReturnErrorCode() throws Exception {
    // Arrange
    setField(addItemCommand, "name", "Cheese");
    setField(addItemCommand, "quantity", 1);
    setField(addItemCommand, "location", Location.FRIDGE);
    setField(addItemCommand, "expirationDateStr", "31-12-2024"); // Invalid format

    // Act
    int exitCode = addItemCommand.call();

    // Assert
    assertEquals(1, exitCode, "Exit code should be 1 for date parsing error.");
    verify(mockInventoryService, never()).addItem(any()); // Service should not be called
    assertTrue(logsContain("Error: Invalid date format"), "Date format error expected in logs.");
  }

  @Test
  void call_ServiceThrowsException_ShouldPrintErrorAndReturnErrorCode() throws Exception {
    // Arrange
    String itemName = "Yogurt";
    int quantity = 6;
    Location location = Location.FRIDGE;

    setField(addItemCommand, "name", itemName);
    setField(addItemCommand, "quantity", quantity);
    setField(addItemCommand, "location", location);
    setField(addItemCommand, "expirationDateStr", null);

    String errorMessage = "Database connection failed";
    when(mockInventoryService.addItem(any(AddItemCommandDto.class)))
        .thenThrow(new RuntimeException(errorMessage));

    // Act
    int exitCode = addItemCommand.call();

    // Assert
    assertEquals(1, exitCode, "Exit code should be 1 for service error.");
    verify(mockInventoryService).addItem(any(AddItemCommandDto.class));
    assertTrue(
        logsContain("An unexpected error occurred"), "Generic error message expected in logs.");
    assertTrue(
        listAppender.getEvents().stream()
            .filter(event -> event.getLevel() == Level.ERROR)
            .anyMatch(
                event ->
                    event.getThrowableProxy() != null
                        && event.getThrowableProxy().getMessage().contains(errorMessage)),
        "Specific error message should be in the logged exception.");
  }
}
