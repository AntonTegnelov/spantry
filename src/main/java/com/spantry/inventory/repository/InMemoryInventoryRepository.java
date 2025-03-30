package com.spantry.inventory.repository;

// import com.spantry.inventory.domain.Item;
import com.spantry.inventory.domain.InventoryItem;
import com.spantry.inventory.domain.Location;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
  public synchronized InventoryItem save(final InventoryItem item) {
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

    inventory.put(itemToStore.itemId(), itemToStore);
    saveInventoryToFile(); // Save after modification
    return itemToStore;
  }

  @Override
  public synchronized Optional<InventoryItem> findById(final String itemId) {
    Objects.requireNonNull(itemId, "Item ID cannot be null for findById");
    return Optional.ofNullable(inventory.get(itemId));
  }

  @Override
  public synchronized List<InventoryItem> findAll() {
    return List.copyOf(inventory.values());
  }

  @Override
  public synchronized void deleteById(final String itemId) {
    Objects.requireNonNull(itemId, "Item ID cannot be null for deleteById");
    if (inventory.remove(itemId) != null) {
      saveInventoryToFile(); // Save only if something was actually removed
    }
  }

  @Override
  public synchronized List<InventoryItem> findByLocation(final Location location) {
    Objects.requireNonNull(location, "Location cannot be null for findByLocation");
    return inventory.values().stream()
        .filter(item -> item.location() == location)
        .collect(Collectors.toUnmodifiableList());
  }

  // --- Serialization/Deserialization Logic ---

  private void saveInventoryToFile() {
    // Ensure build directory exists
    try {
      Files.createDirectories(DATA_FILE_PATH.getParent());
    } catch (IOException e) {
      LOG.error("Failed to create directory for data file: {}", DATA_FILE_PATH.getParent(), e);
      // Decide if we should throw or just log - logging for now
      return; // Cannot save if dir fails
    }

    try (ObjectOutputStream oos =
        new ObjectOutputStream(new FileOutputStream(DATA_FILE_PATH.toFile()))) {
      oos.writeObject(new ConcurrentHashMap<>(this.inventory)); // Save a copy
      LOG.debug("Inventory saved to file: {}", DATA_FILE_PATH);
    } catch (IOException e) {
      LOG.error("Failed to save inventory to file: {}", DATA_FILE_PATH, e);
    }
  }

  @SuppressWarnings("unchecked")
  private Map<String, InventoryItem> loadInventoryFromFile() {
    File dataFile = DATA_FILE_PATH.toFile();
    if (!dataFile.exists()) {
      LOG.debug("Inventory data file not found, starting fresh: {}", DATA_FILE_PATH);
      return new ConcurrentHashMap<>();
    }

    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
      Object readObject = ois.readObject();
      if (readObject instanceof Map) {
        LOG.debug("Inventory loaded from file: {}", DATA_FILE_PATH);
        // Ensure it's the correct type and make it concurrent
        return new ConcurrentHashMap<>((Map<String, InventoryItem>) readObject);
      } else {
        LOG.error(
            "Inventory data file is corrupted (unexpected object type: {}). Starting fresh.",
            readObject.getClass().getName());
        deleteDataFile(); // Delete corrupted file
        return new ConcurrentHashMap<>();
      }
    } catch (EOFException e) {
      LOG.warn("Inventory data file is empty or truncated. Starting fresh: {}", DATA_FILE_PATH);
      deleteDataFile();
      return new ConcurrentHashMap<>();
    } catch (IOException | ClassNotFoundException e) {
      LOG.error("Failed to load inventory from file: {}. Starting fresh.", DATA_FILE_PATH, e);
      deleteDataFile(); // Attempt to delete potentially corrupt file
      return new ConcurrentHashMap<>();
    }
  }

  /** Deletes the data file, logging errors but not throwing exceptions. */
  public static void deleteDataFile() {
    try {
      boolean deleted = Files.deleteIfExists(DATA_FILE_PATH);
      if (deleted) {
        LOG.info("Deleted inventory data file: {}", DATA_FILE_PATH);
      } else {
        LOG.debug("Inventory data file did not exist, nothing to delete: {}", DATA_FILE_PATH);
      }
    } catch (IOException e) {
      LOG.error("Failed to delete inventory data file: {}", DATA_FILE_PATH, e);
    }
  }
}
