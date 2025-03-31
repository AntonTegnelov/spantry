package com.spantry.e2e;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spantry.testsupport.CliTestSupport;
import java.io.IOException;
import org.junit.jupiter.api.Test;

/** End-to-End test for the 'item add' command. Uses CliTestSupport for running the application. */
class ItemAddE2eTest extends CliTestSupport {

  @Test
  void addItemSuccessfully() throws IOException, InterruptedException {
    // Arrange: Prepare command arguments
    String itemName = "Test Flour";
    int quantity = 2;
    String location = "PANTRY";
    String[] args = {
      "item",
      "add",
      "--name",
      itemName,
      "--quantity",
      String.valueOf(quantity),
      "--location",
      location
    };

    // Act: Run the command
    ProcessOutput output = runCliCommand(args);

    // Assert: Verify the output
    assertEquals(
        0,
        output.exitCode(),
        "CLI command should exit successfully. (Exit Code: " + output.exitCode() + ")");
    assertTrue(
        output.stdout().contains("Successfully added item:"),
        "Success message should be present in standard output.\nStdout:" + output.stdout());

    // Optional: Verify state by listing items (Requires list command E2E test)
    // ProcessOutput listOutput = runCliCommand(new String[]{"item", "list"});
    // assertTrue(listOutput.stdout().contains(itemName), "Added item should be listed.");
    // assertTrue(listOutput.stdout().contains(String.valueOf(quantity)), "Quantity should be
    // listed.");
    // assertTrue(listOutput.stdout().contains(location), "Location should be listed.");
  }

  @Test
  void addItemWithMissingRequiredOption() throws IOException, InterruptedException {
    // Arrange: Missing --name
    String[] args = {
      "item", "add",
      "--quantity", "1",
      "--location", "FRIDGE"
    };

    // Act: Run the command
    ProcessOutput output = runCliCommand(args);

    // Assert: Verify the error output and exit code
    assertNotEquals(0, output.exitCode(), "CLI command should exit with an error code.");
    // Picocli's standard error message format can vary slightly
    assertTrue(
        output.stderr().contains("Missing required option: '--name=<name>'")
            || output.stderr().contains("Missing required option: --name"),
        "Error message for missing name should be present in standard error.\nStderr: "
            + output.stderr());
  }

  // TODO: Add more tests for edge cases (invalid quantity, invalid location, expiration date)
}
