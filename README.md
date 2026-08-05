# 🍔 Food CaF — Admin Panel

**A powerful Android admin dashboard for managing a food ordering platform**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Java](https://img.shields.io/badge/Language-Java%2011-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com)
[![Material Design](https://img.shields.io/badge/UI-Material%20Design%203-6200EE?logo=materialdesign&logoColor=white)](https://m3.material.io)
[![Min SDK](https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-blue)](https://developer.android.com)
[![Target SDK](https://img.shields.io/badge/Target%20SDK-36-brightgreen)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-Proprietary-red)](#license)

*Built by **Code X Pvt Ltd** — A real-time admin dashboard to manage products, orders, payments, users, messaging, banners, and analytics for the Food CaF food ordering ecosystem.*

---

[Features](#-features) · [Architecture](#-architecture) · [Tech Stack](#-tech-stack) · [Screenshots](#-screenshots) · [Setup](#-getting-started) · [Firebase](#-firebase-structure) · [Contributing](#-contributing)

---

## 📋 Overview

**Food CaF Admin** is the back-office companion app for the [Food CaF](https://github.com/Dilshan-DevX/Food-CAF) customer-facing food ordering application. It provides cafeteria/restaurant administrators with a centralized dashboard to:

- Manage the full product catalog (CRUD with multi-image uploads)
- Track and process customer orders & payments in real-time
- Communicate with customers via real-time chat with push notifications
- Monitor business analytics with printable PDF reports
- Manage promotional banners displayed on the customer app
- Administer user accounts (activate/suspend)

The app connects to a shared **Firebase** backend (Firestore + Storage + Auth), ensuring data consistency between the admin and customer apps.

---

## ✨ Features

### 🏠 Dashboard (Home)

| Feature | Description |
|---------|-------------|
| **Live Statistics Cards** | Real-time counters for Total Orders, Completed Sales, Pending Payments, Total Revenue (LKR), Product Count, and User Count — powered by Firestore snapshot listeners |
| **Product Catalog** | Scrollable list of all products with thumbnail, name, price, rating, prep time, and availability status |
| **Global Search** | AutoComplete search bar with product name suggestions; filters products in real-time as you type |
| **Product Detail & Edit** | Tap any product to view/edit all details — title, price, rating, category, description, ingredients, availability toggle, and replace product images |

### 📦 Product Management

| Feature | Description |
|---------|-------------|
| **Add New Product** | Full form with auto-generated Product ID, title, price, rating, preparation time, description, category dropdown, availability switch, and dual image upload |
| **Edit Product** | In-place editing of all product fields with image replacement support |
| **Category Integration** | Dynamic category dropdown loaded from Firestore `categories` collection |
| **Multi-Image Upload** | Upload up to 2 product images to Firebase Storage with unique UUID-based filenames |
| **Availability Toggle** | Switch products between "Available" and "Not Available" with color-coded status indicators |

### 💳 Payment Management

| Feature | Description |
|---------|-------------|
| **Pending Payments List** | Real-time list of unpaid orders with customer name, contact, order date, and total amount (including LKR 100 delivery fee) |
| **Mark as Paid** | One-tap confirmation dialog to mark orders as "Paid" — updates Firestore in real-time |
| **Payment Stats** | Live counters showing pending vs. completed payment counts |
| **Auto-Refresh** | Firestore snapshot listener ensures the list updates instantly when payment status changes |

### 📊 Analytics & Reporting

| Feature | Description |
|---------|-------------|
| **Revenue Overview** | Total revenue calculated from all completed/paid orders |
| **Order Statistics** | Total, Completed, and Pending order counts |
| **Top Selling Products** | Ranked list of top 5 products by quantity sold, with revenue per product |
| **Recent Orders** | Last 10 orders with order ID, date, status chip, and total amount |
| **PDF Report Generation** | Professional HTML-styled sales report rendered via WebView and printed to PDF using Android PrintManager |
| **Quick Action Cards** | Direct navigation to "Manage Payments" and "Add Product" from the analytics screen |

### 👥 User Management

| Feature | Description |
|---------|-------------|
| **User Directory** | Real-time list of all registered users with profile picture, name, email, phone, and address |
| **Search & Filter** | Search users by name or email with smart ranking (prefix matches appear first) |
| **User Profile Dialog** | Detailed profile popup showing all user information including UID and status |
| **Account Status Control** | Activate or Suspend user accounts with confirmation dialog — persisted to Firestore |
| **Status Chips** | Color-coded Active (green) / Suspended (red) status indicators |

### 💬 Real-Time Messaging

| Feature | Description |
|---------|-------------|
| **Chat Inbox** | List of all customers available for chat (admin's own account is excluded) |
| **1:1 Admin-Customer Chat** | Real-time bidirectional messaging with Firestore snapshot listeners |
| **Message Bubbles** | Sent (right-aligned) and received (left-aligned) message styling |
| **Push Notifications** | Local notifications for incoming customer messages when the chat screen is not active |
| **Notification Channels** | Android 8.0+ notification channel support for chat messages |
| **Smart Notification Logic** | Only notifies for messages received after app start time and when chat is not open |

### 🎯 Banner Management

| Feature | Description |
|---------|-------------|
| **Current Banner Preview** | Displays the active banner with title and date |
| **Create/Update Banner** | Form to set banner title, body text, and date |
| **Image Upload with Progress** | Pick banner image from gallery, upload to Firebase Storage with real-time progress bar and percentage |
| **Old Image Cleanup** | Automatically deletes the previous banner image from Storage when replacing |
| **Live Preview** | Selected image preview before saving |

### 🔐 Authentication & Security

| Feature | Description |
|---------|-------------|
| **Admin-Only Access** | Sign-in restricted to the designated admin email (`admin@gmail.com`) |
| **Firebase Authentication** | Email/Password authentication via Firebase Auth |
| **Auto-Login** | Splash screen checks auth state and navigates directly to dashboard if already signed in |
| **Session Management** | Persistent login with sign-out from side navigation drawer |
| **Form Validation** | Email format validation and empty field checks on sign-in |

### 🎨 UI/UX

| Feature | Description |
|---------|-------------|
| **Splash Screen** | Full-screen immersive splash with app logo and animated circular progress indicator |
| **Dual Navigation** | Side drawer (Home, Advertising/Banners, Chat, Logout) + Bottom navigation (Home, Users, Analytics) |
| **Material Design 3** | MaterialToolbar, MaterialCardView, MaterialButton, SwitchMaterial, Chips, FAB |
| **Edge-to-Edge** | Modern edge-to-edge display with proper insets handling |
| **View Binding** | Type-safe view references with Android View Binding |

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                       │
│                                                                 │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐    │
│  │  Activities   │  │  Fragments   │  │     Adapters       │    │
│  │              │  │              │  │                    │    │
│  │ SplashAct.   │  │ HomeFragment │  │ AdminProductAdptr  │    │
│  │ SigninAct.   │  │ AddProduct   │  │ UserAdapter        │    │
│  │ MainActivity │  │ SingleProd.  │  │ InboxAdapter       │    │
│  │              │  │ Analytics    │  │ MessageAdapter     │    │
│  │              │  │ BannerMgmt   │  │ HomeCategory       │    │
│  │              │  │ PaymentMgmt  │  │ HomeProduct        │    │
│  │              │  │ UserMgmt     │  │                    │    │
│  │              │  │ Inbox        │  │                    │    │
│  │              │  │ AdminMsg     │  │                    │    │
│  │              │  │ Order        │  │                    │    │
│  │              │  │ Profile      │  │                    │    │
│  └──────────────┘  └──────────────┘  └────────────────────┘    │
│                                                                 │
│                     View Binding + Glide                        │
└────────────────────────────┬────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                         DATA LAYER                              │
│                                                                 │
│  ┌──────────────────────┐  ┌──────────────────────────┐        │
│  │    Data Models        │  │    Firebase Services      │        │
│  │   (Lombok POJOs)      │  │                          │        │
│  │                      │  │  ┌──────────────────┐    │        │
│  │  Product              │  │  │ Firebase Auth    │    │        │
│  │  Order (+ OrderItem,  │  │  │ (Admin Login)    │    │        │
│  │    Address, Attribute)│  │  └──────────────────┘    │        │
│  │  User                 │  │  ┌──────────────────┐    │        │
│  │  Message              │  │  │ Cloud Firestore  │    │        │
│  │  Banner               │  │  │ (Real-time DB)   │    │        │
│  │  Category             │  │  └──────────────────┘    │        │
│  │  CartItem             │  │  ┌──────────────────┐    │        │
│  │                      │  │  │ Firebase Storage │    │        │
│  │                      │  │  │ (Images)         │    │        │
│  └──────────────────────┘  │  └──────────────────┘    │        │
│                             │  ┌──────────────────┐    │        │
│                             │  │ Firebase Analytics│    │        │
│                             │  └──────────────────┘    │        │
│                             └──────────────────────────┘        │
└─────────────────────────────────────────────────────────────────┘
```

### Project Structure

```
com.codex.adminfoodcaf/
│
├── activity/                          # Android Activities
│   ├── SpalshActivity.java            # Splash screen with auth state check
│   ├── SigninActivity.java            # Admin-only email/password login
│   └── MainActivity.java             # Main container with dual navigation,
│                                        global search, and notification system
│
├── fragment/                          # UI Fragments
│   ├── HomeFragment.java              # Dashboard with stats + product list
│   ├── AddProductFragment.java        # New product creation form
│   ├── SingleProductFragment.java     # Product detail view & edit
│   ├── FragmentSingleProduct.java     # Product detail helper
│   ├── AnalyticsFragment.java         # Analytics dashboard + PDF reports
│   ├── BannerManagementFragment.java  # Banner CRUD with image upload
│   ├── PaymentManagementFragment.java # Pending payment processing
│   ├── UserManagementFragment.java    # User directory & account control
│   ├── InboxFragment.java             # Chat inbox (customer list)
│   ├── AdminMessageFragment.java      # 1:1 real-time chat with customers
│   ├── OrderFragment.java             # Order details view
│   └── ProfileFragment.java          # Admin profile view
│
├── adapter/                           # RecyclerView Adapters
│   ├── AdminProductAdapter.java       # Product list with search/filter
│   ├── UserAdapter.java               # User list with status management
│   ├── InboxAdapter.java              # Chat inbox user list
│   ├── MessageAdapter.java            # Chat message bubbles
│   ├── HomeCategoryAdapter.java       # Category chips on home
│   └── HomeProductAdapter.java        # Product cards on home
│
├── model/                             # Data Models (Lombok POJOs)
│   ├── Product.java                   # Product with attributes & images
│   ├── Order.java                     # Order with items, address, payment
│   ├── User.java                      # User profile data
│   ├── Message.java                   # Chat message
│   ├── Banner.java                    # Promotional banner
│   ├── Category.java                  # Food category
│   └── CartItem.java                  # Cart item with attributes
│
└── res/
    ├── layout/                        # 24 XML layouts
    │   ├── activity_main.xml          # Main container layout
    │   ├── activity_signin.xml        # Sign-in screen
    │   ├── activity_spalsh.xml        # Splash screen
    │   ├── fragment_home.xml          # Dashboard layout
    │   ├── fragment_add_product.xml   # Add product form
    │   ├── fragment_analytics.xml     # Analytics dashboard
    │   ├── fragment_banner_management.xml
    │   ├── fragment_payment_management.xml
    │   ├── fragment_uesr_management.xml
    │   ├── fragment_inbox.xml
    │   ├── fragment_admin_message.xml
    │   ├── dialog_user_profile.xml    # User profile popup
    │   └── item_*.xml                 # RecyclerView item layouts
    ├── menu/
    │   ├── bottom_nav_menu.xml        # Home | Users | Analytics
    │   └── side_nav_menu.xml          # Home | Advertising | Chat | Logout
    └── drawable/                      # 39 drawable resources (icons, bgs)
```

---

## 🛠 Tech Stack

### Core Platform

| Technology | Version | Purpose |
|------------|---------|---------|
| **Android SDK** | Compile SDK 36, Min SDK 24 | Native Android app development |
| **Java** | 11 | Primary programming language |
| **Gradle** | AGP 8.12.3 | Build system with version catalog |

### Firebase Services

| Service | Version | Purpose |
|---------|---------|---------|
| **Firebase BOM** | 34.10.0 | Dependency version management |
| **Firebase Authentication** | 24.0.1 | Admin email/password login |
| **Cloud Firestore** | 26.1.1 | Real-time NoSQL database |
| **Firebase Storage** | (BOM managed) | Product & banner image storage |
| **Firebase Analytics** | (BOM managed) | App usage analytics |

### AndroidX & Material

| Library | Version | Purpose |
|---------|---------|---------|
| **AppCompat** | 1.7.1 | Backward-compatible Android features |
| **Material Components** | 1.13.0 | Material Design 3 UI components |
| **ConstraintLayout** | 2.2.1 | Flexible responsive layouts |
| **Activity** | 1.13.0 | Activity APIs (Edge-to-Edge, Result APIs) |
| **LocalBroadcastManager** | 1.1.0 | Local broadcast communication |

### Third-Party Libraries

| Library | Version | Purpose |
|---------|---------|---------|
| **Glide** | 5.0.5 | Image loading, caching & transformations |
| **Lombok** | 1.18.42 | Reduce boilerplate (getters, setters, builders) |

### Testing

| Library | Version | Purpose |
|---------|---------|---------|
| **JUnit** | 4.13.2 | Unit testing |
| **AndroidX Test JUnit** | 1.3.0 | Android instrumented testing |
| **Espresso** | 3.7.0 | UI testing |

---

## 🗄 Firebase Structure

### Firestore Collections

```
├── products/                    # Food product catalog
│   └── {productId}
│       ├── productId: String
│       ├── categoryId: String
│       ├── foodTitle: String
│       ├── productPrice: Number
│       ├── foodRating: String
│       ├── foodTime: String
│       ├── foodDetail: String
│       ├── ingrideint: String
│       ├── availability: Boolean
│       ├── productImage: Array<String>  (URLs)
│       └── attribute: Array<Object>
│           ├── porsion: String
│           ├── type: String
│           ├── values: Array<String>
│           └── Price: Array<String>
│
├── orders/                      # Customer orders
│   └── {orderId}
│       ├── orderId: String
│       ├── userId: String
│       ├── orderDate: String
│       ├── status: String       ("Pending" | "Paid" | "Delivered")
│       ├── paymentMethod: String
│       ├── DeliveryAddress: Object
│       │   ├── name: String
│       │   ├── email: String
│       │   ├── address: String
│       │   └── contactNum: String
│       └── orderItems: Array<Object>
│           ├── productId: String
│           ├── productName: String
│           ├── unitPrice: Number
│           ├── qty: Number
│           ├── totalPrice: Number
│           └── attributes: Array<Object>
│
├── users/                       # Registered customer accounts
│   └── {userId}
│       ├── uId: String
│       ├── name: String
│       ├── email: String
│       ├── address: String
│       ├── mobileNum: String
│       ├── profilePicUrl: String
│       └── status: Boolean      (true = Active, false = Suspended)
│
├── categories/                  # Food categories
│   └── {categoryId}
│       ├── categoryId: String
│       ├── categoryName: String
│       ├── categoryImage: String
│       └── categorySubtitle: String
│
├── chats/                       # Customer-Admin messaging
│   └── {customerId}/
│       └── messages/
│           └── {messageId}
│               ├── messageId: String
│               ├── senderId: String
│               ├── receiverId: String
│               ├── messageText: String
│               └── timestamp: Number
│
└── banner/                      # Promotional banners
    └── {bannerId}
        ├── banner_title: String
        ├── banner_body: String
        ├── banner_date: String
        └── banner_url: String   (Firebase Storage URL)
```

### Firebase Storage Structure

```
gs://foodcaf-82dbc.firebasestorage.app/
├── product-images/
│   └── {productId}/
│       └── {uuid}.jpg           # Product images (up to 2 per product)
└── banner_images/
    └── banner_{uuid}.jpg        # Promotional banner images
```

---

## 📸 Screenshots

> *Screenshots coming soon — run the app to see the live UI!*

| Dashboard | Analytics | User Management |
|-----------|-----------|-----------------|
| Live stats, product list, global search | Revenue, top products, PDF reports | User directory, status control |

| Chat Inbox | Payment Management | Banner Management |
|------------|-------------------|-------------------|
| Customer chat list | Pending payments, mark as paid | Banner CRUD with image upload |

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Ladybug (2024+) or newer
- **JDK 11** or higher
- **Android SDK** with API 36 installed
- A **Firebase project** with Firestore, Auth, Storage, and Analytics enabled

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/Dilshan-DevX/Food-CAF-ADMIN.git
   cd Food-CAF-ADMIN
   ```

2. **Firebase Setup**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create a new project or use your existing Food CaF project
   - Add an Android app with package name: `com.codex.adminfoodcaf`
   - Download `google-services.json` and place it in the `app/` directory
   - Enable **Authentication** → Email/Password sign-in method
   - Enable **Cloud Firestore** in production mode
   - Enable **Firebase Storage**
   - Create the admin account in Firebase Auth with email `admin@gmail.com`

3. **Open in Android Studio**
   ```
   File → Open → Select the project directory
   ```

4. **Sync Gradle & Build**
   ```bash
   ./gradlew assembleDebug
   ```

5. **Run on device/emulator**
   - Connect an Android device (API 24+) or start an emulator
   - Click **Run ▶** in Android Studio

### Admin Login

| Field | Value |
|-------|-------|
| Email | `admin@gmail.com` |
| Password | *(set during Firebase Auth account creation)* |

> ⚠️ Only the `admin@gmail.com` account can access the admin panel. All other accounts are rejected with "Access Denied."

---

## 🔗 Related Projects

| Project | Description |
|---------|-------------|
| [**Food CaF (Customer App)**](https://github.com/Dilshan-DevX/Food-CAF) | Customer-facing food ordering app that shares the same Firebase backend |

---

## 📄 License

This project is proprietary software developed by **Code X Pvt Ltd**.

---

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

**Made with ❤️ by [Code X Pvt Ltd](https://github.com/Dilshan-DevX)**

*Powering the Food CaF ecosystem*
