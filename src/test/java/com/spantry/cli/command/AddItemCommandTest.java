package com.spantry.cli.command;

import com.spantry.inventory.domain.Item;
import com.spantry.inventory.domain.Location;
import com.spantry.inventory.service.InventoryService;
import com.spantry.inventory.service.dto.AddItemCommandDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import picocli.CommandLine; // Needed for reflection setting

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddItemCommandTest {

    @Mock
    private InventoryService mockInventoryService;

    // Use AddItemCommand directly, InjectMocks doesn't work well with picocli's private fields + validator init
    private AddItemCommand addItemCommand;

    @Captor
    private ArgumentCaptor<AddItemCommandDto> dtoCaptor;

    // For capturing console output
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        // Instantiate the command with the mock service
        addItemCommand = new AddItemCommand(mockInventoryService);

        // Redirect System.out and System.err
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void restoreStreams() {
        // Restore original System.out and System.err
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

        Item expectedItem = new Item(generatedId, itemName, quantity, location, Optional.of(expiryDate));
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

        String output = outContent.toString();
        assertTrue(output.contains("Successfully added item:"), "Success message expected.");
        assertTrue(output.contains("ID: " + generatedId), "Item ID should be in output.");
        assertTrue(output.contains("Name: " + itemName), "Item name should be in output.");
        assertTrue(output.contains("Expires: " + expiryStr), "Expiry date should be in output.");
        assertTrue(errContent.toString().isEmpty(), "Error stream should be empty.");
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

        Item expectedItem = new Item(UUID.randomUUID().toString(), itemName, quantity, location, Optional.empty());
        when(mockInventoryService.addItem(any(AddItemCommandDto.class))).thenReturn(expectedItem);

        // Act
        int exitCode = addItemCommand.call();

        // Assert
        assertEquals(0, exitCode);
        verify(mockInventoryService).addItem(dtoCaptor.capture());
        assertFalse(dtoCaptor.getValue().expirationDate().isPresent(), "Expiration date should be empty.");
        String output = outContent.toString();
        assertTrue(output.contains("Successfully added item:"));
        assertFalse(output.contains("Expires:"), "Expires line should not be present."); // Check expiry is not printed
        assertTrue(errContent.toString().isEmpty());
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
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("Error: Invalid item data:"), "Validation error header expected.");
        assertTrue(errorOutput.contains("Item quantity must be positive."), "Specific validation message expected.");
        assertTrue(outContent.toString().isEmpty(), "Standard output should be empty.");
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
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("Error: Invalid item data:"));
        assertTrue(errorOutput.contains("Item name cannot be blank."), "Specific validation message expected.");
        assertTrue(outContent.toString().isEmpty());
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
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("Error: Invalid date format"), "Date format error expected.");
        assertTrue(outContent.toString().isEmpty());
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
        when(mockInventoryService.addItem(any(AddItemCommandDto.class))).thenThrow(new RuntimeException(errorMessage));

        // Act
        int exitCode = addItemCommand.call();

        // Assert
        assertEquals(1, exitCode, "Exit code should be 1 for service error.");
        verify(mockInventoryService).addItem(any()); // Verify service was called
        String errorOutput = errContent.toString();
        // Checking for the general error message printed by the command's catch block
        assertTrue(errorOutput.contains("An unexpected error occurred"), "Generic error message expected.");
        // The specific exception message might also be printed depending on the command's catch block detail
        // assertTrue(errorOutput.contains(errorMessage)); // Check if specific message is included
        assertTrue(outContent.toString().isEmpty());
    }
} 