---

# 🧠 beWise – The Ultimate Productivity & Focus Super-App

beWise is a next-generation productivity and focus management application designed to help users **reclaim their time**. It combines a powerful task manager, deep focus tools, a ruthless system-level App Blocker, multiplayer accountability, and cutting-edge AI task verification — all wrapped in a premium **Liquid Glass** aesthetic.

---

## ✨ Features

### 🛡️ System-Level App Blocker

A ruthless Android Accessibility-Service-powered productivity guardian.

* **Ruthless Intervention:** Detects when you open distracting apps (Instagram, TikTok, etc.).
* **Instant Kill Switch:** Force-closes them instantly.
* **Red Alert Mode:** Flashes a full-screen warning: **“Get Back to Work!”**
* **Timer-Based:** Only activates when your Focus Timer is running.

---

### ☁️ Cloud Sync & Multiplayer

Stay productive alone or with friends — in real time.

* **Google Login:** Secure sign-in with Firebase Authentication.
* **Realtime Sync:** Tasks, points, streaks, and data sync across devices via Firestore.
* **Global Leaderboard:** Compete worldwide and track your rank live.

---

### 👥 Social Squads

Community + accountability = unstoppable focus.

* **Private Squads:** Create groups with secret 6-digit access codes.
* **Public Squads:** Discover open productivity communities.
* **Live Chat (Coming Soon):** Stay connected with your squad in real time.

---

### 🎨 Premium “Liquid Glass” UI

A uniquely immersive design built with Jetpack Compose.

* **Dual Theme Engine:** Void Black (Dark) & Silver Mist (Light).
* **Glassmorphism Everywhere:** Frosted cards, dynamic mesh gradients, glassy blur layers.
* **Immersive Mode:** Full-screen, distraction-free experience with hidden system bars.

---

### 📊 Advanced Analytics

Understand your productivity at a glance.

* **GitHub-Style Heatmap:** See your last 100 days of productivity.
* **Streak Counter:** Track consecutive days of hitting your goals.
* **Smart Points System:** Earn points, unlock badges (“Novice”, “Master”, “Productivity God”).

---

### 🔮 AI Task Verification (In Development)

Ensuring 100% honest productivity using Google Gemini Vision.

* **Camera Verification:** Snap a photo of your completed task (notes, workout, clean room).
* **AI Analysis:** The Gemini Vision API checks if the task is actually completed.
* **Auto-Completion:** Verified tasks are automatically marked as done.

---

## 🏗️ Architecture

### 🧩 Technology Stack

| Layer              | Technology                                   |
| ------------------ | -------------------------------------------- |
| **Frontend**       | Kotlin, Jetpack Compose (Material 3)         |
| **Backend**        | Firebase Firestore & Firebase Authentication |
| **Local Data**     | Room Database (with sync logic)              |
| **Focus Engine**   | Android Accessibility Services               |
| **AI Integration** | Google Gemini API (Vision)                   |

---

### 📁 Project Structure

```
beWise/
├── app/
│   ├── src/main/java/com/example/productivitycontrol/
│   │   ├── MainActivity.kt            # Entry point & Immersive Mode
│   │   ├── AppViewModel.kt            # Brain: Cloud Logic, Stats, State
│   │   ├── ui/theme/                  # Liquid Glass Theme Engine
│   │   ├── HomeScreen.kt              # Dashboard & Timer
│   │   ├── GroupScreens.kt            # Social Squads Logic
│   │   ├── FeatureScreens.kt          # Leaderboard, Calendar, Badges
│   │   ├── AppBlockerService.kt       # Accessibility Service (The Watchdog)
│   │   └── ...
│   └── google-services.json           # Firebase Config
└── README.md
```

---

## 🚀 Getting Started

### ✔️ Prerequisites

* Android Studio Hedgehog (or newer)
* Android device running **Android 8.0+**
* Firebase account

---

### 📦 Installation

#### 1. Clone the Repository

```bash
git clone https://github.com/AkashK0907/-DOCTYPE-html.git
```

(You may rename the repo link later if needed.)

---

#### 2. Open in Android Studio

File → Open → Select the project folder.

---

#### 3. Add Firebase Config

1. Create a project in **Firebase Console**.
2. Download the `google-services.json`.
3. Place it inside the **app/** folder.

---

#### 4. Build & Run

Click the **Run (▶️)** button in Android Studio.

👉 **Important:** When prompted on your device, enable **Accessibility Permissions** to activate the App Blocker.

---

## 👥 Team

* **Akash K** — Lead Developer
  GitHub: [@AkashK0907](https://github.com/AkashK0907)
* **Akshayaditya K R** — UI/UX Developer
  GitHub: [@AkShAdIt](https://github.com/AkShAdIt)
* **Akash H G** — AI Developer
  GitHub: [@akashhg2007](https://github.com/akashhg2007)
* **Aniketh V** — Lead Researcher
  GitHub: [@aniketh-collab](https://github.com/aniketh-collab)
* **Akash K J** — Tester
  GitHub: [@kjakash05-lang](https://github.com/kjakash05-lang)
* **Pragna** — AI Developer
  GitHub: [@pragna1526](https://github.com/pragna1526)
  

---

## 🙏 Acknowledgments

* **Google Firebase** for backend services.
* **Jetpack Compose** for the modern UI toolkit.
* Hackathon 2025 organizers for the inspiration.

Built with ❤️ and way too much caffeine.

---
