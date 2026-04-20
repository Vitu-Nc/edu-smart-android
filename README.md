📚 EDU-SMART – Smart Learning Mobile App

EDU-SMART is a modern AI-powered educational Android application designed to help students access learning materials, take quizzes, watch tutorials, and stay updated with educational news—all in one platform.

Built using Kotlin and Jetpack Compose, the app follows modern Android development practices with a scalable MVVM architecture.

🎥 Demo Video

👉 https://drive.google.com/file/d/1ZSOPBX0xp_OWIfzfmVsxPSW0mShHjoFw/view?usp=drivesd

Home Screen
Library (Offline & Online)
News Module
Quiz Interface
Video Tutorials
🚀 Features
🔐 Authentication
Secure login and signup using Firebase Authentication
📚 Library System
Upload, view, and download PDF learning materials
Search and read online books
📰 News Module
Fetches real-time educational news using external APIs
Displays latest global updates
🎥 Tutorials
Search and watch educational videos inside the app via YouTube integration
🧠 Quiz System
Subject-based quizzes (Maths, Biology, Malawi History, Agriculture, etc.)
Interactive learning experience
💡 Did You Know?
Rotational science trivia for quick learning
🤖 AI Chatbot
Integrated AI assistant powered by Gemini API
📌 Bookmarks
Save books and news articles for later reading
🎨 Modern UI
Built with Jetpack Compose and Material Design principles
🏗 Architecture

The application follows the MVVM (Model-View-ViewModel) architecture:

UI Layer (Jetpack Compose) → Displays data and handles user interaction
ViewModel Layer → Manages UI state and business logic
Repository Layer → Handles data sources (Firebase + APIs)

This structure ensures scalability, maintainability, and clean separation of concerns.

🛠️ Tech Stack
Language: Kotlin
UI: Jetpack Compose
Architecture: MVVM
Backend: Firebase (Authentication, Firestore, Storage)
Networking: Retrofit + OkHttp
🔗 APIs Used
YouTube Data API
The Guardian API
Gemini API
🔑 API Configuration

This project uses external APIs. To run locally:

Create a local.properties file
Add your API keys:
GUARDIAN_API_KEY=your_key_here
YOUTUBE_API_KEY=your_key_here
GEMINI_API_KEY=your_key_here

⚠️ Challenges & Solutions
Handling large file downloads
→ Implemented Android DownloadManager for background downloads
API data delays and failures
→ Added loading states and error handling
Navigation between screens
→ Used Jetpack Compose Navigation with argument passing
🌍 Real-World Impact

EDU-SMART addresses key challenges faced by students, especially in regions like Malawi:

Limited access to learning materials
High cost of textbooks
Need for centralized educational resources
🚧 Future Improvements
Offline learning mode
Personalized AI recommendations
User progress tracking
Push notifications for new content
📦 Installation
Clone the repository
Open in Android Studio
Add API keys in local.properties
Run the app

