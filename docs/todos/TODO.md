# Spantry Application - Detailed TODO List (CLI-First, Modular Monolith)

This document outlines the specific tasks for building the Spantry application, starting with a minimal CLI and a focus on a modular structure, SOLID principles, and Dependency Injection (manual wiring initially) for future scalability.

## Guiding Principles

- **Simplicity First:** Start with core inventory tracking (add, list, remove). Defer recipes, AI, database, and complex features.
- **CLI Primary:** The initial interface will be purely command-line based.
- **Modular Design:**
  - Organize code into logical feature packages (e.g., `com.spantry.inventory`).
  - Define clear public APIs for each module using interfaces (in dedicated `api` sub-packages where appropriate).
  - Minimize dependencies _between_ feature modules initially.
- **SOLID & Loose Coupling:**
  - Adhere strictly to SOLID principles, especially Single Responsibility and Interface Segregation.
  - Enforce the Dependency Inversion Principle: High-level components (e.g., CLI commands, Services) MUST depend on abstractions (interfaces), NOT concrete implementations (e.g., `InMemoryInventoryRepository`).
- **Dependency Injection (Manual):** Use interfaces and constructor injection extensively. Wire components together manually in the main application entry point (`Main.java` or dedicated Composition Root).
- **In-Memory Persistence:** Start with the simplest persistence model – store data in memory during application runtime. Data will be lost when the app closes.

## Phase 1: Core Foundation & Setup

- **[Setup]**
  - [x] Initialize Git repository.
  - [x] Setup Gradle project (`gradle wrapper`, `build.gradle.kts`, `settings.gradle.kts`).
  - [x] Setup basic Java directory structure (`src/main/java`, `src/test/java`, etc.).
  - [x] Setup Checkstyle & PMD for code quality (`build.gradle.kts`).
  - [x] Create placeholder `Main` class (`src/main/java/com/spantry/Main.java`).
  - [x] Add JUnit 5 dependencies to `build.gradle.kts` (if not already added by the `java` plugin implicitly, explicitly add `testImplementation("org.junit.jupiter:junit-jupiter-api:...")` and `testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:...")`).
  - [x] Add a simple CLI argument parsing library (e.g., `info.picocli:picocli`) to `build.gradle.kts` dependencies.
  - [x] Create core package structure: `com.spantry.core`, `com.spantry.inventory`, `com.spantry.cli`.

## Phase 2: Feature - Inventory Management (`com.spantry.inventory`)

- **[Domain]** (`src/main/java/com/spantry/inventory/domain`)
  - [x] Define `Item` class (`Item.java`) - Properties: `id` (String), `name` (String), `quantity` (int), `location` (Enum), `expirationDate` (Optional<LocalDate>). Keep this class focused on state and essential invariants.
  - [x] Define `Location` enum (`Location.java`).
- **[API/Abstractions]** (`src/main/java/com/spantry/inventory/api` or `src/main/java/com/spantry/inventory/service` & `repository`)
  - [x] Define `InventoryRepository` interface (`InventoryRepository.java`) - Methods: `save(Item item)`, `findById(String id)`, `findAll()`, `deleteById(String id)`, `findByLocation(Location location)`. Defines the _contract_ for persistence.
  - [x] Define `InventoryService` interface (`InventoryService.java`) - Methods define _use cases_: `addItem(AddItemCommand cmd)`, `getAllItems()`, `getItemsByLocation(Location location)`, `removeItem(String itemId)`. (Consider using Command objects or simple DTOs for input to avoid primitive obsession).
  - [x] Define simple DTOs/Command objects if needed for the Service interface (e.g., `AddItemCommand.java`) to enhance decoupling by preventing domain object details from leaking into the public API.
  - [x] Consider defining custom, specific exceptions (e.g., `ItemNotFoundException`) that the service layer can throw to represent business rule violations or failures.
