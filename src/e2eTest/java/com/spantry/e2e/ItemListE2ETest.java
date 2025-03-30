package com.spantry.e2e;

import com.spantry.testsupport.CliTestSupport;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-End test for the 'item list' command.
 */
class ItemListE2ETest extends CliTestSupport {

    @Test
    void listItems_whenItemsExist_shouldShowItems() throws IOException, InterruptedException {
        // Arrange: Add some items first
        runCliCommand(new String[]{"item", "add", "--name", "E2E_Milk", "--quantity", "1", "--location", "FRIDGE"});
        runCliCommand(new String[]{"item", "add", "--name", "E2E_Bread", "--quantity", "2", "--location", "PANTRY"});

        // Act: Run the list command
        String[] listArgs = {"item", "list"};
        ProcessOutput output = runCliCommand(listArgs);

        // Assert: Verify the output
        assertEquals(0, output.exitCode(), "CLI command should exit successfully. Stderr: " + output.stderr());
        assertTrue(output.stderr().isEmpty(), "Standard error should be empty.");
        assertTrue(output.stdout().contains("E2E_Milk"), "Output should contain first item name.");
        assertTrue(output.stdout().contains("Quantity: 1"), "Output should contain first item quantity.");
        assertTrue(output.stdout().contains("Location: FRIDGE"), "Output should contain first item location.");
        assertTrue(output.stdout().contains("E2E_Bread"), "Output should contain second item name.");
        assertTrue(output.stdout().contains("Quantity: 2"), "Output should contain second item quantity.");
        assertTrue(output.stdout().contains("Location: PANTRY"), "Output should contain second item location.");
    }

    @Test
    void listItems_whenNoItemsExist_shouldShowEmptyMessage() throws IOException, InterruptedException {
        // Arrange: Ensure no items exist (tricky in E2E without state reset, assuming clean run or remove implemented)
        // For now, we assume a clean state for this specific test or rely on removal if available.
        // Consider adding a dedicated "reset" or "clear" command for testing.
        // If remove is implemented, we could add and then remove items.

        // Act: Run the list command
        String[] listArgs = {"item", "list"};
        ProcessOutput output = runCliCommand(listArgs);

        // Assert: Verify the output
        assertEquals(0, output.exitCode(), "CLI command should exit successfully. Stderr: " + output.stderr());
        assertTrue(output.stderr().isEmpty(), "Standard error should be empty.");
        assertTrue(output.stdout().contains("No items found in inventory."), "Output should contain 'No items found' message.");
    }

    // TODO: Add tests for list --location filter
} 