# 🍽 VES Canteen — College Canteen Ordering App

A modern Android application for the VES (Vivekanand Education Society) college canteen, built with **Kotlin**, **Firebase**, and **Material Design**. Students can browse the menu, add items to cart, and pay via UPI — while admins manage orders in real-time.

---

## 📱 Features

### Student Side
- **Browse Menu** — Grid layout with categorized items (Breakfast, Beverages, For You)
- **Search** — Instant search across all menu items
- **Quantity Stepper** — Add/remove items with a sleek `- 1 +` control
- **Cart Management** — Review items, adjust quantities, see total
- **UPI Payment** — Pay via GPay, PhonePe, Paytm using native Android intents
- **Cash at Counter** — Alternative payment option
- **Order Confirmation** — Token number, order summary, payment method
- **Push Notifications** — Order status updates
- **Time-based Greetings** — Dynamic greeting based on time of day

### Admin Panel
- **Dashboard** — Total orders, pending count, today's revenue, menu item count
- **Order Management** — View all orders with status workflow:
  ```
  Pending → Preparing → Ready → Completed
  ```
- **Status Filters** — Filter orders by status (All / Pending / Preparing / Ready)
- **Menu Management** — Add, delete, and reset menu items
- **Real-time Updates** — Firestore snapshot listeners for live order tracking

### Authentication
- **Student Login** — Only `@ves.ac.in` emails allowed
- **Admin Login** — `admin@vescanteen.com` with auto-account creation
- **Session Persistence** — Stay logged in across app restarts

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin |
| **UI** | XML Layouts, Material Design Components |
| **Auth** | Firebase Authentication |
| **Database** | Cloud Firestore |
| **Images** | Glide |
| **Payments** | UPI Intent (Native Android) |
| **Architecture** | Single Activity + Fragments |

---

## 📁 Project Structure

```
app/src/main/java/com/example/vescanteen/
├── LoginActivity.kt              # Email/password auth with domain validation
├── SignupActivity.kt             # Student registration (@ves.ac.in only)
├── MainActivity.kt               # Student shell (Home, Search, Cart, Profile)
├── HomeFragment.kt               # Menu grid with category filters
├── SearchFragment.kt             # Search menu items
├── CartFragment.kt               # Cart with checkout
├── ProfileFragment.kt            # User profile & order history
├── PaymentActivity.kt            # UPI + Cash payment options
├── OrderConfirmationActivity.kt  # Order summary with token
├── AdminActivity.kt              # Admin shell (Dashboard, Orders, Menu)
├── AdminDashboardFragment.kt     # Stats cards + recent orders
├── AdminOrdersFragment.kt        # Order list with status workflow
├── AdminMenuFragment.kt          # CRUD menu items
├── CartManager.kt                # Singleton cart state
├── NotificationHelper.kt         # Push notification helper
├── adapter/
│   ├── MenuAdapter.kt            # Menu grid cards
│   ├── CartAdapter.kt            # Cart item list
│   ├── AdminOrderAdapter.kt      # Admin order cards with actions
│   └── AdminMenuAdapter.kt       # Admin menu item list
└── model/
    ├── MenuItem.kt               # Menu item data class
    ├── CartItem.kt               # Cart item wrapper
    ├── Order.kt                  # Order data class
    └── User.kt                   # User data class
```

---

## 🔥 Firestore Collections

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

### Default Credentials (Demo Only)

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@vescanteen.com` | `admin123` |
| Student | Any `@ves.ac.in` email | Your password |

> ⚠️ For production, remove hardcoded credentials from `LoginActivity.kt` and use Firestore role-based access instead.

---

## 💳 Payment Configuration

The UPI payment ID is configured in `PaymentActivity.kt`:

```kotlin
const val CANTEEN_UPI_ID = "your-upi-id@bank"
```

> ⚠️ UPI payments require a **real device** with a UPI app installed. Emulators will show "No UPI app found".

---

## 📱 App Screens

| Screen | Description |
|--------|------------|
| **Login** | Email/password authentication with `@ves.ac.in` domain restriction |
| **Signup** | Student registration with username, email, phone, and password |
| **Home** | Menu grid with food images, category chips (For You / Breakfast / Beverages), and quantity stepper |
| **Search** | Instant search across all menu items |
| **Cart** | Cart items with quantity controls, total price, and checkout button |
| **Payment** | Choose between UPI (GPay, PhonePe, Paytm) or Cash at Counter |
| **Order Confirmation** | Token number, order summary, timestamp, and payment method |
| **Admin Dashboard** | Real-time stats — total orders, pending count, revenue today, menu items |
| **Admin Orders** | Order list with status workflow (Pending → Preparing → Ready → Completed) |
| **Admin Menu** | Add, delete, and reset menu items synced to student side |

---

## 📝 About

VES Canteen is a digital ordering solution for the college canteen at **VES Institute of Technology (VESIT), Mumbai**. It replaces the manual ordering process with a streamlined mobile experience — students browse the menu, add items, and pay via UPI or cash, while canteen staff manage incoming orders in real-time through the admin panel. Built with Kotlin, Firebase, and Material Design as part of the **Mobile Application Development** course.

---

## 👨‍💻 Team

- **Vedish Chawla**
- **Dolly Balwani**

VES Institute of Technology, Mumbai

---

## 📄 License

This project is built for academic purposes as part of the Mobile Application Development course at VESIT.
