# Cosmos

A modular framework for Spigot/Paper plugin development focused on reusable systems, scalable architecture, and cleaner APIs.

Cosmos provides ready-to-use frameworks for:

- Inventory menus
- Menu components
- GUI animations
- Command systems
- Player input workflows
- Shared utilities

Instead of rebuilding infrastructure for every plugin, Cosmos helps developers focus on gameplay and features.

---

# Modules

| Module | Description |
|---|---|
| `cosmos-menu` | Inventory GUI framework with components and animations |
| `cosmos-command` | Structured command framework |
| `cosmos-input` | Player input and response handling |
| `cosmos-utilities` | Shared utility APIs and builders |
| `cosmos-core` | Internal shared abstractions |

---

# Features

## Menu Framework

Create reusable and scalable inventory GUIs without manually handling Bukkit inventory events.

Features include:

- Component-based menus
- Pagination
- Pattern layouts
- Slot utilities
- Dynamic rendering
- Inventory protection
- Menu lifecycle management

---

## Menu Components

Menus are built using reusable components instead of hardcoded click logic.

Examples:

- Static buttons
- Paginated content
- Decorative patterns
- Dynamic components
- Custom components

---

## Menu Animations

Built-in animation framework for inventory interfaces.

Examples:

- Sequential animations
- Wave animations
- Looping animations
- Dynamic menu updates

Useful for:

- Lobby menus
- Cosmetic GUIs
- Minigame interfaces
- Interactive dashboards

---

## Command Framework

A modular command architecture that replaces large `onCommand()` methods.

Features include:

- Subcommands
- Permission handling
- Sender validation
- Argument parsing
- Structured execution flow

---

## Input System

Handle temporary player input workflows without manually tracking chat state.

Useful for:

- Setup wizards
- Confirmation prompts
- Interactive configuration
- Chat-based workflows

---

## Utility APIs

Shared utilities designed for plugin development.

Includes:

- Item builders
- Inventory utilities
- Reflection helpers
- Collection utilities
- Version compatibility helpers

---

# Why Cosmos?

Spigot plugin development often involves rewriting the same systems repeatedly.

Cosmos centralizes these systems into reusable modules designed for:

- Faster development
- Cleaner architecture
- Better maintainability
- Modular design
- Reusability

---

# Installation

Cosmos is available on Maven Central.

No additional repository configuration is required.

---

## Maven

Replace `MODULE_NAME` with the module you want to use.

```xml
<dependency>
    <groupId>io.gitlab.xnaut97</groupId>
    <artifactId>MODULE_NAME</artifactId>
    <version>1.0.0</version>
</dependency>
```

Example:

```xml
<dependency>
    <groupId>io.gitlab.xnaut97</groupId>
    <artifactId>cosmos-menu</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## Gradle (Groovy DSL)

```gradle
implementation 'io.gitlab.xnaut97:MODULE_NAME:1.0.0'
```

Example:

```gradle
implementation 'io.gitlab.xnaut97:cosmos-menu:1.0.0'
```

---

## Gradle (Kotlin DSL)

```kotlin
implementation("io.gitlab.xnaut97:MODULE_NAME:1.0.0")
```

Example:

```kotlin
implementation("io.gitlab.xnaut97:cosmos-menu:1.0.0")
```

---

## Available Modules

| Module | Artifact |
|---|---|
| Menu Framework | `cosmos-menu` |
| Command Framework | `cosmos-command` |
| Input System | `cosmos-input` |
| Utilities | `cosmos-utilities` |
| Core APIs | `cosmos-core` |

---

---

# Documentation

Full documentation and guides are available in the Wiki.

## Wiki Sections

- Getting Started
- Menu Framework
- Command Framework
- Input System
- Utilities
- Examples

➡ Wiki:  
https://github.com/xnaut97/Cosmos/wiki

---

# Example Use Cases

Cosmos is designed for projects such as:

- Minigame servers
- RPG systems
- Lobby frameworks
- Administrative tools
- Interactive GUI plugins
- Setup/configuration systems

---

# Requirements

- Java 8+
- Spigot/Paper server

---

# Building

```bash
mvn clean install
```

---

# Contributing

Contributions, issues, and suggestions are welcome.

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Open a pull request

---
