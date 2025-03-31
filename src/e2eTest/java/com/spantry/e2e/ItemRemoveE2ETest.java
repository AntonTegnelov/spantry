package com.spantry.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spantry.testsupport.CliTestSupport;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/** End-to-End test for the 'item remove' command. */
class ItemRemoveE2eTest extends CliTestSupport {

  // Pattern to extract UUID from the list output (adjust if output format changes)
  private static final Pattern UUID_PATTERN =
      Pattern.compile("ID: ([a-f0-9-]+), Name: E2E_ToRemove");

  @Test
  void removeItem_whenItemExists_shouldRemoveItem() throws IOException, InterruptedException {
    // Arrange: Add an item specifically for removal
    String itemName = "E2E_ToRemove";
    ProcessOutput addOutput =
        runCliCommand(
            new String[] {
              "item", "add", "--name", itemName, "--quantity", "1", "--location", "OTHER"
            });
    assertEquals(
        0,
        addOutput.exitCode(),
        "Failed to add item for removal test. Exit Code: "
            + addOutput.exitCode()
            + "\nStderr: "
            + addOutput.stderr()
            + "\nStdout: "
            + addOutput.stdout());

    // Arrange: List items to find the ID of the item we just added
    ProcessOutput listOutput = runCliCommand(new String[] {"item", "list"});
    assertEquals(0, listOutput.exitCode(), "Failed to list items to find ID for removal.");
    String listStdout = listOutput.stdout();
    Matcher matcher = UUID_PATTERN.matcher(listStdout);
    assertTrue(
        matcher.find(),
        "Could not find added item '"
            + itemName
            + "' with its ID in the list output.\nList Output:\n"
            + listStdout);
    String itemIdToRemove = matcher.group(1);
    assertNotNull(itemIdToRemove, "Extracted Item ID is null.");

    // Act: Run the remove command
    String[] removeArgs = {"item", "remove", itemIdToRemove};
    ProcessOutput removeOutput = runCliCommand(removeArgs);

    // Assert: Verify the remove command output
    assertEquals(
        0,
        removeOutput.exitCode(),
        "Remove CLI command should exit successfully. Stderr: " + removeOutput.stderr());
    assertTrue(
        removeOutput.stdout().contains("Successfully removed item with ID: " + itemIdToRemove),
        "Success message should be present in remove output.\nStdout: " + removeOutput.stdout());

    // Assert: Verify item is actually gone by listing again
    ProcessOutput listAfterRemoveOutput = runCliCommand(new String[] {"item", "list"});
    assertEquals(0, listAfterRemoveOutput.exitCode(), "Failed to list items after removal.");
    assertFalse(
        listAfterRemoveOutput.stdout().contains(itemName),
        "Removed item name should NOT be present in list output after removal.");
    assertFalse(
        listAfterRemoveOutput.stdout().contains("ID: " + itemIdToRemove),
        "Removed item ID should NOT be present in list output after removal.");
  }

  @Test
  void removeItem_whenItemDoesNotExist_shouldShowErrorMessage()
      throws IOException, InterruptedException {
    // Arrange: Use a known non-existent ID
    String nonExistentId = "non-existent-uuid-12345";

    // Act: Run the remove command
    String[] removeArgs = {"item", "remove", nonExistentId};
    ProcessOutput removeOutput = runCliCommand(removeArgs);

    // Assert: Verify the error output
    assertNotEquals(
        0,
        removeOutput.exitCode(),
        "Remove CLI command should exit with error for non-existent ID.");
    // Stdout check removed - exit code and stderr message are primary indicators of this error
    // Service layer should throw ItemNotFoundException, check for its message
    // Note: ERROR logs now go to stdout due to logback config for e2e tests
    assertTrue(
        removeOutput.stdout().contains("Item not found with ID: " + nonExistentId),
        "Error message 'Item not found with ID: ...' should be present in stdout.\nStderr: "
            + removeOutput.stderr()
            + "\nStdout: "
            + removeOutput.stdout());
  }

  // TODO: Add test for removing with blank/missing ID (should be caught by picocli)
}
