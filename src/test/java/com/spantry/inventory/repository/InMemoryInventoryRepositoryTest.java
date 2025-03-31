package com.spantry.inventory.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.spantry.inventory.domain.InventoryItem;
import com.spantry.inventory.domain.Location;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link InMemoryInventoryRepository}. */
class InMemoryInventoryRepositoryTest {

  private InMemoryInventoryRepository repository;

  /** Default constructor. */
  InMemoryInventoryRepositoryTest() {
    // Default constructor added to satisfy PMD rule
  }

  // Delete the data file before and after each test
  @BeforeEach
  @AfterEach // Run after as well to clean up for subsequent test classes if any
  void manageDataFile() {
    InMemoryInventoryRepository.deleteDataFile(); // Call the static delete method
    repository = new InMemoryInventoryRepository(); // Create a new instance for the test
  }

  @Test
  void saveNewItemShouldGenerateIdAndStore() {
    // Arrange
    final InventoryItem newItem = new InventoryItem(null, "Test Item", 1, Location.PANTRY, null);

    // Act
    final InventoryItem savedItem = repository.save(newItem);

    // Assert
    assertNotNull(savedItem, "Saved item should not be null");
    assertNotNull(savedItem.itemId(), "ID should be generated");
    assertEquals(newItem.name(), savedItem.name(), "Name should match");
    assertEquals(newItem.quantity(), savedItem.quantity(), "Quantity should match");
    assertEquals(newItem.location(), savedItem.location(), "Location should match");
    assertEquals(
        newItem.expirationDate(), savedItem.expirationDate(), "Expiration date should match");

    // Verify item is stored and retrievable (reloads from file)
    final InMemoryInventoryRepository reloadedRepo = new InMemoryInventoryRepository();
    final Optional<InventoryItem> found = reloadedRepo.findById(savedItem.itemId());
    assertTrue(found.isPresent(), "Item should be found after saving");
    assertEquals(savedItem, found.get(), "Loaded item should match saved item");
  }

  @Test
  void saveExistingItemShouldUpdateAndStore() {
    // Arrange
    final InventoryItem initialItem =
        new InventoryItem(null, "Update Me", 1, Location.FRIDGE, null);
    final InventoryItem savedInitial = repository.save(initialItem);
    final String existingId = savedInitial.itemId();

    final InventoryItem updatedItem =
        new InventoryItem(existingId, "Updated Name", 5, Location.FRIDGE, null);

    // Act
    final InventoryItem savedUpdated = repository.save(updatedItem);

    // Assert
    assertNotNull(savedUpdated, "Updated item should not be null");
    assertEquals(existingId, savedUpdated.itemId(), "ID should remain the same");
    assertEquals(updatedItem.name(), savedUpdated.name(), "Name should be updated");
    assertEquals(updatedItem.quantity(), savedUpdated.quantity(), "Quantity should be updated");

    // Verify updated item is stored (reloads from file)
    final InMemoryInventoryRepository reloadedRepo = new InMemoryInventoryRepository();
    final Optional<InventoryItem> found = reloadedRepo.findById(existingId);
    assertTrue(found.isPresent(), "Item should be found after updating");
    assertEquals(savedUpdated, found.get(), "Loaded item should match updated item");
  }

  @Test
  void findByIdExistingItemShouldReturnItem() {
    // Arrange
    final LocalDate expiry = LocalDate.now();
    final InventoryItem item =
        repository.save(new InventoryItem(null, "Find Me", 1, Location.CUPBOARD, expiry));
    final String itemId = item.itemId();

    // Act: Use a new instance to force reload from file
    final InMemoryInventoryRepository reloadedRepo = new InMemoryInventoryRepository();
    final Optional<InventoryItem> found = reloadedRepo.findById(itemId);

    // Assert
    assertTrue(found.isPresent(), "Item should be found");
    assertEquals(item, found.get(), "Found item should match the saved item");
  }

  @Test
  void findByIdNonExistentItemShouldReturnEmpty() {
    // Arrange: Add an item so the file exists
    repository.save(new InventoryItem(null, "Other", 1, Location.OTHER, null));
    // Act: Use a new instance
    final InMemoryInventoryRepository reloadedRepo = new InMemoryInventoryRepository();
    final Optional<InventoryItem> found = reloadedRepo.findById("non-existent-id");

    // Assert
    assertTrue(found.isEmpty(), "Finding non-existent item should return empty");
  }

