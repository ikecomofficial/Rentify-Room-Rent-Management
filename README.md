# Rentify: Room Rent Management App
---

## 📌 Overview
Room Rent Management App is an Android application built with **Java**, **Firebase Realtime Database**, and **Firebase Authentication**.  
It helps property owners manage multiple properties, rooms, tenants, and rent/bill records seamlessly.

---

## 🚀 Features (v1.0.3)

### 🔑 Authentication
- Google Sign-In
- Email/Password authentication

### 🏠 Property Management
- **Add Property Activity** → Add new properties (Rooms or Shops stored in Firebase nodes)
- **MainActivity** → Lists all added properties  
  - Powered by `FirebaseRecyclerAdapter`
  - Displays user’s added properties in real-time
  - Includes **Account Settings menu**: show user info + logout

### 🏘 Property Details
- **PropertyDetails Activity**:
  - Shows all rooms within a property
  - Uses **custom RecyclerView adapter** to merge data from multiple nodes (room + tenant details)
  - Options: **Edit Property** / **Delete Property**

### 🚪 Room Details
- **RoomDetails Activity**:
  - Displays tenant information
  - Displays rent status
  - Displays electricity bill records (**planned, structure implemented**)

### 📂 Firebase Database Structure
- Properties stored in separate nodes:  
  - `/rooms`
  - `/shops`
- Each property contains tenant + rent details linked to user

---

## 📲 Tech Stack
- **Language**: Java  
- **Database**: Firebase Realtime Database  
- **Authentication**: Firebase Auth (Google & Email/Password)  
- **UI**: XML layouts, RecyclerView, Custom Adapters  
- **Libraries**:  
  - [Glide](https://github.com/bumptech/glide) (for image loading)  
  - Firebase UI Database (RecyclerAdapter)  

---

## 🛠️ Installation & Setup
1. Clone this repository:
