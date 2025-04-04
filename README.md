# Spantry: Smart Pantry

**Note:** This project is currently in the **MVP stage**. The core command-line inventory management features are implemented, but it's not yet ready for general use.

## Description

Spantry is an application designed to help you keep track of items in your fridge and pantry, including their expiration dates.

**Current MVP (Command-Line Interface):**

The current version provides a basic command-line interface (CLI) for managing your inventory:

- Add items with name, quantity, location (FRIDGE, PANTRY, FREEZER), and optional expiration date.
- List all items in your inventory.
- Remove items by their ID.

Data is currently stored in memory (persisted to a file `build/e2e-inventory.dat` using Java Serialization for testing purposes) and will be lost if the application is cleaned or the data file is deleted.

**Future Goals:**

- Persistent storage using a proper database.
- Recipe suggestions based on available ingredients.
- AI-powered features (e.g., grocery image recognition).
- Mobile application interface.

## Features (Current MVP)

- **Add Items:** `item add -n <name> -q <quantity> -l <LOCATION> [-e YYYY-MM-DD]`
- **List Items:** `item list`
- **Remove Items:** `item remove -i <item-id>`
- Basic input validation.
- Persistence across runs via file serialization (primarily for testing).

## Technologies (Current)

- Java 17
- Gradle (Build Tool)
- Picocli (CLI Framework)
- SLF4j + Logback (Logging)
- JUnit 5 (Testing)
- Checkstyle, PMD, Spotless (Code Quality)
- Jakarta Bean Validation (Input Validation)

## Setup

1.  **Prerequisites:** JDK 17 (Adoptium recommended) installed and configured.
2.  **Clone the repository:** `git clone <repository-url>`
3.  **Navigate to the project directory:** `cd spantry`
4.  **Build the project:** `./gradlew build` (or `gradlew.bat build` on Windows)

## Usage

After building, you can run the application using the scripts generated by Gradle:

- **Linux/macOS:** `./build/install/spantry/bin/spantry <command> [options]`
- **Windows:** `build\install\spantry\bin\spantry.bat <command> [options]`

**Examples:**

- Add an item: `build\install\spantry\bin\spantry.bat item add -n "Milk" -q 1 -l FRIDGE -e 2024-04-15`
- List items: `build\install\spantry\bin\spantry.bat item list`
- Remove item (assuming ID is 'abc-123'): `build\install\spantry\bin\spantry.bat item remove -i abc-123`

You can get help on commands:

- `build\install\spantry\bin\spantry.bat --help`
- `build\install\spantry\bin\spantry.bat item --help`
- `build\install\spantry\bin\spantry.bat item add --help`

## Contributing

(Information on how to contribute to the project will go here.)

## License

(License information will go here.)
