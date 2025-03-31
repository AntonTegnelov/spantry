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

  // Delete the data file before and after each test
  @BeforeEach
  @AfterEach // Run after as well to clean up for subsequent test classes if any
  void manageDataFile() {
    InMemoryInventoryRepository.deleteDataFile(); // Call the static delete method
    repository = new InMemoryInventoryRepository(); // Create a new instance for the test
  }

  @Test
  void save_newItem_shouldGenerateIdAndStore() {
    // Arrange
    InventoryItem newItem = new InventoryItem(null, "Test Item", 1, Location.PANTRY, null);

    // Act
    InventoryItem savedItem = repository.save(newItem);

    // Assert
    assertNotNull(savedItem);
    assertNotNull(savedItem.itemId(), "ID should be generated");
    assertEquals(newItem.name(), savedItem.name());
    assertEquals(newItem.quantity(), savedItem.quantity());
    assertEquals(newItem.location(), savedItem.location());
    assertEquals(newItem.expirationDate(), savedItem.expirationDate());

    // Verify item is stored and retrievable (reloads from file)
    InMemoryInventoryRepository reloadedRepo = new InMemoryInventoryRepository();
    Optional<InventoryItem> found = reloadedRepo.findById(savedItem.itemId());
    assertTrue(found.isPresent());
    assertEquals(savedItem, found.get());
  }

  @Test
  void save_existingItem_shouldUpdateAndStore() {
    // Arrange
    InventoryItem initialItem = new InventoryItem(null, "Update Me", 1, Location.FRIDGE, null);
    InventoryItem savedInitial = repository.save(initialItem);
    String existingId = savedInitial.itemId();

    InventoryItem updatedItem =
        new InventoryItem(existingId, "Updated Name", 5, Location.FRIDGE, null);

    // Act
    InventoryItem savedUpdated = repository.save(updatedItem);

    // Assert
    assertNotNull(savedUpdated);
    assertEquals(existingId, savedUpdated.itemId()); // ID should remain the same
    assertEquals(updatedItem.name(), savedUpdated.name());
    assertEquals(updatedItem.quantity(), savedUpdated.quantity());

    // Verify updated item is stored (reloads from file)
    InMemoryInventoryRepository reloadedRepo = new InMemoryInventoryRepository();
    Optional<InventoryItem> found = reloadedRepo.findById(existingId);
    assertTrue(found.isPresent());
    assertEquals(savedUpdated, found.get());
  }

  @Test
  void findById_existingItem_shouldReturnItem() {
    // Arrange
    LocalDate expiry = LocalDate.now();
    InventoryItem item =
        repository.save(new InventoryItem(null, "Find Me", 1, Location.CUPBOARD, expiry));
    String id = item.itemId();

    // Act: Use a new instance to force reload from file
    InMemoryInventoryRepository reloadedRepo = new InMemoryInventoryRepository();
    Optional<InventoryItem> found = reloadedRepo.findById(id);

    // Assert
    assertTrue(found.isPresent());
    assertEquals(item, found.get());
  }

  @Test
  void findById_nonExistentItem_shouldReturnEmpty() {
    // Arrange: Add an item so the file exists
    repository.save(new InventoryItem(null, "Other", 1, Location.OTHER, null));
    // Act: Use a new instance
    InMemoryInventoryRepository reloadedRepo = new InMemoryInventoryRepository();
    Optional<InventoryItem> found = reloadedRepo.findById("non-existent-id");

    // Assert
    assertTrue(found.isEmpty());
  }

  @Test
  void findAll_whenEmpty_shouldReturnEmptyList() {
    // Arrange: Ensure file is deleted (done by @BeforeEach)
    // Act: Create new instance (which loads from non-existent file)
    InMemoryInventoryRepository reloadedRepo = new InMemoryInventoryRepository();
    List<InventoryItem> allItems = reloadedRepo.findAll();

    // Assert
    assertNotNull(allItems);
    assertTrue(allItems.isEmpty());
  }

  @Test
  void findAll_withItems_shouldReturnAllItems() {
    // Arrange
    InventoryItem item1 =
        repository.save(new InventoryItem(null, "Item 1", 1, Location.PANTRY, null));
    InventoryItem item2 =
        repository.save(new InventoryItem(null, "Item 2", 2, Location.FRIDGE, LocalDate.now()));

    // Act: Use a new instance
    InMemoryInventoryRepository reloadedRepo = new InMemoryInventoryRepository();
    List<InventoryItem> allItems = reloadedRepo.findAll();

    // Assert
    assertEquals(2, allItems.size());
    assertTrue(allItems.contains(item1));
    assertTrue(allItems.contains(item2));
  }

  @Test
  void deleteById_existingItem_shouldRemoveItem() {
    // Arrange
    InventoryItem item =
        repository.save(new InventoryItem(null, "Delete Me", 1, Location.FREEZER, null));
    String id = item.itemId();
    // Verify exists in initial repo
    assertTrue(repository.findById(id).isPresent(), "Item should exist before delete");

    // Act: Delete using the initial repo instance
    repository.deleteById(id);

    // Assert: Check using a new instance that reloads the file
    InMemoryInventoryRepository reloadedRepo = new InMemoryInventoryRepository();
    assertTrue(reloadedRepo.findById(id).isEmpty(), "Item should not exist after delete");
  }

  @Test
  void deleteById_nonExistentItem_shouldDoNothing() {
    // Arrange
    repository.save(new InventoryItem(null, "Existing", 1, Location.COUNTER, null));
    long initialCount = repository.findAll().size();

    // Act: Attempt delete using the initial repo
    repository.deleteById("non-existent-id");

    // Assert: Check using a reloaded repo
    InMemoryInventoryRepository reloadedRepo = new InMemoryInventoryRepository();
    assertEquals(initialCount, reloadedRepo.findAll().size(), "Size should not change");
  }

  @Test
  void findByLocation_shouldReturnMatchingItems() {
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
    assertEquals(2, pantryItems.size());
    assertTrue(pantryItems.contains(pantryItem1));
    assertTrue(pantryItems.contains(pantryItem2));

    assertEquals(1, fridgeItems.size());
    assertTrue(fridgeItems.contains(fridgeItem));

    assertTrue(freezerItems.isEmpty());
  }

  // --- Null Argument Tests ---

  @Test
  void save_nullItem_shouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> repository.save(null));
  }

  @Test
  void findById_nullId_shouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> repository.findById(null));
  }

  @Test
  void deleteById_nullId_shouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> repository.deleteById(null));
  }

  @Test
  void findByLocation_nullLocation_shouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> repository.findByLocation(null));
  }
}
