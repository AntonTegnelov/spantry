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
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
class ListItemsCommandTest {

  @Mock private InventoryService mockInventoryService;

  @InjectMocks private ListItemsCommand listItemsCommand;

  // --- Log Capture Setup ---
  private ListAppender listAppender;
  private Logger commandLogger;

  @BeforeEach
  void setUp() {
    // Setup Logback capture
    commandLogger = (Logger) LoggerFactory.getLogger(ListItemsCommand.class);
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
  void call_NoFilter_ShouldCallGetAllItemsAndPrintAll() {
    // Arrange
    InventoryItem item1 =
        new InventoryItem(UUID.randomUUID().toString(), "Milk", 1, Location.FRIDGE, null);
    InventoryItem item2 =
        new InventoryItem(
            UUID.randomUUID().toString(), "Bread", 1, Location.PANTRY, LocalDate.now().plusDays(5));
    List<InventoryItem> allItems = Arrays.asList(item1, item2);
    when(mockInventoryService.getAllItems()).thenReturn(allItems);

    // Act
    int exitCode = listItemsCommand.call();

    // Assert
    assertEquals(0, exitCode);
    verify(mockInventoryService).getAllItems();
    verify(mockInventoryService, never()).getItemsByLocation(any());

    // Assertions check logs for table and status
    assertTrue(logsContain("Listing all inventory items:"), "Log should contain header.");
    assertTrue(logsContain("ID"), "Log should contain table header ID."); // Check header part
    assertTrue(logsContain(item1.itemId()), "Log should contain item1 ID.");
    assertTrue(logsContain(item1.name()), "Log should contain item1 name.");
    assertTrue(logsContain(item2.itemId()), "Log should contain item2 ID.");
    assertTrue(logsContain(item2.name()), "Log should contain item2 name.");
    assertTrue(logsContain(item2.expirationDate().toString()), "Log should contain item2 expiry.");
    assertTrue(logsContain("N/A"), "Log should contain N/A for item1 expiry.");
    assertFalse(hasErrorLogs(), "No ERROR level logs expected.");
  }

  @Test
  void call_WithLocationFilter_ShouldCallGetItemsByLocationAndPrintFiltered() throws Exception {
    // Arrange
    Location filterLocation = Location.FRIDGE;
    InventoryItem item1 =
        new InventoryItem(UUID.randomUUID().toString(), "Milk", 1, Location.FRIDGE, null);
    InventoryItem item2 =
        new InventoryItem(
            UUID.randomUUID().toString(),
            "Cheese",
            1,
            Location.FRIDGE,
            LocalDate.now().plusDays(10));
    List<InventoryItem> fridgeItems = Arrays.asList(item1, item2);

    setField(listItemsCommand, "location", filterLocation);
    when(mockInventoryService.getItemsByLocation(filterLocation)).thenReturn(fridgeItems);

    // Act
    int exitCode = listItemsCommand.call();

    // Assert
    assertEquals(0, exitCode);
    verify(mockInventoryService, never()).getAllItems();
    verify(mockInventoryService).getItemsByLocation(filterLocation);

    // Assertions check logs for table and status
    assertTrue(
        logsContain("Listing items in location: " + filterLocation),
        "Log should contain filtered header.");
    assertTrue(logsContain("ID"), "Log should contain table header ID."); // Check header part
    assertTrue(logsContain(item1.itemId()), "Log should contain item1 ID.");
    assertTrue(logsContain(item1.name()), "Log should contain item1 name.");
    assertTrue(logsContain(item2.itemId()), "Log should contain item2 ID.");
    assertTrue(logsContain(item2.name()), "Log should contain item2 name.");
    assertTrue(logsContain(item2.expirationDate().toString()), "Log should contain item2 expiry.");
    assertFalse(logsContain("Bread"), "Log should not contain items from other locations.");
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

    // Assertions check logs
    assertTrue(logsContain("Listing all inventory items:"), "Log should contain header.");
    assertTrue(logsContain("No items found in inventory."), "Log message for no items not found.");
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

    // Assert that the initial status message was logged, but no table details.
    assertTrue(logsContain("Listing all inventory items:"));
    assertFalse(logsContain("  ID "), "Table header ID should not be logged on error.");
    assertFalse(logsContain(" Name "), "Table header Name should not be logged on error.");
  }

  // Helper to check if any ERROR level log messages exist
  private boolean hasErrorLogs() {
    return listAppender.getEvents().stream().anyMatch(event -> event.getLevel() == Level.ERROR);
  }
}