- **[Implementation - Persistence]** (`src/main/java/com/spantry/inventory/repository`)
  - [x] Implement `InMemoryInventoryRepository` (`InMemoryInventoryRepository.java`) - Implements `InventoryRepository`. Use a `Map<String, Item>` internally. Handle ID generation (e.g., `UUID.randomUUID().toString()`) as an implementation detail of this specific repository. _This class is an implementation detail._
- **[Implementation - Application Logic]** (`src/main/java/com/spantry/inventory/service`)
  - [x] Implement `InventoryServiceImpl` (`InventoryServiceImpl.java`) - Implements `InventoryService`. Inject `InventoryRepository` via constructor. Encapsulates application-specific logic (validation, coordinating repository, potentially throwing custom exceptions defined alongside the service interface).
- **[Unit Tests]** (`src/test/java/com/spantry/inventory/`)
  - [x] Write unit tests for `InventoryServiceImpl` (mock `InventoryRepository`). Test use cases and logic.
  - [x] Write unit tests for `InMemoryInventoryRepository`. Test persistence logic.

## Phase 3: Command-Line Interface (`com.spantry.cli` & `com.spantry.Main`)

- **[Setup]**
  - [x] Configure `picocli` basic setup in `src/main/java/com/spantry/Main.java`. Define a root command class (`SpantryCliApp.java` implementing `Runnable` or `Callable`).
- **[Composition Root / Manual DI Wiring]** (`src/main/java/com/spantry/Main.java` or `SpantryCliApp.java`)
  - [x] This is the _only_ place where concrete implementation classes (like `InMemoryInventoryRepository`, `InventoryServiceImpl`) should be instantiated.
  - [x] Instantiate `InventoryRepository repo = new InMemoryInventoryRepository();`
  - [x] Instantiate `InventoryService service = new InventoryServiceImpl(repo);`
  - [x] Pass the `InventoryService` instance (the interface type!) to the CLI commands via their constructors or a setter method configured by picocli's factory.
- **[Inventory Commands]** (`src/main/java/com/spantry/cli/command`)
  - [x] Create `ItemCommands.java` (e.g., annotated with `@Command(name = "item", subcommands = {AddItemCommand.class, ListItemsCommand.class, RemoveItemCommand.class})`).
  - [x] Implement `AddItemCommand.java` (`@Command(name = "add")`). Use `@Option` for parameters. Inject `InventoryService` (interface!) via constructor/factory. Call the appropriate service method.
  - [x] Implement `ListItemsCommand.java` (`@Command(name = "list")`). Inject `InventoryService` (interface!) via constructor/factory. Call the appropriate service method.
  - [x] Implement `RemoveItemCommand.java` (`@Command(name = "remove")`). Inject `InventoryService` (interface!) via constructor/factory. Call the appropriate service method.
- **[Dependency Injection (DI) Wiring]**
  - [x] Instantiate `InMemoryInventoryRepository` in `Main.java` (Composition Root).
  - [x] Instantiate `InventoryService` (`InventoryServiceImpl`) in `Main.java`, passing the repository.
  - [x] Wire `InventoryService` into Commands (e.g., using Picocli's `IFactory`).
- **[Implement Command Logic]**
  - [x] `AddItemCommand`: Call `inventoryService.addItem()`.
  - [x] `ListItemsCommand`: Call `inventoryService.getAllItems()`.
  - [x] `RemoveItemCommand`: Call `inventoryService.removeItem()`.

## Phase 4: End-to-End Testing (CLI)

- **[Strategy]**
  - [x] Chosen Strategy: Use JUnit 5 test framework to launch the application JAR/process and assert standard output/error streams.
- **[Tests]**
  - [x] Write E2E test for `item add` command: run the app, add an item, verify output (or potential state if observable). _Note: Gradle configuration issue prevents compilation/execution._
  - [x] Write E2E test for `item list` command: run the app, add items, list them, verify output. _Note: Gradle configuration issue prevents compilation/execution._
  - [x] Write E2E test for `item remove` command: run the app, add item, remove it, verify output/list again. _Note: Gradle configuration issue prevents compilation/execution._
