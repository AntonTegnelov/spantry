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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class AddItemCommandTest {

  @Mock private InventoryService mockInventoryService;

  @InjectMocks private AddItemCommand addItemCommand;

  @Captor private ArgumentCaptor<AddItemCommandDto> commandDtoCaptor;

  // --- Log Capture Setup ---
  private ListAppender listAppender;
  private Logger commandLogger;

  @BeforeEach
  void setUp() {
    commandLogger = (Logger) LoggerFactory.getLogger(AddItemCommand.class);
    listAppender = new ListAppender();
    listAppender.start();
    commandLogger.addAppender(listAppender);
  }

  @AfterEach
  void tearDown() {
    if (commandLogger != null && listAppender != null) {
      commandLogger.detachAppender(listAppender);
      listAppender.stop();
    }
  }

  // Helper to set private fields using reflection
  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  // Helper to check if log messages contain specific text
  private boolean logsContain(String text) {
    return listAppender.getEvents().stream()
        .map(ILoggingEvent::getFormattedMessage)
        .anyMatch(msg -> msg != null && msg.contains(text));
  }

  // Helper to check for error logs
  private boolean hasErrorLogs() {
    return listAppender.getEvents().stream().anyMatch(event -> event.getLevel() == Level.ERROR);
  }

  @Test
  void call_ValidInput_ShouldCallServiceAndSucceed() throws Exception {
    // Arrange
    final String name = "Milk";
    final int quantity = 1;
    final Location location = Location.FRIDGE;
    final String expiryStr = "2024-12-31";
    final LocalDate expiryDate = LocalDate.parse(expiryStr);
    final String generatedId = "f11c1743-c557-45f7-a23a-499865d5d66c";

    setField(addItemCommand, "name", name);
    setField(addItemCommand, "quantity", quantity);
    setField(addItemCommand, "location", location);
    setField(addItemCommand, "expirationDateStr", expiryStr);

    final InventoryItem expectedSavedItem =
        new InventoryItem(generatedId, name, quantity, location, expiryDate);
    when(mockInventoryService.addItem(any(AddItemCommandDto.class))).thenReturn(expectedSavedItem);

    // Act
    int exitCode = addItemCommand.call();

    // Assert
    assertEquals(0, exitCode, "Exit code should be 0 for success");
    verify(mockInventoryService).addItem(commandDtoCaptor.capture());
    AddItemCommandDto capturedDto = commandDtoCaptor.getValue();
    assertEquals(name, capturedDto.name());
    assertEquals(quantity, capturedDto.quantity());
    assertEquals(location, capturedDto.location());
    assertEquals(expiryDate, capturedDto.expirationDate()); // Check LocalDate directly

    assertTrue(logsContain("Successfully added item:"), "Success message should be logged.");
    assertTrue(logsContain("ID: " + generatedId), "Generated ID should be logged.");
    assertTrue(logsContain("Expires: " + expiryStr), "Expiration date should be logged.");
    assertFalse(hasErrorLogs(), "No errors should be logged.");
  }

  @Test
  void call_ValidInputNoExpiry_ShouldCallServiceWithNullDate() throws Exception {
    // Arrange
    final String name = "Flour";
    final int quantity = 1;
    final Location location = Location.PANTRY;
    final String generatedId = "id-no-expiry";

    setField(addItemCommand, "name", name);
    setField(addItemCommand, "quantity", quantity);
    setField(addItemCommand, "location", location);
    setField(addItemCommand, "expirationDateStr", null); // No expiry provided

    final InventoryItem expectedSavedItem =
        new InventoryItem(generatedId, name, quantity, location, null); // Expect null LocalDate
    when(mockInventoryService.addItem(any(AddItemCommandDto.class))).thenReturn(expectedSavedItem);

    // Act
    int exitCode = addItemCommand.call();

    // Assert
    assertEquals(0, exitCode);
    verify(mockInventoryService).addItem(commandDtoCaptor.capture());
    AddItemCommandDto capturedDto = commandDtoCaptor.getValue();
    assertEquals(name, capturedDto.name());
    assertEquals(quantity, capturedDto.quantity());
    assertEquals(location, capturedDto.location());
    assertEquals(null, capturedDto.expirationDate()); // Verify null date in DTO

    assertTrue(logsContain("Successfully added item:"));
    assertTrue(logsContain("ID: " + generatedId));
    assertFalse(logsContain("Expires:"), "Expires line should not be logged.");
    assertFalse(hasErrorLogs());
  }

  @Test
  void call_InvalidDateFormat_ShouldFailParsingAndReturnErrorCode() throws Exception {
    // Arrange
    setField(addItemCommand, "name", "Test");
    setField(addItemCommand, "quantity", 1);
    setField(addItemCommand, "location", Location.OTHER);
    setField(addItemCommand, "expirationDateStr", "31-12-2024"); // Invalid format

    // Act
    int exitCode = addItemCommand.call();

    // Assert
    assertEquals(1, exitCode, "Exit code should be 1 for error");
    verify(mockInventoryService, never()).addItem(any());
    assertTrue(logsContain("Invalid date format"), "Error message for date format expected.");
    assertTrue(hasErrorLogs(), "Error should be logged.");
  }

  @Test
  void call_InvalidQuantity_ShouldFailValidationAndReturnErrorCode() throws Exception {
    // Arrange
    setField(addItemCommand, "name", "Test");
    setField(addItemCommand, "quantity", 0); // Invalid quantity
    setField(addItemCommand, "location", Location.OTHER);
    setField(addItemCommand, "expirationDateStr", null);

    // Mock validator behavior (though AddItemCommand uses static validator directly)
    // This test primarily checks if the command handles validation results.

    // Act
    int exitCode = addItemCommand.call();

    // Assert
    assertEquals(1, exitCode, "Exit code should be 1 for validation error");
    verify(mockInventoryService, never()).addItem(any());
    assertTrue(logsContain("Invalid item data"), "Validation error header expected.");
    assertTrue(
        logsContain("Item quantity must be positive"), "Quantity validation message expected.");
    assertTrue(hasErrorLogs(), "Error should be logged.");
  }

  @Test
  void call_BlankName_ShouldFailValidationAndReturnErrorCode() throws Exception {
    // Arrange
    setField(addItemCommand, "name", " "); // Blank name
    setField(addItemCommand, "quantity", 1);
    setField(addItemCommand, "location", Location.OTHER);
    setField(addItemCommand, "expirationDateStr", null);

    // Act
    int exitCode = addItemCommand.call();

    // Assert
    assertEquals(1, exitCode);
    verify(mockInventoryService, never()).addItem(any());
    assertTrue(logsContain("Invalid item data"));
    assertTrue(logsContain("Item name cannot be blank"));
    assertTrue(hasErrorLogs());
  }

  @Test
  void call_ServiceThrowsException_ShouldLogErrorAndReturnErrorCode() throws Exception {
    // Arrange
    final String name = "Error Item";
    final int quantity = 1;
    final Location location = Location.OTHER;
    final String errorMessage = "Database connection failed";

    setField(addItemCommand, "name", name);
    setField(addItemCommand, "quantity", quantity);
    setField(addItemCommand, "location", location);
    setField(addItemCommand, "expirationDateStr", null);

    when(mockInventoryService.addItem(any(AddItemCommandDto.class)))
        .thenThrow(new RuntimeException(errorMessage));

    // Act
    int exitCode = addItemCommand.call();

    // Assert
    assertEquals(1, exitCode);
    verify(mockInventoryService).addItem(any(AddItemCommandDto.class)); // Service is called
    assertTrue(logsContain("An unexpected error occurred"), "Generic error message expected.");
    assertTrue(hasErrorLogs(), "Error should be logged.");
    assertFalse(logsContain("Successfully added item:"), "Success message should not be logged.");
  }
}
