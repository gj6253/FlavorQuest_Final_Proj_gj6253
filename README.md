# FlavorQuest  
  
FlavorQuest is an Android application designed to alleviate mealtime decision fatigue. It serves as a dual-purpose culinary companion that helps users either **Cook at Home** by generating AI-powered recipes or **Order Out** by discovering nearby restaurants — all based on their current mood and preferences.  
  
## Features  
  
- **Mood-Based Discovery** — Select a mood (e.g., "Adventurous", "Comfort", "Healthy") to influence suggestions  
- **Cook at Home** — Generate detailed recipes with ingredients, step-by-step instructions, and nutritional info using Google Gemini AI  
- **Order Out** — Find local restaurants with ratings, price levels, and distance via the Google Places API  
- **Smart Refinement** — Chat with the AI to refine recipe results (e.g., "Make it spicier", "Remove the nuts")  
- **Favorites & History** — Save favorite recipes and restaurants locally; sync search history to Firebase for cross-device access  
- **Authentication** — Firebase Auth for user accounts and profile management  
  
## Tech Stack  
  
| Category | Technology |  
|---|---|  
| Language | Kotlin (JVM 17) |  
| UI | Jetpack Compose with Material 3 |  
| Architecture | MVVM (ViewModel + StateFlow + Coroutines) |  
| Local Database | Room 2.6.1 |  
| Cloud Backend | Firebase (Auth, Firestore, Storage) |  
| AI | Google Generative AI (Gemini) 0.9.0 |  
| Networking | Retrofit 2.9.0 + OkHttp |  
| Image Loading | Coil 2.5.0 |  
| Location | Google Play Services Location |  
| Preferences | DataStore |  
  
## Project Structure
app/src/main/java/edu/utap/flavorquest/
├── FlavorQuestApp.kt # Application class (Firebase init)
├── MainActivity.kt # Entry point & navigation host
├── data/
│ ├── ai/ # AI service (Gemini integration)
│ ├── api/ # Places API client
│ ├── local/ # Room database, DAOs, converters
│ ├── model/ # Data models (Recipe, Restaurant, etc.)
│ └── repository/ # Repository (single source of truth)
├── ui/
│ ├── components/ # Reusable Compose components
│ ├── navigation/ # NavGraph & bottom bar
│ ├── screens/ # Feature screens (Home, Results, Detail)
│ └── theme/ # Material 3 theme & design tokens
└── viewmodel/ # ViewModels (HomeViewModel, AuthViewModel)

## Prerequisites  
  
- **Android Studio** Hedgehog (2023.1.1) or newer  
- **JDK 17**  
- **Android SDK**:  
  - `compileSdk`: 34  
  - `minSdk`: 26 (Android 8.0)  
  - `targetSdk`: 34  
  
## Setup  
  
1. **Clone the repository**  
   ```bash  
   git clone https://github.com/gjangid/Falvor_Demo.git  
   cd Falvor_Demo

## Architecture
┌─────────────────────────────────────────────┐  
│  UI Layer (Compose Screens)                 │  
│  ↕ StateFlow                                │  
│  ViewModels (HomeViewModel, AuthViewModel)  │  
│  ↕                                          │  
│  Repository (FlavorQuestRepository)         │  
│  ↕                          ↕               │  
│  Room DB (Local)    Firebase (Cloud Sync)   │  
│  ↕                                          │  
│  AIService (Gemini)  PlacesService (API)    │  
└─────────────────────────────────────────────┘
