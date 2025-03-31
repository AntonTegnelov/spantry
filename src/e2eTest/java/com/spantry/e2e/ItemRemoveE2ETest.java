package com.spantry.e2e;

import com.spantry.testsupport.CliTestSupport;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/** End-to-End test for the 'item remove' command. */
class ItemRemoveE2eTest extends CliTestSupport {

  // Pattern to extract UUID from the list output (adjust if output format changes)
  private static final Pattern UUID_PATTERN =
      Pattern.compile("ID: ([a-f0-9-]+), Name: E2E_ToRemove");

  /** Default constructor. */
  ItemRemoveE2eTest() {
    super(); // Added to satisfy PMD rule about calling super()
    // Default constructor added to satisfy PMD rule
  }

  /**
   * Implements the abstract method from CliTestSupport. Prepares the test environment for this
   * specific test class.
   */
  @Override
  protected void prepareTestEnvironment() {
    // No specific environment preparation needed for this test
  }

  /**
   * Tests the full remove item flow from adding an item, confirming it exists, removing it, and
   * verifying it has been removed. Multiple assertions are needed to validate the complete user
   * workflow.
   */
  @Test
  void removeItemWhenItemExistsShouldRemoveItem() throws IOException, InterruptedException {
    // Arrange: Add an item specifically for removal
    final String itemName = "E2E_ToRemove";
    final ProcessOutput addOutput =
        runCliCommand("item", "add", "--name", itemName, "--quantity", "1", "--location", "OTHER");
    Assertions.assertEquals(
        0,
        addOutput.exitCode(),
        "Failed to add item for removal test. Exit Code: "
            + addOutput.exitCode()
            + "\nStderr: "
            + addOutput.stderr()
            + "\nStdout: "
            + addOutput.stdout());

    // Arrange: List items to find the ID of the item we just added
    final ProcessOutput listOutput = runCliCommand("item", "list");
    Assertions.assertEquals(
        0, listOutput.exitCode(), "Failed to list items to find ID for removal.");
    final String listStdout = listOutput.stdout();
    final Matcher matcher = UUID_PATTERN.matcher(listStdout);
    Assertions.assertTrue(
        matcher.find(),
        "Could not find added item '"
            + itemName
            + "' with its ID in the list output.\nList Output:\n"
            + listStdout);
    final String itemIdToRemove = matcher.group(1);
    Assertions.assertNotNull(itemIdToRemove, "Extracted Item ID is null.");

    // Act: Run the remove command
    final ProcessOutput removeOutput = runCliCommand("item", "remove", itemIdToRemove);

    // Assert: Verify the remove command output
    Assertions.assertEquals(
        0,
        removeOutput.exitCode(),
        "Remove CLI command should exit successfully. Stderr: " + removeOutput.stderr());
    Assertions.assertTrue(
        removeOutput.stdout().contains("Successfully removed item with ID: " + itemIdToRemove),
        "Success message should be present in remove output.\nStdout: " + removeOutput.stdout());

    // Assert: Verify item is actually gone by listing again
    final ProcessOutput listAfterRemoveOutput = runCliCommand("item", "list");
    Assertions.assertEquals(
        0, listAfterRemoveOutput.exitCode(), "Failed to list items after removal.");
    Assertions.assertFalse(
        listAfterRemoveOutput.stdout().contains(itemName),
        "Removed item name should NOT be present in list output after removal.");
    Assertions.assertFalse(
        listAfterRemoveOutput.stdout().contains("ID: " + itemIdToRemove),
        "Removed item ID should NOT be present in list output after removal.");
  }

  /**
   * Tests error handling for non-existent items. Multiple assertions are needed to verify both the
   * exit code and the error message.
   */
  @Test
  void removeItemWhenItemDoesNotExistShouldShowErrorMessage()
      throws IOException, InterruptedException {
    // Arrange: Use a known non-existent ID
    final String nonExistentId = "non-existent-uuid-12345";

    // Act: Run the remove command
    final ProcessOutput removeOutput = runCliCommand("item", "remove", nonExistentId);

    // Assert: Verify the error output
    Assertions.assertNotEquals(
        0,
        removeOutput.exitCode(),
        "Remove CLI command should exit with error for non-existent ID.");
    // Stdout check removed - exit code and stderr message are primary indicators of this error
    // Service layer should throw ItemNotFoundException, check for its message
    // Note: ERROR logs now go to stdout due to logback config for e2e tests
    Assertions.assertTrue(
        removeOutput.stdout().contains("Item not found with ID: " + nonExistentId),
        "Error message 'Item not found with ID: ...' should be present in stdout.\nStderr: "
            + removeOutput.stderr()
            + "\nStdout: "
            + removeOutput.stdout());
  }

  // TODO: Add test for removing with blank/missing ID (should be caught by picocli)
}
