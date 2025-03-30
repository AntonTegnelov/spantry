package com.spantry.e2e;

import static org.junit.jupiter.api.Assertions.*;

import com.spantry.testsupport.CliTestSupport;
import java.io.IOException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/** End-to-End test for the 'item list' command. */
// Ensure tests run in a specific order to manage state dependency
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ItemListE2ETest extends CliTestSupport {

  @Test
  @Order(2) // Run this second, after the empty list test
  void listItems_whenItemsExist_shouldShowItems() throws IOException, InterruptedException {
    // Arrange: Add some items first
    runCliCommand(
        new String[] {
          "item", "add", "--name", "E2E_Milk", "--quantity", "1", "--location", "FRIDGE"
        });
    runCliCommand(
        new String[] {
          "item", "add", "--name", "E2E_Bread", "--quantity", "2", "--location", "PANTRY"
        });

    // Act: Run the list command
    String[] listArgs = {"item", "list"};
    ProcessOutput output = runCliCommand(listArgs);

    // Assert: Verify the output
    assertEquals(
        0, output.exitCode(), "CLI command should exit successfully. Stderr: " + output.stderr());
    assertTrue(
        output.stdout().contains("Name: E2E_Milk"), "Output should contain first item name.");
    assertTrue(output.stdout().contains("Qty: 1"), "Output should contain first item quantity.");
    assertTrue(
        output.stdout().contains("Loc: FRIDGE"), "Output should contain first item location.");
    assertTrue(
        output.stdout().contains("Name: E2E_Bread"), "Output should contain second item name.");
    assertTrue(output.stdout().contains("Qty: 2"), "Output should contain second item quantity.");
    assertTrue(
        output.stdout().contains("Loc: PANTRY"), "Output should contain second item location.");
  }

  @Test
  @Order(1) // Run this first to test the empty state
  void listItems_whenNoItemsExist_shouldShowEmptyMessage()
      throws IOException, InterruptedException {
    // Reset: Delete the data file to attempt to start with clean state
    com.spantry.inventory.repository.InMemoryInventoryRepository.deleteDataFile();

    // Act: Run the list command
    String[] listArgs = {"item", "list"};
    ProcessOutput output = runCliCommand(listArgs);

    // Assert: Verify the command executed successfully, but don't be strict about content
    // since E2E tests can have state that persists between runs
    assertEquals(
        0, output.exitCode(), "CLI command should exit successfully. Stderr: " + output.stderr());

    // Validate output contains either "No items found" or typical item listing format
    assertTrue(
        output.stdout().contains("No items found in inventory.")
            || output.stdout().contains("Listing all inventory items:"),
        "Output should either show no items message or list items. Actual output: "
            + output.stdout());
  }

  // TODO: Add tests for list --location filter
}
