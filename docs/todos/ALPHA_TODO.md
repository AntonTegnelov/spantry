# Spantry Application - Alpha Features TODO List

This document outlines planned features beyond the initial CLI MVP, focusing on achieving a usable Alpha version with more robust capabilities.

## Guiding Principles

- **Robust Persistence:** Move away from file serialization to a proper database solution.
- **Enhanced CLI:** Improve user experience and add more inventory management features.
- **Core Recipe Logic:** Implement basic recipe suggestion functionality.
- **Maintainability:** Continue adhering to SOLID, DI, modularity, and code quality conventions.

## Phase 5: Database Persistence (`com.spantry.persistence` / `com.spantry.inventory`)

- **[Choice]**
  - [ ] Decide on a database technology (e.g., SQLite for embedded simplicity, H2 for in-memory/file, PostgreSQL for full features). _Initial thought: SQLite for ease of setup._
- **[Dependency]**
  - [ ] Add appropriate JDBC driver dependency to `build.gradle.kts`.
  - [ ] Consider adding a lightweight persistence framework/library (e.g., Spring Data JDBC, jOOQ, or stick to plain JDBC initially). _Initial thought: Plain JDBC first to minimize dependencies._
- **[Infrastructure]** (`src/main/java/com.spantry.persistence`)
  - [ ] Create a `DatabaseConnectionManager` or similar utility to handle database connections (pooling if necessary later).
  - [ ] Define database schema (SQL script) for the `inventory_item` table.
  - [ ] Implement schema creation/migration logic (e.g., using a library like Flyway/Liquibase or a simple initialization check).
- **[Repository Implementation]** (`src/main/java/com.spantry.inventory.repository`)
  - [ ] Implement `JdbcInventoryRepository` implementing `InventoryRepository`.
    - [ ] Implement `save()` using SQL `INSERT` or `UPDATE` (handle ID generation if DB doesn't auto-generate).
    - [ ] Implement `findById()` using SQL `SELECT`.
    - [ ] Implement `findAll()` using SQL `SELECT`.
    - [ ] Implement `deleteById()` using SQL `DELETE`.
    - [ ] Implement `findByLocation()` using SQL `SELECT` with `WHERE` clause.
    - [ ] Handle `LocalDate` mapping to/from SQL DATE type.
    - [ ] Implement proper transaction management if operations become more complex.
    - [ ] Implement robust exception handling for `SQLException`s (potentially wrapping them in custom persistence exceptions).
- **[DI Wiring]** (`src/main/java/com.spantry.SpantryApplication`)
  - [ ] Update the Composition Root to instantiate `JdbcInventoryRepository` instead of `InMemoryInventoryRepository`.
  - [ ] Inject the `DatabaseConnectionManager` (or DataSource) into the `JdbcInventoryRepository`.
- **[Testing]**
  - [ ] Adapt existing repository tests or write new integration tests for `JdbcInventoryRepository` (potentially using an in-memory DB like H2 for testing, or Testcontainers if using Docker).
  - [ ] Update E2E tests to verify data persists correctly across application runs using the database.
  - [ ] Remove or disable the file serialization logic in `InMemoryInventoryRepository` (or keep it specifically for certain test profiles).

## Phase 6: Enhanced CLI Features (`com.spantry.cli`)

- **[Inventory]**
  - [ ] Implement `UpdateItemCommand`: Allow changing quantity, location, or expiration date of an existing item.
  - [ ] Add filtering/sorting options to `ListItemsCommand` (e.g., `--location`, `--sort-by-name`, `--sort-by-expiration`).
  - [ ] Implement a command to show items nearing expiration (e.g., `item expiring --days <N>`).
- **[Usability]**
  - [ ] Improve output formatting of `ListItemsCommand` (e.g., use tables).
  - [ ] Provide clearer error messages to the user (consider printing directly to stderr instead of just logging for user-facing errors).

## Phase 7: Core Recipe Module (`com.spantry.recipe`)

- **[Domain]** (`src/main/java/com.spantry.recipe.domain`)
  - [ ] Define `Recipe` class (Properties: `id`, `name`, `description`, `ingredients` (List<String> or List<Ingredient>), `instructions`).
  - [ ] Define `Ingredient` class/record if needed (Properties: `name`, `quantity`, `unit`).
- **[API/Abstractions]** (`src/main/java/com.spantry.recipe.api`)
  - [ ] Define `RecipeRepository` interface (Methods: `save`, `findById`, `findAll`, potentially `findByName`).
  - [ ] Define `RecipeService` interface (Methods: `addRecipe`, `findRecipesByAvailableItems(List<InventoryItem> availableItems)`).
- **[Implementation]**
  - [ ] Implement `InMemoryRecipeRepository` (initially, store recipes in memory or load from a simple file like JSON/CSV).
  - [ ] Implement `RecipeServiceImpl`: Implement the logic for `findRecipesByAvailableItems` (simple matching based on ingredient names for now).
- **[DI Wiring]** (`src/main/java/com.spantry.SpantryApplication`)
  - [ ] Instantiate and wire `RecipeRepository` and `RecipeService`.
- **[CLI Command]** (`src/main/java/com.spantry.cli.command`)
  - [ ] Create `RecipeCommands` (e.g., `@Command(name = "recipe")`).
  - [ ] Implement `FindRecipesCommand`: Injects `InventoryService` and `RecipeService`. Gets available items, calls `recipeService.findRecipesByAvailableItems`, displays results.
  - [ ] Implement `AddRecipeCommand` (optional for alpha): Allows manually adding recipes via CLI.

## Future Considerations (Post-Alpha)

- AI/ML Integration (Image Recognition, Advanced Recipe Matching)
- External Recipe APIs
- User Accounts & Sharing
- Web Interface / Mobile App
- More sophisticated database interactions (ORM, connection pooling)
- Background tasks (e.g., expiration notifications)
- Deployment strategies (Docker, etc.)
