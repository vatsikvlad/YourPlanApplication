# Mobile Scheduler (ProjectMobileApplications)

A modern Android mobile application for time management, event planning, and daily task organization. Built using the latest Google technologies, including Jetpack Compose and Material Design 3.

## Main Features

- **Event Management**: Create, edit, and delete one-time or recurring (weekly) events.
- **Smart Notifications**: Integrated alert system that reminds you 10 minutes before an event starts and at the exact start time.
- **Task Checklists (Todo)**: Each event can have its own sub-task list, allowing for precise progress tracking.
- **Conflict Detection**: Automatic time validation mechanism that prevents overlapping activities.
- **Export to Google Calendar**: Quick synchronization of local plans with the system Google Calendar app.
- **Adaptive Interface**: Specialized layout for tablets (**TabletLayout**) that takes advantage of larger screen real estate.
- **Personalization**: Full support for Dark Mode, time format selection (12h/24h), and multi-language support.

## Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Declarative UI)
- **Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room) (SQLite)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Design System**: [Material Design 3](https://m3.material.io/) (Material You)
- **State Management**: Kotlin Flow & Compose State
- **Background Tasks**: AlarmManager & NotificationHelper

## Project Architecture

The project follows **Clean Architecture** principles and the **MVVM** pattern:

- `data/`: Contains Room database definitions, entities (`ScheduleEntity`), and DAOs.
- `viewmodel/`: Business logic, state handling, and database interaction (`ElementsViewModel`).
- `phone/` & `tablet/`: UI components specific to the device form factor.
- `notifications/`: Handling system BroadcastReceivers and scheduling alerts.
- `ui/theme/`: Definitions for colors, typography, and Material 3 themes.

## Installation & Setup

1. Clone the repository:
   
