package com.spantry.inventory.service;

import com.spantry.inventory.domain.InventoryItem;
import com.spantry.inventory.domain.Location;
import com.spantry.inventory.repository.InventoryRepository;
import com.spantry.inventory.service.dto.AddItemCommandDto;
import com.spantry.inventory.service.exception.ItemNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link InventoryServiceImpl}. */
@ExtendWith(MockitoExtension.class) // Integrate Mockito with JUnit 5
class InventoryServiceImplTest {

  @Mock // Create a mock instance of InventoryRepository
  private InventoryRepository repository;

  @InjectMocks // Re-enable injection
  private InventoryServiceImpl inventoryService;

  private InventoryItem sampleItem1;
  private InventoryItem sampleItem2;
  private AddItemCommandDto sampleAddCommand;

  /** Default constructor. */
  InventoryServiceImplTest() {
    // Default constructor added to satisfy PMD rule
  }

  @BeforeEach
  void setUp() {
    // Reusable test data
    sampleItem1 = new InventoryItem("id1", "Apple", 5, Location.PANTRY, null);
    sampleItem2 = new InventoryItem("id2", "Milk", 1, Location.FRIDGE, LocalDate.now().plusDays(7));
    sampleAddCommand = new AddItemCommandDto("Banana", 3, Location.COUNTER, null);
  }

  @Test
  void addItemShouldCallRepositorySaveAndReturnItem() {
    // Arrange
    final InventoryItem savedItem =
        new InventoryItem(
            "newId",
            sampleAddCommand.name(),
            sampleAddCommand.quantity(),
            sampleAddCommand.location(),
            sampleAddCommand.expirationDate());

    // Simulate repository save behavior - SIMPLIFIED
    Mockito.when(repository.save(Mockito.any(InventoryItem.class))).thenReturn(savedItem);

    // Act
    final InventoryItem result = inventoryService.addItem(sampleAddCommand);

    // Assert
    Assertions.assertNotNull(result, "Result should not be null");
    Assertions.assertEquals(savedItem.itemId(), result.itemId(), "Item ID should match");
    Assertions.assertEquals(sampleAddCommand.name(), result.name(), "Item name should match");

    // Verify repository.save was called
    Mockito.verify(repository, Mockito.times(1)).save(Mockito.any(InventoryItem.class));
  }

  @Test
  void getAllItemsShouldReturnListFromRepository() {
    // Arrange
    final List<InventoryItem> expectedItems = List.of(sampleItem1, sampleItem2);
    Mockito.when(repository.findAll()).thenReturn(expectedItems);

    // Act
    final List<InventoryItem> actualItems = inventoryService.getAllItems();

    // Assert
    Assertions.assertEquals(expectedItems, actualItems, "Returned list should match expected list");
    Mockito.verify(repository, Mockito.times(1)).findAll(); // Verify findAll was called
  }

  @Test
  void getItemsByLocationShouldReturnFilteredListFromRepository() {
    // Arrange
    final Location targetLocation = Location.PANTRY;
    final List<InventoryItem> expectedItems = List.of(sampleItem1);
    Mockito.when(repository.findByLocation(targetLocation)).thenReturn(expectedItems);

    // Act
    final List<InventoryItem> actualItems = inventoryService.getItemsByLocation(targetLocation);

    // Assert
    Assertions.assertEquals(
        expectedItems, actualItems, "Filtered items should match expected items");
    Mockito.verify(repository, Mockito.times(1))
        .findByLocation(targetLocation); // Verify findByLocation was called
  }

  @Test
  void removeItemWhenItemExistsShouldCallRepositoryDelete() {
    // Arrange
    final String itemIdToRemove = sampleItem1.itemId();
    // Simulate findById returning the item, indicating it exists
    Mockito.when(repository.findById(itemIdToRemove)).thenReturn(Optional.of(sampleItem1));

    // Act
    Assertions.assertDoesNotThrow(
        () -> inventoryService.removeItem(itemIdToRemove),
        "Should not throw exception when removing existing item");

    // Assert
    Mockito.verify(repository, Mockito.times(1))
        .findById(itemIdToRemove); // Verify findById was checked
    Mockito.verify(repository, Mockito.times(1))
        .deleteById(itemIdToRemove); // Verify deleteById was called
  }

  @Test
  void removeItemWhenItemDoesNotExistShouldThrowItemNotFoundException() {
    // Arrange
    final String nonExistentItemId = "nonExistentId";
    // Simulate findById returning empty, indicating item does not exist
    Mockito.when(repository.findById(nonExistentItemId)).thenReturn(Optional.empty());

    // Act & Assert
    final ItemNotFoundException exception =
        Assertions.assertThrows(
            ItemNotFoundException.class,
            () -> {
              inventoryService.removeItem(nonExistentItemId);
            },
            "Should throw ItemNotFoundException when item does not exist");

    Assertions.assertTrue(
        exception.getMessage().contains(nonExistentItemId),
        "Exception message should contain the item ID"); // Check exception message

    Mockito.verify(repository, Mockito.times(1))
        .findById(nonExistentItemId); // Verify findById was checked
    Mockito.verify(repository, Mockito.never())
        .deleteById(Mockito.anyString()); // Verify deleteById was NOT called
  }

  @Test
  void addItemWhenCommandIsNullShouldThrowNullPointerException() {
    Assertions.assertThrows(
        NullPointerException.class,
        () -> inventoryService.addItem(null),
        "Should throw NullPointerException for null command");
    Mockito.verify(repository, Mockito.never())
        .save(Mockito.any()); // Ensure repository save was not called
  }

  @Test
  void getItemsByLocationWhenLocationIsNullShouldThrowNullPointerException() {
    Assertions.assertThrows(
        NullPointerException.class,
        () -> inventoryService.getItemsByLocation(null),
        "Should throw NullPointerException for null location");
    Mockito.verify(repository, Mockito.never())
        .findByLocation(Mockito.any()); // Ensure repository findByLocation was not called
  }

  @Test
  void removeItemWhenItemIdIsNullShouldThrowNullPointerException() {
    Assertions.assertThrows(
        NullPointerException.class,
        () -> inventoryService.removeItem(null),
        "Should throw NullPointerException for null item ID");
    Mockito.verify(repository, Mockito.never())
        .findById(Mockito.any()); // Ensure repository findById was not called
    Mockito.verify(repository, Mockito.never())
        .deleteById(Mockito.any()); // Ensure repository deleteById was not called
  }
}
