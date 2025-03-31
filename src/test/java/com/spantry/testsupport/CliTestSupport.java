package com.spantry.testsupport;

import com.spantry.inventory.repository.InMemoryInventoryRepository;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class or utility for running CLI commands during E2E tests. Assumes the application JAR is
 * built.
 */
public abstract class CliTestSupport {

  // Path to the application script in the installed distribution
  private static final Path SCRIPT_PATH = findApplicationScript();

  // Logger for this class
  private static final Logger LOGGER = Logger.getLogger(CliTestSupport.class.getName());

  /**
   * Record to hold the results of a process execution, including exit code and output streams. Used
   * to capture and verify results from CLI command execution during E2E tests.
   */
  public record ProcessOutput(int exitCode, String stdout, String stderr) {}

  /** Protected constructor for abstract class. */
  protected CliTestSupport() {
    // Default constructor for the abstract class
  }

  /**
   * Abstract method to be implemented by concrete test classes to define their test-specific setup.
   */
  protected abstract void prepareTestEnvironment();

  /** Deletes the E2E data file before each test method. */
  @BeforeEach
  void resetInventoryState() {
    // Call the static method to delete the data file used by the file-based repository
    InMemoryInventoryRepository.deleteDataFile();
  }

  /**
   * Runs the application script from the installed distribution.
   *
   * @param args Command-line arguments to pass to the application.
   * @return ProcessOutput containing exit code, stdout, and stderr.
   * @throws IOException If an I/O error occurs.
   * @throws InterruptedException If the process is interrupted.
   */
  protected ProcessOutput runCliCommand(final String... args)
      throws IOException, InterruptedException {
    if (SCRIPT_PATH == null) {
      throw new IllegalStateException(
          "Application script not found in build/install/spantry/bin. "
              + "Ensure 'gradlew installDist' has run.");
    }

    final List<String> command = new ArrayList<>();
    command.add(SCRIPT_PATH.toAbsolutePath().toString()); // Use absolute path for script
    command.addAll(Arrays.asList(args));

    final ProcessBuilder pb = new ProcessBuilder(command);
    // Set working directory to the installation root for consistency
    pb.directory(SCRIPT_PATH.getParent().getParent().toFile());

    final Process process = pb.start();

    // Capture stdout
    final StringBuilder stdoutBuilder = new StringBuilder();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        stdoutBuilder.append(line).append(System.lineSeparator());
      }
    }

    // Capture stderr
    final StringBuilder stderrBuilder = new StringBuilder();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        stderrBuilder.append(line).append(System.lineSeparator());
      }
    }

    // Wait for process to complete (with a timeout)
    final boolean exited = process.waitFor(30, TimeUnit.SECONDS); // 30-second timeout
    if (!exited) {
      process.destroyForcibly();
      throw new InterruptedException("CLI command timed out.");
    }

    return new ProcessOutput(
        process.exitValue(), stdoutBuilder.toString().trim(), stderrBuilder.toString().trim());
  }

  /**
   * Finds the application start script in the build/install/spantry/bin directory.
   *
   * @return Path to the script file, or null if not found.
   */
  private static Path findApplicationScript() {
    final Path scriptDir = Paths.get("build", "install", "spantry", "bin");
    Path result = null;

    // Check if the directory exists first (positive case)
    if (Files.isDirectory(scriptDir)) {
      // Determine script name based on OS - ternary is acceptable here by PMD defaults
      final String scriptName =
          System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win")
              ? "spantry.bat"
              : "spantry";

      final Path scriptPath = scriptDir.resolve(scriptName);

      // Simplified conditional logic to avoid confusing ternary pattern
      if (Files.exists(scriptPath)) {
        result = scriptPath;
      } else {
        // Use isLoggable for guard condition
        if (LOGGER.isLoggable(java.util.logging.Level.WARNING)) {
          LOGGER.warning(
              "Warning: Application script '"
                  + scriptName
                  + "' not found in "
                  + scriptDir.toAbsolutePath());
        }
      }
    } else { // Handle the case where the directory does not exist
      // Use isLoggable for guard condition
      if (LOGGER.isLoggable(java.util.logging.Level.WARNING)) {
        LOGGER.warning("Warning: Script directory not found: " + scriptDir.toAbsolutePath());
      }
    }
    return result;
  }
}
