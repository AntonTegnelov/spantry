package com.spantry.cli.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.spantry.inventory.service.InventoryService;
import com.spantry.inventory.service.exception.ItemNotFoundException;
import com.spantry.testsupport.ListAppender;
import java.lang.reflect.Field;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class RemoveItemCommandTest {

  @Mock private InventoryService mockInventoryService;

  @InjectMocks private RemoveItemCommand removeItemCommand;

  // --- Log Capture Setup ---
  private ListAppender listAppender;
  private Logger commandLogger;

  @BeforeEach
  void setUp() {
    // Setup Logback capture
    commandLogger = (Logger) LoggerFactory.getLogger(RemoveItemCommand.class);
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

  // Helper to set private fields using reflection
  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Test
  void call_ValidId_ShouldCallServiceAndSucceed() throws Exception {
    // Arrange
    String itemIdToRemove = UUID.randomUUID().toString();
    setField(removeItemCommand, "itemId", itemIdToRemove);

    // Configure mock to do nothing when removeItem is called (void method)
    doNothing().when(mockInventoryService).removeItem(itemIdToRemove);

    // Act
    int exitCode = removeItemCommand.call();

    // Assert
    assertEquals(0, exitCode, "Exit code should be 0 for success.");
    verify(mockInventoryService).removeItem(itemIdToRemove);
    // Check log for success message
    assertTrue(logsContain("Successfully removed item with ID: " + itemIdToRemove));
  }

  @Test
  void call_ItemNotFound_ShouldPrintErrorAndReturnErrorCode() throws Exception {
    // Arrange
    String nonExistentItemId = UUID.randomUUID().toString();
    setField(removeItemCommand, "itemId", nonExistentItemId);

    String errorMessage = "Item not found with ID: " + nonExistentItemId;
    doThrow(new ItemNotFoundException(errorMessage))
        .when(mockInventoryService)
        .removeItem(nonExistentItemId);

    // Act
    int exitCode = removeItemCommand.call();

    // Assert
    assertEquals(1, exitCode, "Exit code should be 1 for item not found.");
    verify(mockInventoryService).removeItem(nonExistentItemId);
    // String errorOutput = errContent.toString(); // Replaced with log assertion
    assertTrue(logsContain("Error: " + errorMessage), "Item not found error expected in logs.");
  }

  @Test
  void call_NullId_ShouldPrintErrorAndReturnErrorCode() throws Exception {
    // Arrange
    setField(removeItemCommand, "itemId", null); // Simulate picocli not setting the field

    // Act
    int exitCode = removeItemCommand.call();

    // Assert
    assertEquals(1, exitCode, "Exit code should be 1 for missing ID.");
    verify(mockInventoryService, never()).removeItem(any());
    // String errorOutput = errContent.toString(); // Replaced with log assertion
    assertTrue(
        logsContain("Error: Item ID must be provided."), "Missing ID error expected in logs.");
  }

  @Test
  void call_BlankId_ShouldPrintErrorAndReturnErrorCode() throws Exception {
    // Arrange
    setField(removeItemCommand, "itemId", "  "); // Blank ID

    // Act
    int exitCode = removeItemCommand.call();

    // Assert
    assertEquals(1, exitCode, "Exit code should be 1 for blank ID.");
    verify(mockInventoryService, never()).removeItem(any());
    // String errorOutput = errContent.toString(); // Replaced with log assertion
    assertTrue(logsContain("Error: Item ID must be provided."), "Blank ID error expected in logs.");
  }

  @Test
  void call_ServiceThrowsUnexpectedException_ShouldPrintErrorAndReturnErrorCode() throws Exception {
    // Arrange
    String itemIdToRemove = UUID.randomUUID().toString();
    setField(removeItemCommand, "itemId", itemIdToRemove);

    String errorMessage = "Unexpected database error";
    doThrow(new RuntimeException(errorMessage))
        .when(mockInventoryService)
        .removeItem(itemIdToRemove);

    // Act
    int exitCode = removeItemCommand.call();

    // Assert
    assertEquals(1, exitCode, "Exit code should be 1 for unexpected service error.");
    verify(mockInventoryService).removeItem(itemIdToRemove);
    // String errorOutput = errContent.toString(); // Replaced with log assertion
    assertTrue(
        logsContain("An unexpected error occurred while removing the item:"),
        "Generic error message expected in logs.");
    // Assert that the specific error message is part of the logged exception details
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
