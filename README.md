# 📚 Bookstore Android App

A premium, fully-functional Android bookstore application built with **Kotlin** and **Jetpack Compose**, powered by a **Firebase** backend.

## ✨ Features

### 👤 User App
- **Modern UI/UX:** Built entirely with Jetpack Compose for a smooth and responsive experience.
- **Book Browsing:** Explore books by categories with real-time search filtering.
- **Book Previews:** Native PDF and image-based previews for books.
- **Shopping Cart:** Dynamic total calculation with per-item selection and robust Firestore-backed persistence.
- **Checkout System:** Secure checkout with promo code support and real-time stock management using atomic Firestore transactions.
- **Order Tracking:** Keep track of placed orders and their status.
- **Rating & Reviews:** Robust 1-5 star rating system with text reviews, calculating average ratings in real-time.
- **User Profiles:** Manage personal information and order history.

### 🛡️ Admin Dashboard
- **Inventory Management:** Full control over book inventory, including adding, editing, and managing stock quantities (preventing overselling).
- **Category Management:** Create and edit existing product categories.
- **Sales & Orders:** Track overall sales, view detailed order information, and manage order statuses.
- **Promo Codes:** Dynamic promo code management for marketing campaigns.

## 🛠️ Technology Stack

- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose
- **Architecture:** MVVM (Model-View-ViewModel)
- **Backend:** Firebase (Authentication, Firestore Database, Cloud Storage)
- **Asynchronous Programming:** Kotlin Coroutines & Flow

## 🚀 Getting Started

### Prerequisites
- Android Studio (Latest Version recommended)
- JDK 17+
- A Firebase Project (for backend configuration)

### Installation
1. **Clone the repository:**
   ```bash
   git clone https://github.com/Tridibdeb21/BookStore.git
   ```
2. **Open the project in Android Studio:**
   - File > Open... > Select the cloned repository folder.
3. **Setup Firebase:**
   - Create a project on the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app to the project and follow the instructions to download the `google-services.json` file.
   - Place the `google-services.json` file in the `app/` directory of the project.
   - Enable Authentication (Email/Password), Firestore, and Storage in your Firebase Console.
4. **Build and Run:**
   - Sync the Gradle project files.
   - Run the application on an emulator or a physical device.

## 📱 Screenshots
*(Add screenshots of your application here)*

## 🤝 Contributing
Contributions, issues and feature requests are welcome!

## 📝 License
This project is open-source and available under the [MIT License](LICENSE).
