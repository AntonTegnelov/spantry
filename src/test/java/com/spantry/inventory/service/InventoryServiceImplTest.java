package com.spantry.inventory.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spantry.inventory.domain.InventoryItem;
import com.spantry.inventory.domain.Location;
import com.spantry.inventory.repository.InventoryRepository;
import com.spantry.inventory.service.dto.AddItemCommandDto;
import com.spantry.inventory.service.exception.ItemNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Unit tests for {@link InventoryServiceImpl}. */
@ExtendWith(MockitoExtension.class) // Integrate Mockito with JUnit 5
class InventoryServiceImplTest {

  @Mock // Create a mock instance of InventoryRepository
  private InventoryRepository mockInventoryRepository;

  @InjectMocks // Re-enable injection
  private InventoryServiceImpl inventoryService;

  private InventoryItem sampleItem1;
  private InventoryItem sampleItem2;
  private AddItemCommandDto sampleAddCommandDto;

  @BeforeEach
  void setUp() {
    // Removed manual initialization
    // Reusable test data
    sampleItem1 = new InventoryItem("id1", "Apple", 5, Location.PANTRY, null);
    sampleItem2 = new InventoryItem("id2", "Milk", 1, Location.FRIDGE, LocalDate.now().plusDays(7));
    sampleAddCommandDto = new AddItemCommandDto("Banana", 3, Location.COUNTER, null);
  }

  @Test
  void addItem_shouldCallRepositorySaveAndReturnItem() {
    // Arrange
    InventoryItem savedItem =
        new InventoryItem(
            "newId",
            sampleAddCommandDto.name(),
            sampleAddCommandDto.quantity(),
            sampleAddCommandDto.location(),
            sampleAddCommandDto.expirationDate());

    // Simulate repository save behavior - SIMPLIFIED
    when(mockInventoryRepository.save(any(InventoryItem.class))).thenReturn(savedItem);

    // Act
    InventoryItem result = inventoryService.addItem(sampleAddCommandDto);

    // Assert
    assertNotNull(result); // Check result is not null
    assertEquals(savedItem.itemId(), result.itemId());
    assertEquals(sampleAddCommandDto.name(), result.name());

    // Verify repository.save was called
    verify(mockInventoryRepository, times(1)).save(any(InventoryItem.class));
  }

  @Test
  void getAllItems_shouldReturnListFromRepository() {
    // Arrange
    List<InventoryItem> expectedItems = List.of(sampleItem1, sampleItem2);
    when(mockInventoryRepository.findAll()).thenReturn(expectedItems);

    // Act
    List<InventoryItem> actualItems = inventoryService.getAllItems();

    // Assert
    assertEquals(expectedItems, actualItems);
    verify(mockInventoryRepository, times(1)).findAll(); // Verify findAll was called
  }

  @Test
  void getItemsByLocation_shouldReturnFilteredListFromRepository() {
    // Arrange
    Location targetLocation = Location.PANTRY;
    List<InventoryItem> expectedItems = List.of(sampleItem1);
    when(mockInventoryRepository.findByLocation(targetLocation)).thenReturn(expectedItems);

    // Act
    List<InventoryItem> actualItems = inventoryService.getItemsByLocation(targetLocation);

    // Assert
    assertEquals(expectedItems, actualItems);
    verify(mockInventoryRepository, times(1))
        .findByLocation(targetLocation); // Verify findByLocation was called
  }

  @Test
  void removeItem_whenItemExists_shouldCallRepositoryDelete() {
    // Arrange
    String itemIdToRemove = sampleItem1.itemId();
    // Simulate findById returning the item, indicating it exists
    when(mockInventoryRepository.findById(itemIdToRemove)).thenReturn(Optional.of(sampleItem1));

    // Act
    assertDoesNotThrow(() -> inventoryService.removeItem(itemIdToRemove));

    // Assert
    verify(mockInventoryRepository, times(1))
        .findById(itemIdToRemove); // Verify findById was checked
    verify(mockInventoryRepository, times(1))
        .deleteById(itemIdToRemove); // Verify deleteById was called
  }

  @Test
  void removeItem_whenItemDoesNotExist_shouldThrowItemNotFoundException() {
    // Arrange
    String nonExistentItemId = "nonExistentId";
    // Simulate findById returning empty, indicating item does not exist
    when(mockInventoryRepository.findById(nonExistentItemId)).thenReturn(Optional.empty());

    // Act & Assert
    ItemNotFoundException exception =
        assertThrows(
            ItemNotFoundException.class,
            () -> {
              inventoryService.removeItem(nonExistentItemId);
            });

    assertTrue(exception.getMessage().contains(nonExistentItemId)); // Check exception message

    verify(mockInventoryRepository, times(1))
        .findById(nonExistentItemId); // Verify findById was checked
    verify(mockInventoryRepository, never())
        .deleteById(anyString()); // Verify deleteById was NOT called
  }

  @Test
  void addItem_whenCommandIsNull_shouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> inventoryService.addItem(null));
    verify(mockInventoryRepository, never()).save(any()); // Ensure repository save was not called
  }

  @Test
  void getItemsByLocation_whenLocationIsNull_shouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> inventoryService.getItemsByLocation(null));
    verify(mockInventoryRepository, never())
        .findByLocation(any()); // Ensure repository findByLocation was not called
  }

  @Test
  void removeItem_whenItemIdIsNull_shouldThrowNullPointerException() {
    assertThrows(NullPointerException.class, () -> inventoryService.removeItem(null));
    verify(mockInventoryRepository, never())
        .findById(any()); // Ensure repository findById was not called
    verify(mockInventoryRepository, never())
        .deleteById(any()); // Ensure repository deleteById was not called
  }
}
