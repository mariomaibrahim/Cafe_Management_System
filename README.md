<div align="center">

# ☕ Cafe Management System

**Modern JavaFX Desktop Application for Complete Cafe Operations**

![Java](https://img.shields.io/badge/Java-11+-032A33?style=flat-square) ![JavaFX](https://img.shields.io/badge/JavaFX-17+-0B4B54?style=flat-square) ![MySQL](https://img.shields.io/badge/MySQL-8.0+-2A777C?style=flat-square)

[About](#-about) • [Features](#-features) • [Tech Stack](#-tech-stack) • [Installation](#-installation) • [Screenshots](#-screenshots) • [Team](#-team)

---

</div>

## 📖 About

A comprehensive desktop application designed to streamline cafe operations with an elegant, professional interface. Built with JavaFX and MySQL, offering complete solutions for inventory management, order processing, billing, and staff coordination.

### Key Highlights

- 🎨 Beautiful teal-themed UI with intuitive design
- ⚡ Fast and responsive performance  
- 🔒 Secure role-based access control
- 📊 Real-time analytics and reporting
- 💾 Reliable MySQL database backend

---

## ✨ Features

### 🔐 User Management
Secure authentication • Role-based access (Admin/Staff/Manager) • Staff activity tracking

### 📦 Inventory Control  
Real-time stock tracking • Low stock alerts • Automatic reorder suggestions • Product categorization

### 🛒 Order Processing
Quick order placement • Table management • Kitchen integration • Order history & modifications

### 💰 Billing System
Multiple payment methods • Invoice generation • Receipt printing • Transaction history

### 📊 Analytics Dashboard
Revenue tracking • Sales analytics • Staff performance metrics • Custom report generation

---
## 📸 Screenshots

<div align="center">

### 🔑 Login Screen
<img src="/Screens/login.png" width="100%" alt="Login Screen"/>


### 🛒 Order Management
<img src="/Screens/menu.png" width="100%" alt="Order Management"/>

### 📦 Inventory Management
<img src="/Screens/inventory.png" width="100%" alt="Inventory Management"/>

### 📊 Dashboard
<img src="/Screens/dashboard.png" width="100%" alt="Dashboard"/>

</div>


---

## 🛠 Tech Stack

<table>
<tr>
<td width="50%">

**Backend**
-  Java 11+ - Core application logic
-  MySQL 8.0+ - Database management
-  HikariCP 5.0.1 - Connection pooling
-  MySQL Connector 8.0.33 - JDBC driver

</td>
<td width="50%">

**Frontend**
-  JavaFX 17+ - Rich desktop UI
-  Ikonli 12.3.1 - Icon library
-  Custom CSS - Teal theme design

</td>
</tr>
</table>

### 🎨 Color Palette

| Deep Teal | Ocean Teal | Turquoise | Light Teal | Pale Mint |
|:---------:|:----------:|:---------:|:----------:|:---------:|
| `#032A33` | `#0B4B54`  | `#2A777C` | `#82ACAB`  | `#D3E4E7` |

---

## 🚀 Installation

### Prerequisites
- Java Development Kit (JDK) 11 or higher
- MySQL Server 8.0 or higher  

### Setup Steps

**1. Clone the Repository**
```bash
git clone https://github.com/mariomaibrahim/cafe-management-system.git
cd cafe-management-system
```

**2. Setup Database**
```bash
mysql -u root -p -e "CREATE DATABASE cafe_management;"
mysql -u root -p cafe_management < database/schema.sql
```



---

## 👥 Team

<div align="center">

### AITP Development Team

<table>
  <tr>
    <td align="center" width="50%">
      <br>
      <b>Mariam Ibrahim</b><br>
      <sub>Co-Lead Developer</sub><br><br>
      💻 Frontend & Backend Development
    </td>
    <td align="center" width="50%">
      <br>
      <b>Mariam Eid</b><br>
      <sub>Co-Lead Developer</sub><br><br>
      💻 Frontend & Backend Development
    </td>
  </tr>
</table>

</div>

---

## 📁 Project Structure

```
cafe-management-system/
├── src/
│   ├── main/
│   │   ├── java/              # Java source files
│   │   │   ├── controllers/   # JavaFX controllers
│   │   │   ├── models/        # Data models
│   │   │   ├── services/      # Business logic
│   │   │   └── utils/         # Utility classes
│   │   └── resources/
│   │       ├── fxml/          # JavaFX layouts
│   │       ├── css/           # Stylesheets
│   │       └── images/        # Image assets
│   └── test/                  # Unit tests
├── database/
│   └── schema.sql             # Database schema
├── screenshots/               # Application screenshots
├── pom.xml                    # Maven configuration
└── README.md                  # This file
```

---

<div align="center">

### ⭐ Support This Project

If you find this project helpful, please give it a star on GitHub!

**[⭐ Star on GitHub](https://github.com/mariomaibrahim/cafe-management-system)**

---

Made with ❤️ and ☕ by **Team AITP**

*Mariam Ibrahim & Mariam Eid*

</div>
