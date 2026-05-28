# ShopEase - E-Commerce Platform

ShopEase is a Java Servlet e-commerce application with a vanilla HTML/CSS/JS frontend, JWT authentication, and SQL Server storage. The project is containerized with Docker Compose so it can run without a local Tomcat or SQL Server installation.

## Quick Start

From the project root:

```powershell
docker compose up -d --build
```

Open:

```text
http://localhost:8080/login.html
```

If your Docker installation uses the older command, replace `docker compose` with `docker-compose`.

## Demo Data Setup

Use this if you want a clean database with ready-to-test users, sellers, products, orders, ratings, and comments.

The seed file is:

```text
src/main/resources/seed-data.sql
```

### 1. Delete the old database volume

Warning: this deletes the current SQL Server data for this Docker Compose project.

```powershell
docker compose down -v
```

### 2. Start SQL Server

```powershell
docker compose up -d sqlserver
```

Check that the SQL Server container is healthy:

```powershell
docker compose ps
```

### 3. Create the empty database

```powershell
docker compose exec sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Admin123!" -C -Q "IF DB_ID('ecommerce_db') IS NULL CREATE DATABASE ecommerce_db;"
```

### 4. Start the app so Hibernate creates the tables

```powershell
docker compose up -d --build app
```

Wait about 20-40 seconds. You can check the logs with:

```powershell
docker compose logs app
```

Before seeding, verify that Hibernate created the tables:

```powershell
docker compose exec sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Admin123!" -C -d ecommerce_db -Q "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE='BASE TABLE' ORDER BY TABLE_NAME;"
```

You should see tables like `users`, `products`, `orders`, `order_items`, `reviews`, and `categories`. If `reviews` is missing, wait a little more and check `docker compose logs app`.

### 5. Copy the seed file into SQL Server

```powershell
docker compose cp .\src\main\resources\seed-data.sql sqlserver:/tmp/seed-data.sql
```

### 6. Run the seed file

```powershell
docker compose exec sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Admin123!" -C -d ecommerce_db -i /tmp/seed-data.sql
```

### 7. Verify the seed

List users:

```powershell
docker compose exec sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Admin123!" -C -d ecommerce_db -Q "SELECT email, role FROM users;"
```

Count products:

```powershell
docker compose exec sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Admin123!" -C -d ecommerce_db -Q "SELECT COUNT(*) AS products_count FROM products;"
```

## Demo Accounts

| Role | Email | Password |
|------|-------|----------|
| ADMIN | `admin@gmail.com` | `admin123` |
| CUSTOMER | `amina.customer@gmail.com` | `customer123` |
| CUSTOMER | `youssef.customer@gmail.com` | `customer123` |
| CUSTOMER | `lina.customer@gmail.com` | `customer123` |
| SELLER | `nora.seller@gmail.com` | `seller123` |
| SELLER | `samir.seller@gmail.com` | `seller123` |
| SELLER | `mira.seller@gmail.com` | `seller123` |

## Common Commands

Start or rebuild everything:

```powershell
docker compose up -d --build
```

Rebuild only the web app after HTML/CSS/Java changes:

```powershell
docker compose up -d --build app
```

Stop containers but keep the database volume:

```powershell
docker compose down
```

Stop containers and delete the database volume:

```powershell
docker compose down -v
```

View app logs:

```powershell
docker compose logs app
```

View SQL Server logs:

```powershell
docker compose logs sqlserver
```

## Application URLs

| Page | URL |
|------|-----|
| Home | `http://localhost:8080/index.html` |
| Login | `http://localhost:8080/login.html` |
| Sign up | `http://localhost:8080/signup.html` |
| Store | `http://localhost:8080/store.html` |
| Cart | `http://localhost:8080/cart.html` |
| Customer orders | `http://localhost:8080/Orders.html` |
| Profile | `http://localhost:8080/profile.html` |
| Seller dashboard | `http://localhost:8080/products.html` |
| Admin dashboard | `http://localhost:8080/admin.html` |

## User Roles

| Role | Access |
|------|--------|
| CUSTOMER | Browse the store, use cart, place orders, manage profile |
| SELLER | Customer access plus seller product and order tools |
| ADMIN | Full admin dashboard for users, categories, products, and orders |

## API Summary

All API routes are served from:

```text
http://localhost:8080/api
```

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
| PUT | `/api/products/{id}` | Product owner SELLER, ADMIN |
| DELETE | `/api/products/{id}` | Product owner SELLER, ADMIN |

### Categories

| Method | Endpoint | Access |
|--------|----------|--------|
| GET | `/api/categories` | Public |
| GET | `/api/categories/active` | Public |
| GET | `/api/categories/{id}` | Public |
| POST | `/api/categories` | ADMIN |
| PUT | `/api/categories/{id}` | ADMIN |
| DELETE | `/api/categories/{id}` | ADMIN |

### Orders and Cart

| Method | Endpoint | Access |
|--------|----------|--------|
| POST | `/api/orders` | Logged in user |
| GET | `/api/orders/customer` | Logged in user |
| GET | `/api/orders/{id}` | Logged in user |
| POST | `/api/cart` | Logged in user |
| GET | `/api/cart` | Logged in user |

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

## Architecture

| Layer | Technology |
|-------|------------|
| Frontend | Vanilla HTML, CSS, JavaScript |
| Backend | Java Servlets, JPA/Hibernate |
| Database | Microsoft SQL Server 2022 |
| Auth | JWT stored in `localStorage` |
| Runtime | Tomcat 10 in Docker |
| Orchestration | Docker Compose |

Docker services:

| Service | Purpose | Port |
|---------|---------|------|
| `app` | Tomcat web application | `8080` |
| `sqlserver` | SQL Server database | internal, exposed inside Docker network |

## Project Structure

```text
src/main/
  java/com/ecommerce/
    entity/       JPA entities
    repository/   Data access
    service/      Business logic
    servlet/      REST endpoints
    filter/       JWT and role guards
    util/         JWT, password, JPA helpers
  resources/
    META-INF/persistence.xml
    seed-data.sql
  webapp/
    index.html
    store.html
    product.html
    products.html
    admin.html
    cart.html
    Orders.html
    profile.html
    login.html
    signup.html
```

## Notes

- The WAR is deployed as the root application, so pages are opened directly from `http://localhost:8080/...`.
- Frontend API calls use relative paths like `/api/products`.
- The database name is `ecommerce_db`.
- The SQL Server `sa` password in Docker Compose is `Admin123!`.
