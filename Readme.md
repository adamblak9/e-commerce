# ShopEase — E-Commerce Platform

A full-stack Java EE e-commerce application running on Tomcat + SQL Server via Docker.

---

## 🚀 Quick Start

```bash
docker-compose up --build
```

Visit → http://localhost:8080/login.html

---

## 🏗️ Architecture

- **Backend** — Java EE (Servlets + JPA/Hibernate), deployed as `ROOT.war` on Tomcat
- **Frontend** — Vanilla HTML/CSS/JS
- **Database** — Microsoft SQL Server 2022
- **Auth** — JWT (Bearer token, stored in `localStorage`)
- **Orchestration** — Docker Compose (Tomcat on port `8080`, SQL Server on port `1433`)

---

## 👤 Creating the First Admin Account

Since there is no admin by default, follow these steps after the app is running:

### Step 1 — Sign up normally

Go to http://localhost:8080/signup.html and create your account.

### Step 2 — Find your SQL Server container

```bash
docker ps
```

Look for `commerce-sqlserver-1` in the list.

### Step 3 — Promote your account to ADMIN

Run this single command (replace the email):

```bash
docker exec -it commerce-sqlserver-1 /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P "Admin123!" \
  -Q "USE ecommerce_db; UPDATE users SET role='ADMIN' WHERE email='adamarbai170@gmail.com';"
```
one ligne bash admin
docker exec -it commerce-sqlserver-1 /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Admin123!" -C -Q "USE ecommerce_db; UPDATE users SET role='ADMIN' WHERE email='adamarbai170@gmail.com';"

print users
docker exec -it commerce-sqlserver-1 /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Admin123!" -C -Q "USE ecommerce_db; SELECT email, role FROM users;"
> If that path doesn't work, try `/opt/mssql-tools18/bin/sqlcmd` and add `-C` at the end.

### Step 4 — Log in and open the Admin Dashboard

Go to → http://localhost:8080/admin.html

---

## 🛡️ User Roles

| Role | Access |
|------|--------|
| `CUSTOMER` | Browse store, cart, orders, profile |
| `SELLER` | All of the above + manage own products |
| `ADMIN` | Full admin dashboard (users, categories, products, orders) |

---

## 📋 Admin Dashboard Features

| Section | What you can do |
|---------|-----------------|
| **Dashboard** | Stats overview — users, revenue, orders, products |
| **Users** | List, search, filter, change role/status, reset password, deactivate |
| **Categories** | Add, edit, delete product categories with icon & description |
| **Products** | Browse all products, deactivate any listing |
| **Orders** | View all orders, update order status |

---

## 🔌 API Reference

### Auth
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/auth/signup` | Public |
| POST | `/api/auth/login` | Public |

### Products
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/api/products` | Public |
| GET | `/api/products/{id}` | Public |
| POST | `/api/products` | SELLER, ADMIN |
| PUT | `/api/products/{id}` | Owner SELLER, ADMIN |
| DELETE | `/api/products/{id}` | Owner SELLER, ADMIN |

### Categories
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/api/categories` | Public |
| GET | `/api/categories/active` | Public |
| GET | `/api/categories/{id}` | Public |
| POST | `/api/categories` | ADMIN |
| PUT | `/api/categories/{id}` | ADMIN |
| DELETE | `/api/categories/{id}` | ADMIN |

### Admin
| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/api/admin/stats` | ADMIN |
| GET | `/api/admin/users` | ADMIN |
| GET | `/api/admin/users/{id}` | ADMIN |
| PUT | `/api/admin/users/{id}/role` | ADMIN |
| PUT | `/api/admin/users/{id}/status` | ADMIN |
| PUT | `/api/admin/users/{id}/reset-password` | ADMIN |
| DELETE | `/api/admin/users/{id}` | ADMIN |

### Orders & Cart
| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/orders` | AUTH |
| GET | `/api/orders/customer` | AUTH |
| GET | `/api/orders/{id}` | AUTH |
| POST | `/api/cart` | AUTH |
| GET | `/api/cart` | AUTH |

---

## 🐳 Docker Details

```yaml
Services:
  app   → Tomcat 10, port 8080
  db    → SQL Server 2022, port 1433
```

Rebuild after code changes:
```bash
docker-compose up --build
```

Stop everything:
```bash
docker-compose down
```

Wipe database and restart fresh:
```bash
docker-compose down -v
docker-compose up --build
```

---

## 📁 Project Structure

```
src/main/
├── java/com/ecommerce/
│   ├── entity/          # JPA entities (User, Product, Order, Category…)
│   ├── repository/      # Data access layer
│   ├── service/         # Business logic
│   ├── servlet/         # REST API endpoints
│   ├── filter/          # JWT auth filter, role guard
│   └── util/            # JWT, password, JPA utilities
├── webapp/
│   ├── admin.html       # Admin dashboard
│   ├── store.html       # Customer storefront
│   ├── auth.js          # Client-side auth guard
│   └── …               # Other pages
└── resources/
    └── META-INF/
        └── persistence.xml
```
Docker Migration: Complete containerization of ShopEase platform

🔄 Architecture Changes:
- Application now runs entirely in Docker containers
- Tomcat (port 8080) + SQL Server (port 1433) orchestrated via docker-compose
- WAR deployed as ROOT.war (no /ecommerce context path)

🔧 Code Updates:
- Frontend: Changed all fetch() URLs from 'http://localhost:8080/ecommerce/api/*' to '/api/*'
- Backend: persistence.xml JDBC URL updated from 'localhost' to 'commerce-sqlserver-1'
- JwtFilter: Public routes properly configured (/api/auth/login, /api/auth/signup)

📦 New Files:
- Dockerfile: Multi-stage build for Tomcat deployment ..
- docker-compose.yml: Service orchestration with automatic networking

✅ Benefits:
- No local Tomcat or SQL Server installation required
- One command setup: docker-compose up --build
- Consistent environment across all team members
- Easy onboarding for new developers

🚀 Quick Start:
1. docker-compose up --build
2. Visit http://localhost:8080/login.html
