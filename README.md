# Restaurant Management App with UI

A JavaFX desktop application for managing restaurant operations through a clean, interactive GUI. The app persists data in a PostgreSQL database using Hibernate (JPA) and reads basic settings from a simple JSON config file.

## Features
- JavaFX GUI (FXML-based UI)
- Database-backed persistence (PostgreSQL)
- ORM layer using Hibernate / Jakarta Persistence (JPA)
- JSON configuration (`config.json`)
- Designed as a small restaurant management system (menu / entities / operations depending on the implemented screens)

## Tech Stack
- Java 21
- JavaFX **21** (`javafx-controls`, `javafx-fxml`)
- Hibernate Core 6.2.x
- PostgreSQL JDBC Driver
- Gson (for JSON)

## Project Structure
- `src/` — application source code (Java + FXML)
- `config.json` — configuration file
- `pom.xml` — Maven build config and dependencies
