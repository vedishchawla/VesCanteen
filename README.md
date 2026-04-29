# 🍽 VES Canteen — College Canteen Ordering App

A modern Android application for college canteen ordering, built with **Kotlin**, **Firebase**, and **Material Design**. Students can browse the menu, search for items, manage their cart with gestures, pay via UPI, and receive real-time order status notifications — while admins manage orders and menu items from a dedicated dashboard.

---

## 📱 Features

### Student Side
- **Browse Menu** — 2-column grid layout with category filtering (chips)
- **Real-time Search** — Instant search-as-you-type using TextWatcher
- **Smart Cart** — Quantity stepper (`- 1 +`), swipe-to-delete, shake-to-clear
- **Nutrition Info** — Long-press any item to fetch calories via REST API
- **UPI Payment** — Pay via GPay, PhonePe, Paytm using implicit intents
- **Cash at Counter** — Alternative payment option with preference memory
- **Order Tracking** — Background service polls for status changes + push notifications
- **GPS Canteen Finder** — Distance calculation using FusedLocationProvider
- **Offline Order History** — Past orders stored locally in Room Database
- **Remember Me** — Email persistence via SharedPreferences
- **Time-based Greetings** — Dynamic greeting using Calendar (Morning/Afternoon/Evening)

### Admin Panel
- **Live Dashboard** — Real-time stats (orders, revenue, pending count) via Firestore snapshot listeners
- **Order Management** — Status workflow: `Confirmed → Preparing → Ready → Completed`
- **Status Filters** — Chip-based filtering (All / Pending / Preparing / Ready)
- **Menu CRUD** — Add, delete, and reset menu items synced to student side

### Authentication
- **Domain-restricted Login** — Only `@ves.ac.in` emails allowed
- **Admin Auto-provisioning** — Admin account auto-created on first login
- **Session Persistence** — Firebase JWT token keeps user logged in across restarts

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin |
| **UI** | XML Layouts, Material Design 3 |
| **Auth** | Firebase Authentication |
| **Cloud DB** | Cloud Firestore (real-time sync) |
| **Local DB** | Room (SQLite ORM) |
| **Preferences** | SharedPreferences (key-value) |
| **REST API** | HttpURLConnection + CalorieNinjas API |
| **Images** | Glide (caching + transformations) |
| **Payments** | UPI Deep Linking (implicit intents) |
| **Sensors** | Accelerometer (shake), FusedLocationProvider (GPS) |
| **Background** | Android Service (`START_STICKY` polling) |
| **Notifications** | NotificationCompat + NotificationChannel |
| **Architecture** | Single Activity + Fragments (BottomNavigationView) |

---

## 📁 Project Structure

```
app/src/main/java/com/example/vescanteen/
├── LoginActivity.kt              # Email/password auth with domain validation
├── SignupActivity.kt             # Student registration
├── MainActivity.kt               # Student shell — 4 fragments via BottomNav
├── HomeFragment.kt               # Menu grid, category chips, greeting
├── SearchFragment.kt             # Real-time search with TextWatcher
├── CartFragment.kt               # Swipe-to-delete, shake-to-clear, checkout
├── ProfileFragment.kt            # User info, GPS distance, order history
├── PaymentActivity.kt            # UPI + Cash payment with saved preference
├── OrderConfirmationActivity.kt  # Token, summary, Firestore + Room save
├── AdminActivity.kt              # Admin shell — 3 fragments via BottomNav
├── AdminDashboardFragment.kt     # Real-time stats with snapshot listener
├── AdminOrdersFragment.kt        # Order list with status workflow
├── AdminMenuFragment.kt          # Menu CRUD
├── CartManager.kt                # Singleton cart with JSON persistence
├── ShakeDetector.kt              # Accelerometer gesture detection
├── NutritionApiHelper.kt         # REST API client with fallback data
├── OrderStatusService.kt         # Background polling service
├── NotificationHelper.kt         # Notification channel + builder
├── adapter/
│   ├── MenuAdapter.kt            # Menu grid cards + long-press handler
│   ├── CartAdapter.kt            # Cart item rows
│   ├── AdminOrderAdapter.kt      # Admin order cards with actions
│   └── AdminMenuAdapter.kt       # Admin menu item list
├── model/
│   ├── MenuItem.kt               # Food item data class
│   ├── CartItem.kt               # Cart item wrapper (MenuItem + quantity)
│   ├── Order.kt                  # Order data class
│   └── User.kt                   # User profile data class
└── database/
    ├── OrderHistoryEntity.kt     # Room Entity — order table schema
    ├── OrderHistoryDao.kt        # Room DAO — SQL query definitions
    └── OrderHistoryDatabase.kt   # Room Database — singleton connection
```