  @Test
  void findAllWhenEmptyShouldReturnEmptyList() {
    // Arrange: Ensure file is deleted (done by @BeforeEach)
    // Act: Create new instance (which loads from non-existent file)
    final InMemoryInventoryRepository reloadedRepo = new InMemoryInventoryRepository();
    final List<InventoryItem> allItems = reloadedRepo.findAll();

    // Assert
    assertNotNull(allItems, "Result list should not be null");
    assertTrue(allItems.isEmpty(), "Result list should be empty");
  }

  @Test
  void findAllWithItemsShouldReturnAllItems() {
    // Arrange
    final InventoryItem item1 =
        repository.save(new InventoryItem(null, "Item 1", 1, Location.PANTRY, null));
    final InventoryItem item2 =
        repository.save(new InventoryItem(null, "Item 2", 2, Location.FRIDGE, LocalDate.now()));

    // Act: Use a new instance
    final InMemoryInventoryRepository reloadedRepo = new InMemoryInventoryRepository();
    final List<InventoryItem> allItems = reloadedRepo.findAll();

    // Assert
    assertEquals(2, allItems.size(), "Should return 2 items");
    assertTrue(allItems.contains(item1), "Result should contain first item");
    assertTrue(allItems.contains(item2), "Result should contain second item");
  }

  @Test
  void deleteByIdExistingItemShouldRemoveItem() {
    // Arrange
    final InventoryItem item =
        repository.save(new InventoryItem(null, "Delete Me", 1, Location.FREEZER, null));
    final String itemId = item.itemId();
    // Verify exists in initial repo
    assertTrue(repository.findById(itemId).isPresent(), "Item should exist before delete");

    // Act: Delete using the initial repo instance
    repository.deleteById(itemId);

    // Assert: Check using a new instance that reloads the file
    final InMemoryInventoryRepository reloadedRepo = new InMemoryInventoryRepository();
    assertTrue(reloadedRepo.findById(itemId).isEmpty(), "Item should not exist after delete");
  }

  @Test
  void deleteByIdNonExistentItemShouldDoNothing() {
    // Arrange
    repository.save(new InventoryItem(null, "Existing", 1, Location.COUNTER, null));
    final long initialCount = repository.findAll().size();

    // Act: Attempt delete using the initial repo
    repository.deleteById("non-existent-id");

    // Assert: Check using a reloaded repo
    final InMemoryInventoryRepository reloadedRepo = new InMemoryInventoryRepository();
    assertEquals(initialCount, reloadedRepo.findAll().size(), "Size should not change");
  }

  @Test
  void findByLocationShouldReturnMatchingItems() {
    // Arrange
    final InventoryItem pantryItem1 =
        repository.save(new InventoryItem(null, "Pantry 1", 1, Location.PANTRY, null));
    final InventoryItem fridgeItem =
        repository.save(new InventoryItem(null, "Fridge 1", 1, Location.FRIDGE, null));
    final InventoryItem pantryItem2 =
        repository.save(new InventoryItem(null, "Pantry 2", 2, Location.PANTRY, null));

    // Act: Use a new instance
    final InMemoryInventoryRepository reloadedRepo = new InMemoryInventoryRepository();
    final List<InventoryItem> pantryItems = reloadedRepo.findByLocation(Location.PANTRY);
    final List<InventoryItem> fridgeItems = reloadedRepo.findByLocation(Location.FRIDGE);
    final List<InventoryItem> freezerItems = reloadedRepo.findByLocation(Location.FREEZER);

    // Assert
    assertEquals(2, pantryItems.size(), "Should find 2 pantry items");
    assertTrue(pantryItems.contains(pantryItem1), "Should contain first pantry item");
    assertTrue(pantryItems.contains(pantryItem2), "Should contain second pantry item");

    assertEquals(1, fridgeItems.size(), "Should find 1 fridge item");
    assertTrue(fridgeItems.contains(fridgeItem), "Should contain the fridge item");

    assertTrue(freezerItems.isEmpty(), "Should find no freezer items");
  }

  // --- Null Argument Tests ---

  @Test
  void saveNullItemShouldThrowNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () -> repository.save(null),
        "Saving null item should throw NullPointerException");
  }

  @Test
  void findByIdNullIdShouldThrowNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () -> repository.findById(null),
        "Finding by null ID should throw NullPointerException");
  }

  @Test
  void deleteByIdNullIdShouldThrowNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () -> repository.deleteById(null),
        "Deleting by null ID should throw NullPointerException");
  }

  @Test
  void findByLocationNullLocationShouldThrowNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () -> repository.findByLocation(null),
        "Finding by null location should throw NullPointerException");
  }
}
