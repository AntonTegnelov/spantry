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
import com.spantry.testsupport.ListAppender;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
class ListItemsCommandTest {

  @Mock private InventoryService mockInventoryService;

  @InjectMocks private ListItemsCommand listItemsCommand;

  // --- Log Capture Setup ---
  private ListAppender listAppender;
  private Logger commandLogger;

  // Still need to capture System.out for non-error output
  private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  @BeforeEach
  void setUp() {
    // Setup Logback capture
    commandLogger = (Logger) LoggerFactory.getLogger(ListItemsCommand.class);
    listAppender = new ListAppender();
    listAppender.start();
    commandLogger.addAppender(listAppender);

    // Redirect System.out and clear buffer
    outContent.reset(); // Clear buffer before each test
    System.setOut(new PrintStream(outContent));
  }

  @AfterEach
  void tearDown() {
    // Detach appender and stop it
    if (commandLogger != null && listAppender != null) {
      commandLogger.detachAppender(listAppender);
      listAppender.stop();
    }
    // Restore original System.out
    System.setOut(originalOut);
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
  void call_NoFilter_ShouldCallGetAllItemsAndPrintAll() {
    // Arrange
    InventoryItem item1 =
        new InventoryItem(
            UUID.randomUUID().toString(), "Milk", 1, Location.FRIDGE, Optional.empty());
    InventoryItem item2 =
        new InventoryItem(
            UUID.randomUUID().toString(),
            "Bread",
            1,
            Location.PANTRY,
            Optional.of(LocalDate.now().plusDays(5)));
    List<InventoryItem> allItems = Arrays.asList(item1, item2);
    when(mockInventoryService.getAllItems()).thenReturn(allItems);

    // Act
    int exitCode = listItemsCommand.call();

    // Assert
    assertEquals(0, exitCode);
    verify(mockInventoryService).getAllItems();
    verify(mockInventoryService, never()).getItemsByLocation(any());

    // Assertions check System.out for table, logs for errors/status
    String output = outContent.toString();
    assertTrue(output.contains("Listing all inventory items:")); // Check console output header
    assertTrue(output.contains(item1.getItemId()));
    assertTrue(output.contains(item1.getName()));
    assertTrue(output.contains(item2.getItemId()));
    assertTrue(output.contains(item2.getName()));
    assertTrue(output.contains(item2.getExpirationDate().get().toString()));
    assertTrue(output.contains("N/A")); // For item1 expiry
    assertFalse(hasErrorLogs(), "No ERROR level logs expected.");
  }

  @Test
  void call_WithLocationFilter_ShouldCallGetItemsByLocationAndPrintFiltered() throws Exception {
    // Arrange
    Location filterLocation = Location.FRIDGE;
    InventoryItem item1 =
        new InventoryItem(
            UUID.randomUUID().toString(), "Milk", 1, Location.FRIDGE, Optional.empty());
    InventoryItem item2 =
        new InventoryItem(
            UUID.randomUUID().toString(),
            "Cheese",
            1,
            Location.FRIDGE,
            Optional.of(LocalDate.now().plusDays(10)));
    List<InventoryItem> fridgeItems = Arrays.asList(item1, item2);

    setField(listItemsCommand, "location", filterLocation);
    when(mockInventoryService.getItemsByLocation(filterLocation)).thenReturn(fridgeItems);

    // Act
    int exitCode = listItemsCommand.call();

    // Assert
    assertEquals(0, exitCode);
    verify(mockInventoryService, never()).getAllItems();
    verify(mockInventoryService).getItemsByLocation(filterLocation);

    // Assertions check System.out for table, logs for errors/status
    String output = outContent.toString();
    assertTrue(
        output.contains("Listing items in location: " + filterLocation)); // Check console header
    assertTrue(output.contains(item1.getItemId()));
    assertTrue(output.contains(item1.getName()));
    assertTrue(output.contains(item2.getItemId()));
    assertTrue(output.contains(item2.getName()));
    assertTrue(output.contains(item2.getExpirationDate().get().toString()));
    assertFalse(output.contains("Bread")); // Should not contain items from other locations
    assertFalse(hasErrorLogs(), "No ERROR level logs expected.");
  }

  @Test
  void call_NoItemsFound_ShouldPrintNoItemsMessage() {
    // Arrange
    when(mockInventoryService.getAllItems()).thenReturn(Collections.emptyList());

    // Act
    int exitCode = listItemsCommand.call();

    // Assert
    assertEquals(0, exitCode);
    verify(mockInventoryService).getAllItems();

    // Assertions check System.out for table, logs for errors/status
    String output = outContent.toString();
    assertTrue(output.contains("Listing all inventory items:")); // Check console header
    assertTrue(output.contains("No items found.")); // Check console message
    assertFalse(hasErrorLogs(), "No ERROR level logs expected.");
  }

  @Test
  void call_ServiceThrowsException_ShouldPrintErrorAndReturnErrorCode() {
    // Arrange
    String errorMessage = "Failed to connect";
    when(mockInventoryService.getAllItems()).thenThrow(new RuntimeException(errorMessage));

    // Act
    int exitCode = listItemsCommand.call();

    // Assert
    assertEquals(1, exitCode);
    verify(mockInventoryService).getAllItems();
    assertTrue(logsContain("Error listing items:"), "Error message expected in logs.");
    assertTrue(
        listAppender.getEvents().stream()
            .filter(event -> event.getLevel() == Level.ERROR)
            .anyMatch(
                event ->
                    event.getFormattedMessage() != null
                        && event.getFormattedMessage().contains(errorMessage)),
        "Specific error message should be logged.");

    // Assert that the initial status message was printed to System.out (via logger),
    // but no table header details.
    String output = outContent.toString();
    assertTrue(output.contains("Listing all inventory items:"));
    assertFalse(output.contains("  ID "), "Table header ID should not be printed on error.");
    assertFalse(output.contains(" Name "), "Table header Name should not be printed on error.");
  }

  // Helper to check if any ERROR level log messages exist
  private boolean hasErrorLogs() {
    return listAppender.getEvents().stream().anyMatch(event -> event.getLevel() == Level.ERROR);
  }
}
