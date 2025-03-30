package com.spantry.cli.command;

import com.spantry.inventory.service.InventoryService;
import com.spantry.inventory.service.exception.ItemNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemoveItemCommandTest {

    @Mock
    private InventoryService mockInventoryService;

    @InjectMocks
    private RemoveItemCommand removeItemCommand;

    // For capturing console output
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
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
        String output = outContent.toString();
        assertTrue(output.contains("Successfully removed item with ID: " + itemIdToRemove));
        assertTrue(errContent.toString().isEmpty());
    }

    @Test
    void call_ItemNotFound_ShouldPrintErrorAndReturnErrorCode() throws Exception {
        // Arrange
        String nonExistentItemId = UUID.randomUUID().toString();
        setField(removeItemCommand, "itemId", nonExistentItemId);

        String errorMessage = "Item not found with ID: " + nonExistentItemId;
        doThrow(new ItemNotFoundException(errorMessage)).when(mockInventoryService).removeItem(nonExistentItemId);

        // Act
        int exitCode = removeItemCommand.call();

        // Assert
        assertEquals(1, exitCode, "Exit code should be 1 for item not found.");
        verify(mockInventoryService).removeItem(nonExistentItemId);
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("Error: " + errorMessage));
        assertTrue(outContent.toString().isEmpty());
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
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("Error: Item ID must be provided."));
        assertTrue(outContent.toString().isEmpty());
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
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("Error: Item ID must be provided."));
        assertTrue(outContent.toString().isEmpty());
    }

    @Test
    void call_ServiceThrowsUnexpectedException_ShouldPrintErrorAndReturnErrorCode() throws Exception {
        // Arrange
        String itemIdToRemove = UUID.randomUUID().toString();
        setField(removeItemCommand, "itemId", itemIdToRemove);

        String errorMessage = "Unexpected database error";
        doThrow(new RuntimeException(errorMessage)).when(mockInventoryService).removeItem(itemIdToRemove);

        // Act
        int exitCode = removeItemCommand.call();

        // Assert
        assertEquals(1, exitCode, "Exit code should be 1 for unexpected service error.");
        verify(mockInventoryService).removeItem(itemIdToRemove);
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("An unexpected error occurred while removing the item: " + errorMessage));
        assertTrue(outContent.toString().isEmpty());
    }
} 