# 🎓 UniSwap: The Campus Marketplace

> A full-stack, peer-to-peer campus marketplace Android app built with Kotlin & Jetpack Compose, powered by a Spring Boot REST API.

UniSwap is designed to facilitate safe, fast, and local buying and selling within a university campus. Students can easily list items like textbooks, electronics, and dorm essentials, while browsing a real-time feed of available goods from their peers.

---

## 📸 Screenshots

| Home / Campus Feed | Sell an Item | Item Details |
| :---: | :---: | :---: |
| <img width="250" height="1079" alt="image" src="https://github.com/user-attachments/assets/3a717e3c-5def-4656-a504-52cef6d4738b" /> | <img width="250" height="1079" alt="image" src="https://github.com/user-attachments/assets/d374b4c1-821a-4bd5-9107-1f0f18312a92" /> | <img width="250" height="1079" alt="image" src="https://github.com/user-attachments/assets/6e3e86ba-058b-42d2-b770-2109f0aa84a1" /> |



---

## ✨ Features

* **Real-Time Campus Feed:** Browse a dynamic list of available items categorized by type (Electronics, Books, Furniture, etc.).
* **Seamless Listing Creation:** Post new items for sale in seconds with title, price, and category selection.
* **Intelligent UI State Management:** Handles loading, success, and error states gracefully using Kotlin `StateFlow`.
* **Robust Backend:** Secure and fast data processing powered by a custom Spring Boot API.

---

## 🛠️ Tech Stack & Architecture

This project strictly adheres to enterprise-level architecture patterns on both the client and server sides.

### **📱 Frontend (Android)**
* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose (100% Declarative UI)
* **Architecture:** MVVM (Model-View-ViewModel) + Clean Architecture principles
* **Concurrency:** Kotlin Coroutines
* **State Management:** `StateFlow` & `MutableStateFlow`
* **Networking:** Retrofit & OkHttp

### **⚙️ Backend (Server)**
* **Language:** Java 17+
* **Framework:** Spring Boot 3.x
* **Architecture:** 3-Tier Layered Architecture (Controller ➔ Service ➔ Repository)
* **Database Management:** Spring Data JPA
* **Database:** H2 (In-Memory for rapid development) / PostgreSQL (Production)
* **Design Patterns:** Dependency Injection (IoC), Data Transfer Objects (DTOs), Global Exception Handling (`@RestControllerAdvice`)

---

## 🚀 Getting Started (Local Development)

To run this project locally, you will need to run both the Spring Boot backend and the Android frontend.

### 1. Clone the Repository
```bash
git clone [https://github.com/NavneetSindhu/UniSwap.git](https://github.com/NavneetSindhu/UniSwap.git)
