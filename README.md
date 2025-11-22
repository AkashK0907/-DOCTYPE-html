🧠 beWise - The Ultimate Productivity & Focus Super-App

beWise is a comprehensive productivity and focus management application designed to help users reclaim their time. It combines a powerful task manager with a ruthless, system-level App Blocker, all wrapped in a premium "Liquid Glass" aesthetic.

✨ Key Features

🛡️ System-Level App Blocker

Ruthless Intervention: Uses Android Accessibility Services to detect when you open distracting apps (Instagram, TikTok, etc.).

Instant Kill Switch: Instantly forces the distracting app closed.

Red Alert: Flashes a full-screen "Get Back to Work!" warning to break the dopamine loop.

Timer-Based: Only active when your Focus Timer is running.

☁️ Cloud Sync & Multiplayer

Google Login: Secure authentication powered by Firebase.

Real-time Database: Tasks, points, and streaks sync instantly across devices using Firestore.

Global Leaderboard: Compete with friends and see your rank worldwide in real-time.

👥 Social Squads

Private Groups: Create exclusive productivity circles with secret 6-digit codes.

Public Squads: Browse and join communities to find study partners.

Live Chat (Coming Soon): Stay accountable with your squad.

🎨 Premium "Liquid Glass" UI

Dual Theme Engine: Seamlessly switches between Void Black (Dark) and Silver Mist (Light) modes.

Glassmorphism: Features frosted glass cards, dynamic mesh gradients, and shiny borders.

Immersive Experience: Full-screen mode removes system bars for deep focus.

📊 Advanced Analytics

GitHub-style Heatmap: Visualizes your consistency over the last 100 days.

Streak Counter: Tracks consecutive days of productivity.

Smart Points System: Earn 10 pts per task. Unlock badges like "Novice", "Master", and "Productivity God".

🔮 Future Innovation: AI Task Verification

We are actively developing a groundbreaking feature using the Google Gemini API:

Camera Verification: Instead of just checking a box, users will snap a picture of their work (e.g., completed notes, clean room, gym equipment).

AI Analysis: The Gemini Vision model will analyze the image to verify the task was actually completed.

Auto-Completion: If valid, the task is marked done automatically, ensuring 100% honest productivity.

🏗️ Architecture

Technology Stack

Frontend: Kotlin, Jetpack Compose (Material 3)

Backend: Firebase (Firestore, Authentication)

Local Data: Room Database (with Cloud Sync logic)

Core API: Android Accessibility Services

AI Integration: Google Gemini API (Computer Vision)

Project Structure

beWise/
├── app/
│   ├── src/main/java/com/example/productivitycontrol/
│   │   ├── MainActivity.kt          # Entry point & Immersive Mode
│   │   ├── AppViewModel.kt          # Brain: Cloud Logic, Stats, State
│   │   ├── ui/theme/                # Liquid Glass Theme Engine
│   │   ├── HomeScreen.kt            # Dashboard & Timer
│   │   ├── GroupScreens.kt          # Social Squads Logic
│   │   ├── FeatureScreens.kt        # Leaderboard, Calendar, Badges
│   │   ├── AppBlockerService.kt     # Accessibility Service (The Watchdog)
│   │   └── ...
│   └── google-services.json         # Firebase Config
└── README.md


🚀 Getting Started

Prerequisites

Android Studio Hedgehog (or newer)

Android Device (Android 8.0+)

Firebase Account

Installation

Clone the repository:

git clone [https://github.com/AkashK0907/-DOCTYPE-html.git](https://github.com/AkashK0907/-DOCTYPE-html.git)


Open in Android Studio.

Add Firebase Config:

Create a project on Firebase Console.

Download google-services.json.

Place it in the app/ folder.

Build & Run:

Click the Green Play Button (▶️).

Important: Grant "Accessibility Permissions" on your phone when prompted to enable the App Blocker.

👥 Team

Akash K - Lead Developer (@AkashK0907)

🙏 Acknowledgments

Google Firebase for the robust backend.

Jetpack Compose for the beautiful UI toolkit.

Hackathon Organizers for the inspiration to build this.

Built with ❤️ and too much caffeine for Hackathon 2025.