---

## 💾 Data Storage

### Cloud — Firestore
```
├── users/{uid}
│   ├── username, email, phone, role
│
├── menuItems/{itemId}
│   ├── name, price, category, description
│   ├── imageUrl, drawableResName, isAvailable
│
└── orders/{orderId}
    ├── userId, tokenNumber, status
    ├── items[], totalPrice, paymentMethod
    └── timestamp
```

### Local — Room Database (SQLite)
```
order_history table
├── id (auto-increment PK)
├── tokenNumber, items, totalPrice
├── paymentMethod, status, timestamp
```

### Local — SharedPreferences
```
ves_canteen_prefs.xml → remember_email, saved_email, last_payment_method
ves_canteen_cart.xml  → cart_items (JSON array string)
```

---

## 📊 Architecture

```
LoginActivity ──→ MainActivity (student)
     │                 ├── HomeFragment
     │                 ├── SearchFragment
     │                 ├── CartFragment ──→ PaymentActivity ──→ OrderConfirmationActivity
     │                 └── ProfileFragment
     │
     └────────→ AdminActivity (admin)
                   ├── AdminDashboardFragment
                   ├── AdminOrdersFragment
                   └── AdminMenuFragment

Background: OrderStatusService (polls Firestore every 30s → sends notifications)
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 8+
- Firebase project with Authentication and Firestore enabled

### Setup

1. **Clone the repo**
   ```bash
   git clone https://github.com/vedishchawla/VesCanteen.git
   cd VesCanteen
   ```

2. **Add Firebase config**
   - Go to [Firebase Console](https://console.firebase.google.com)
   - Download `google-services.json` and place it in `app/`

3. **Create Firestore Database**
   - Firebase Console → Firestore → Create Database → **Test Mode**
   - Location: `asia-south1` (Mumbai)

4. **Enable Email/Password Auth**
   - Firebase Console → Authentication → Sign-in method → Email/Password → Enable

5. **Build & Run**
   - Open in Android Studio → Sync Gradle → Run on device/emulator

### Default Credentials

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@vescanteen.com` | `admin123` |
| Student | Any `@ves.ac.in` email | Your password |

> ⚠️ For production, remove hardcoded credentials and use Firestore role-based access.

---

## 💳 Payment Configuration

The UPI payment ID is configured in `PaymentActivity.kt`:

```kotlin
const val CANTEEN_UPI_ID = "your-upi-id@bank"
```

> ⚠️ UPI payments require a **physical device** with a UPI app installed.

---

## 📱 Screens

| Screen | Description |
|--------|------------|
| **Login** | Email/password auth with domain restriction + Remember Me |
| **Signup** | Registration with username, email, phone, password |
| **Home** | Menu grid, category chips, quantity stepper, cart badge |
| **Search** | Real-time search with substring matching |
| **Cart** | Swipe-to-delete, shake-to-clear, checkout |
| **Payment** | UPI / Cash selection with saved preference |
| **Order Confirmation** | Token number, summary, notification trigger |
| **Profile** | GPS canteen finder, offline order history, logout |
| **Admin Dashboard** | Live stats — orders, revenue, pending count |
| **Admin Orders** | Status workflow with filter chips |
| **Admin Menu** | Add, delete, and reset menu items |

---

## 👨‍💻 Authors

- **Vedish Chawla**
- **Dolly Balwani**

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
