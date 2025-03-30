package com.spantry.inventory.repository;

// import com.spantry.inventory.domain.Item;
import com.spantry.inventory.domain.InventoryItem;
import com.spantry.inventory.domain.Location;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * An in-memory implementation of the {@link InventoryRepository}. Stores items in a
 * ConcurrentHashMap. Data is lost when the application stops. This implementation handles ID
 * generation using UUID.
 *
 * <p>Suppressing UnnecessaryConstructor as PMD contradicts itself here; a public constructor
 * (default or explicit) is needed for instantiation.
 */
@SuppressWarnings({"PMD.UnnecessaryConstructor", "PMD.AtLeastOneConstructor"})
public class InMemoryInventoryRepository implements InventoryRepository {

  // Using ConcurrentHashMap for thread safety, although not strictly needed for initial CLI
  private final Map<String, InventoryItem> inventory = new ConcurrentHashMap<>();

  /** Default constructor (explicitly added to satisfy PMD). */
  // public InMemoryInventoryRepository() {
  //   // No initialization needed
  // }

  @Override
  public InventoryItem save(final InventoryItem item) {
    Objects.requireNonNull(item, "Item cannot be null for saving");

    String itemId = item.getItemId();
    InventoryItem itemToStore;

    if (itemId == null || itemId.isBlank()) {
      // Generate ID and create the final Item object to be stored and returned
      itemId = UUID.randomUUID().toString();
      itemToStore =
          new InventoryItem(
              itemId,
              item.getName(),
              item.getQuantity(),
              item.getLocation(),
              item.getExpirationDate());
    } else {
      // Use the provided item directly if it already has an ID (update scenario)
      itemToStore = item;
    }

    inventory.put(itemToStore.getItemId(), itemToStore);
    return itemToStore; // Return the potentially new item instance with the generated ID
  }

  @Override
  public Optional<InventoryItem> findById(final String itemId) {
    Objects.requireNonNull(itemId, "Item ID cannot be null for findById");
    return Optional.ofNullable(inventory.get(itemId));
  }

  @Override
  public List<InventoryItem> findAll() {
    // Return an unmodifiable list to prevent external modification
    return List.copyOf(inventory.values());
  }

  @Override
  public void deleteById(final String itemId) {
    Objects.requireNonNull(itemId, "Item ID cannot be null for deleteById");
    inventory.remove(itemId);
  }

  @Override
  public List<InventoryItem> findByLocation(final Location location) {
    Objects.requireNonNull(location, "Location cannot be null for findByLocation");
    // Return an unmodifiable list
    return inventory.values().stream()
        .filter(item -> item.getLocation() == location)
        .collect(Collectors.toUnmodifiableList());
  }
}
