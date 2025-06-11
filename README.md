CletaEats
A food delivery app built with Jetpack Compose, following MVVM and Clean Architecture principles.
Architecture

MVVM: Separates UI (composables), presentation (ViewModels), and data (Repository).
Layers:
UI: Jetpack Compose with Material Design 3, modular composables in ui/screens and ui/components.
Presentation: ViewModels in viewmodel using mutableStateOf and coroutines.
Data: CletaEatsRepository handles local file storage (JSON) with Gson, using mock data initialization.
Domain: Business logic in repository methods.


Dependency Injection: Hilt for injecting dependencies (@HiltViewModel, @Inject).
Navigation: Jetpack Navigation with NavHost in NavGraph.kt, with safe argument parsing.

Features

User login/register with validation (cedula, phone, email, address).
Restaurant browsing with responsive design (WindowSizeClass, ListDetailPaneScaffold for large screens).
Order creation with combo validation and closest delivery person assignment.
Order status updates with progress indicators ("en preparación", "entregado").
Reporting for restaurant revenue and popularity.
Local data persistence (clientes.txt, pedidos.txt, etc.) with mock initialization.
Safe navigation with try-catch for JSON parsing.
Animations for UI state changes (AnimatedVisibility, fadeIn/fadeOut).
Retry logic for file I/O operations.

Dependencies

Jetpack Compose: UI framework.
Material Design 3: Theming and components.
Hilt: Dependency injection for navigation.
Coil: Image loading (AsyncImage).
Gson: JSON serialization.

Setup

Clone the repository.
Open in Android Studio (2024.2+ recommended).
Sync project with Gradle.
Set minSdk = 24, targetSdk = 36 in app/build.gradle.
Run on an emulator or device (API 24+).

Navigation



Route
Screen
Description



login
User authentication.



restaurants/{clientId}
RestaurantListScreen
Browse restaurants.


restaurant_details/{clientId}/{restaurantJson}
RestaurantDetailsScreen
Order combos.


profile
ProfileScreen
View user profile.


ordersScreen
OrdersScreen
Manage orders with responsive layout.


reports
ReportsScreen
View restaurant analytics.


Improvements

Responsive Design: Uses WindowSizeClass and ListDetailPaneScaffold for tablets/foldables.
Navigation Safety: Validates clientId and handles JSON parsing errors.
UI Polish: Status indicators in OrderCard, animations for state changes.
Error Handling: Retry logic for file I/O (3 retries with exponential backoff).
Performance: LazyTable prefetching for smooth scrolling.
