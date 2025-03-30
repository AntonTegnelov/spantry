package com.spantry.cli.command;

import com.spantry.inventory.domain.Item;
import com.spantry.inventory.domain.Location;
import com.spantry.inventory.service.InventoryService;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListItemsCommandTest {

    @Mock
    private InventoryService mockInventoryService;

    @InjectMocks
    private ListItemsCommand listItemsCommand;

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
    void call_NoFilter_ShouldCallGetAllItemsAndPrintAll() {
        // Arrange
        Item item1 = new Item(UUID.randomUUID().toString(), "Milk", 1, Location.FRIDGE, Optional.empty());
        Item item2 = new Item(UUID.randomUUID().toString(), "Bread", 1, Location.PANTRY, Optional.of(LocalDate.now().plusDays(5)));
        List<Item> allItems = Arrays.asList(item1, item2);
        when(mockInventoryService.getAllItems()).thenReturn(allItems);

        // Act
        int exitCode = listItemsCommand.call();

        // Assert
        assertEquals(0, exitCode);
        verify(mockInventoryService).getAllItems();
        verify(mockInventoryService, never()).getItemsByLocation(any());

        String output = outContent.toString();
        assertTrue(output.contains("Listing all inventory items:"));
        assertTrue(output.contains(item1.getId()));
        assertTrue(output.contains(item1.getName()));
        assertTrue(output.contains(item2.getId()));
        assertTrue(output.contains(item2.getName()));
        assertTrue(output.contains(item2.getExpirationDate().get().toString()));
        assertTrue(output.contains("N/A")); // For item1 expiry
        assertTrue(errContent.toString().isEmpty());
    }

    @Test
    void call_WithLocationFilter_ShouldCallGetItemsByLocationAndPrintFiltered() throws Exception {
        // Arrange
        Location filterLocation = Location.FRIDGE;
        Item item1 = new Item(UUID.randomUUID().toString(), "Milk", 1, Location.FRIDGE, Optional.empty());
        Item item2 = new Item(UUID.randomUUID().toString(), "Cheese", 1, Location.FRIDGE, Optional.of(LocalDate.now().plusDays(10)));
        List<Item> fridgeItems = Arrays.asList(item1, item2);

        setField(listItemsCommand, "location", filterLocation);
        when(mockInventoryService.getItemsByLocation(filterLocation)).thenReturn(fridgeItems);

        // Act
        int exitCode = listItemsCommand.call();

        // Assert
        assertEquals(0, exitCode);
        verify(mockInventoryService, never()).getAllItems();
        verify(mockInventoryService).getItemsByLocation(filterLocation);

        String output = outContent.toString();
        assertTrue(output.contains("Listing items in location: " + filterLocation));
        assertTrue(output.contains(item1.getId()));
        assertTrue(output.contains(item1.getName()));
        assertTrue(output.contains(item2.getId()));
        assertTrue(output.contains(item2.getName()));
        assertTrue(output.contains(item2.getExpirationDate().get().toString()));
        assertFalse(output.contains("Bread")); // Should not contain items from other locations
        assertTrue(errContent.toString().isEmpty());
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
        String output = outContent.toString();
        assertTrue(output.contains("Listing all inventory items:"));
        assertTrue(output.contains("No items found."));
        assertTrue(errContent.toString().isEmpty());
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
        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("Error listing items: " + errorMessage));
        assertTrue(outContent.toString().isEmpty());
    }
} 