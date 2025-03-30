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
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class or utility for running CLI commands during E2E tests. Assumes the application JAR is
 * built.
 */
public abstract class CliTestSupport {

  // Simple record to hold process execution results
  public record ProcessOutput(int exitCode, String stdout, String stderr) {}

  // Path to the application script in the installed distribution
  private static final Path SCRIPT_PATH = findApplicationScript();

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
  protected ProcessOutput runCliCommand(String... args) throws IOException, InterruptedException {
    if (SCRIPT_PATH == null) {
      throw new IllegalStateException(
          "Application script not found in build/install/spantry/bin. Ensure 'gradlew installDist' has run.");
    }

    List<String> command = new ArrayList<>();
    command.add(SCRIPT_PATH.toAbsolutePath().toString()); // Use absolute path for script
    command.addAll(Arrays.asList(args));

    ProcessBuilder pb = new ProcessBuilder(command);
    // Set working directory to the installation root for consistency
    pb.directory(SCRIPT_PATH.getParent().getParent().toFile());

    Process process = pb.start();

    // Capture stdout
    StringBuilder stdoutBuilder = new StringBuilder();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        stdoutBuilder.append(line).append(System.lineSeparator());
      }
    }

    // Capture stderr
    StringBuilder stderrBuilder = new StringBuilder();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        stderrBuilder.append(line).append(System.lineSeparator());
      }
    }

    // Wait for process to complete (with a timeout)
    boolean exited = process.waitFor(30, TimeUnit.SECONDS); // 30-second timeout
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
    // Path relative to project root
    Path scriptDir = Paths.get("build", "install", "spantry", "bin");

    if (!Files.isDirectory(scriptDir)) {
      System.err.println("Warning: Script directory not found: " + scriptDir.toAbsolutePath());
      return null;
    }

    // Determine script name based on OS
    String scriptName =
        System.getProperty("os.name").toLowerCase().contains("win") ? "spantry.bat" : "spantry";

    Path scriptPath = scriptDir.resolve(scriptName);

    if (!Files.exists(scriptPath)) {
      System.err.println(
          "Warning: Application script '"
              + scriptName
              + "' not found in "
              + scriptDir.toAbsolutePath());
      return null;
    }
    return scriptPath;
  }
}
