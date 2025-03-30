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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link InMemoryInventoryRepository}. */
class InMemoryInventoryRepositoryTest {

  private InMemoryInventoryRepository repository;

  @BeforeEach
  void setUp() {
    repository = new InMemoryInventoryRepository();
  }

  @Test
  void save_newItem_shouldGenerateIdAndStore() {
    // Arrange
    InventoryItem newItem =
        new InventoryItem(null, "Test Item", 1, Location.PANTRY, Optional.empty());

    // Act
    InventoryItem savedItem = repository.save(newItem);

    // Assert
    assertNotNull(savedItem);
    assertNotNull(savedItem.getItemId(), "ID should be generated");
    assertEquals(newItem.getName(), savedItem.getName());
    assertEquals(newItem.getQuantity(), savedItem.getQuantity());
    assertEquals(newItem.getLocation(), savedItem.getLocation());
    assertEquals(newItem.getExpirationDate(), savedItem.getExpirationDate());

    // Verify item is stored and retrievable
    Optional<InventoryItem> found = repository.findById(savedItem.getItemId());
    assertTrue(found.isPresent());
    assertEquals(savedItem, found.get());
  }

  @Test
  void save_existingItem_shouldUpdateAndStore() {
    // Arrange
    InventoryItem initialItem =
        new InventoryItem(null, "Update Me", 1, Location.FRIDGE, Optional.empty());
    InventoryItem savedInitial = repository.save(initialItem);
    String existingId = savedInitial.getItemId();

    InventoryItem updatedItem =
        new InventoryItem(existingId, "Updated Name", 5, Location.FRIDGE, Optional.empty());

    // Act
    InventoryItem savedUpdated = repository.save(updatedItem);

    // Assert
    assertNotNull(savedUpdated);
    assertEquals(existingId, savedUpdated.getItemId()); // ID should remain the same
    assertEquals(updatedItem.getName(), savedUpdated.getName());
    assertEquals(updatedItem.getQuantity(), savedUpdated.getQuantity());

    // Verify updated item is stored
    Optional<InventoryItem> found = repository.findById(existingId);
    assertTrue(found.isPresent());
    assertEquals(savedUpdated, found.get());
  }

  @Test
  void findById_existingItem_shouldReturnItem() {
    // Arrange
    InventoryItem item =
        repository.save(new InventoryItem(null, "Find Me", 1, Location.CUPBOARD, Optional.empty()));
    String id = item.getItemId();

    // Act
    Optional<InventoryItem> found = repository.findById(id);

    // Assert
    assertTrue(found.isPresent());
    assertEquals(item, found.get());
  }

  @Test
  void findById_nonExistentItem_shouldReturnEmpty() {
    // Act
    Optional<InventoryItem> found = repository.findById("non-existent-id");

    // Assert
    assertTrue(found.isEmpty());
  }

  @Test
  void findAll_whenEmpty_shouldReturnEmptyList() {
    // Act
    List<InventoryItem> allItems = repository.findAll();

    // Assert
    assertNotNull(allItems);
    assertTrue(allItems.isEmpty());
  }

  @Test
  void findAll_withItems_shouldReturnAllItems() {
    // Arrange
    InventoryItem item1 =
        repository.save(new InventoryItem(null, "Item 1", 1, Location.PANTRY, Optional.empty()));
    final InventoryItem item2 =
        repository.save(
            new InventoryItem(null, "Item 2", 2, Location.FRIDGE, Optional.of(LocalDate.now())));

    final InventoryItem pantryItem2 =
        repository.save(new InventoryItem(null, "Pantry 2", 2, Location.PANTRY, Optional.empty()));

    // Act
    List<InventoryItem> pantryItems = repository.findByLocation(Location.PANTRY);
    List<InventoryItem> fridgeItems = repository.findByLocation(Location.FRIDGE);
    List<InventoryItem> freezerItems = repository.findByLocation(Location.FREEZER);

    // Assert
    assertEquals(2, pantryItems.size());
    assertTrue(pantryItems.contains(item1));
    assertTrue(pantryItems.contains(pantryItem2));

    assertEquals(1, fridgeItems.size());
    assertTrue(fridgeItems.contains(item2));

    assertTrue(freezerItems.isEmpty());
  }

  @Test
  void deleteById_existingItem_shouldRemoveItem() {
    // Arrange
    InventoryItem item =
        repository.save(
            new InventoryItem(null, "Delete Me", 1, Location.FREEZER, Optional.empty()));
    String id = item.getItemId();
    assertTrue(repository.findById(id).isPresent(), "Item should exist before delete");

    // Act
    repository.deleteById(id);

    // Assert
    assertTrue(repository.findById(id).isEmpty(), "Item should not exist after delete");
  }

  @Test
  void deleteById_nonExistentItem_shouldDoNothing() {
    // Arrange
    repository.save(new InventoryItem(null, "Existing", 1, Location.COUNTER, Optional.empty()));
    long initialCount = repository.findAll().size();

    // Act
    repository.deleteById("non-existent-id");

    // Assert
    assertEquals(initialCount, repository.findAll().size(), "Size should not change");
  }

  @Test
  void findByLocation_shouldReturnMatchingItems() {
    // Arrange
    final InventoryItem pantryItem1 =
        repository.save(new InventoryItem(null, "Pantry 1", 1, Location.PANTRY, Optional.empty()));
    final InventoryItem fridgeItem =
        repository.save(new InventoryItem(null, "Fridge 1", 1, Location.FRIDGE, Optional.empty()));
    final InventoryItem pantryItem2 =
        repository.save(new InventoryItem(null, "Pantry 2", 2, Location.PANTRY, Optional.empty()));

    // Act
    List<InventoryItem> pantryItems = repository.findByLocation(Location.PANTRY);
    List<InventoryItem> fridgeItems = repository.findByLocation(Location.FRIDGE);
    List<InventoryItem> freezerItems = repository.findByLocation(Location.FREEZER);

    // Assert
    assertEquals(2, pantryItems.size());
    assertTrue(pantryItems.contains(pantryItem1));
    assertTrue(pantryItems.contains(pantryItem2));

    assertEquals(1, fridgeItems.size());
    assertTrue(fridgeItems.contains(fridgeItem));

    assertTrue(freezerItems.isEmpty());
  }

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
