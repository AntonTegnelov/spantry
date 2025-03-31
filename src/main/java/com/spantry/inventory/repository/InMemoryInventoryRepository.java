package com.spantry.inventory.repository;

// import com.spantry.inventory.domain.Item;
import com.spantry.inventory.domain.InventoryItem;
import com.spantry.inventory.domain.Location;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An inventory repository implementation that persists items to a file using Java Serialization.
 * Intended primarily for E2E testing where state needs to persist across process executions.
 */
public class InMemoryInventoryRepository implements InventoryRepository {

  private static final Logger LOG = LoggerFactory.getLogger(InMemoryInventoryRepository.class);
  private static final Path DATA_FILE_PATH = Paths.get("build", "e2e-inventory.dat");

  // Map is now an instance variable
  private final Map<String, InventoryItem> inventory;

  /** Constructor that loads data from the file. */
  public InMemoryInventoryRepository() {
    this.inventory = loadInventoryFromFile();
  }

  @Override
  public InventoryItem save(final InventoryItem item) {
    Objects.requireNonNull(item, "Item cannot be null for saving");

    String itemId = item.itemId();
    InventoryItem itemToStore;

    if (itemId == null || itemId.isBlank()) {
      itemId = UUID.randomUUID().toString();
      itemToStore =
          new InventoryItem(
              itemId, item.name(), item.quantity(), item.location(), item.expirationDate());
    } else {
      itemToStore = item;
    }

    synchronized (this) {
      inventory.put(itemToStore.itemId(), itemToStore);
      saveInventoryToFile(); // Save after modification
    }
    return itemToStore;
  }

  @Override
  public Optional<InventoryItem> findById(final String itemId) {
    Objects.requireNonNull(itemId, "Item ID cannot be null for findById");
    synchronized (this) {
      return Optional.ofNullable(inventory.get(itemId));
    }
  }

  @Override
  public List<InventoryItem> findAll() {
    synchronized (this) {
      return List.copyOf(inventory.values());
    }
  }

  @Override
  public void deleteById(final String itemId) {
    Objects.requireNonNull(itemId, "Item ID cannot be null for deleteById");
    synchronized (this) {
      if (inventory.remove(itemId) != null) {
        saveInventoryToFile(); // Save only if something was actually removed
      }
    }
  }

  @Override
  public List<InventoryItem> findByLocation(final Location location) {
    Objects.requireNonNull(location, "Location cannot be null for findByLocation");
    synchronized (this) {
      return inventory.values().stream()
          .filter(item -> item.location() == location)
          .collect(Collectors.toUnmodifiableList());
    }
  }

  // --- Serialization/Deserialization Logic ---

  private void saveInventoryToFile() {
    // Ensure build directory exists
    try {
      Files.createDirectories(DATA_FILE_PATH.getParent());
    } catch (IOException e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Failed to create directory for data file: {}", DATA_FILE_PATH.getParent(), e);
      }
      // Decide if we should throw or just log - logging for now
      return; // Cannot save if dir fails
    }

    try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(DATA_FILE_PATH))) {
      oos.writeObject(new ConcurrentHashMap<>(this.inventory)); // Save a copy
      if (LOG.isDebugEnabled()) {
        LOG.debug("Inventory saved to file: {}", DATA_FILE_PATH);
      }
    } catch (IOException e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Failed to save inventory to file: {}", DATA_FILE_PATH, e);
      }
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, InventoryItem> loadInventoryFromFile() {
    final File dataFile = DATA_FILE_PATH.toFile();
    final Map<String, InventoryItem> result = new ConcurrentHashMap<>();

    // Check if file exists
    if (dataFile.exists()) {
      // Try to load existing file
      tryLoadExistingInventoryFile(result);
    } else if (LOG.isDebugEnabled()) {
      LOG.debug("Inventory data file not found, starting fresh: {}", DATA_FILE_PATH);
    }

    return result;
  }

  /**
   * Helper method to try loading inventory from an existing file. This method modifies the provided
   * map by reference.
   *
   * @param resultMap the map to populate with loaded data
   */
  @SuppressWarnings("unchecked")
  private void tryLoadExistingInventoryFile(final Map<String, InventoryItem> resultMap) {
    try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(DATA_FILE_PATH))) {
      final Object readObject = ois.readObject();

      // Check if object is the expected Map type
      if (readObject instanceof Map) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Inventory loaded from file: {}", DATA_FILE_PATH);
        }
        // Copy data to the provided result map
        resultMap.putAll((Map<String, InventoryItem>) readObject);
      } else {
        handleCorruptedFile("unexpected object type: " + readObject.getClass().getName());
      }
    } catch (EOFException e) {
      handleCorruptedFile("empty or truncated file", e);
    } catch (IOException | ClassNotFoundException e) {
      handleCorruptedFile("IO or class loading error", e);
    }
  }

  /**
   * Helper method to handle corrupted inventory files.
   *
   * @param reason description of corruption
   * @param exception optional exception that caused the corruption
   */
  private void handleCorruptedFile(final String reason, final Exception... exception) {
    if (exception.length > 0 && exception[0] instanceof EOFException) {
      if (LOG.isWarnEnabled()) {
        LOG.warn("Inventory data file is empty or truncated. Starting fresh: {}", DATA_FILE_PATH);
      }
    } else if (exception.length > 0) {
      if (LOG.isErrorEnabled()) {
        LOG.error(
            "Failed to load inventory from file: {}. Starting fresh. Reason: {}",
            DATA_FILE_PATH,
            reason,
            exception[0]);
      }
    } else {
      if (LOG.isErrorEnabled()) {
        LOG.error("Inventory data file is corrupted ({}). Starting fresh.", reason);
      }
    }
    deleteDataFile();
  }

  /** Deletes the data file, logging errors but not throwing exceptions. */
  public static void deleteDataFile() {
    try {
      final boolean deleted = Files.deleteIfExists(DATA_FILE_PATH);
      if (deleted) {
        if (LOG.isInfoEnabled()) {
          LOG.info("Deleted inventory data file: {}", DATA_FILE_PATH);
        }
      } else {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Inventory data file did not exist, nothing to delete: {}", DATA_FILE_PATH);
        }
      }
    } catch (IOException e) {
      if (LOG.isErrorEnabled()) {
        LOG.error("Failed to delete inventory data file: {}", DATA_FILE_PATH, e);
      }
    }
  }
}
