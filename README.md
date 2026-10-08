# MSS301 — FA26 — Phạm Thị Vân Thanh

> Bài tập môn **MSS301** (Microservices) — Fall 2026.

## 👩‍💻 Thông tin sinh viên

| | |
|---|---|
| **Họ và tên** | Phạm Thị Vân Thanh |
| **MSSV** | SE192641 |
| **Email** | vaanthanh2005@gmail.com |
| **GitHub** | [@vtmoazz](https://github.com/vtmoazz) |

## 📂 Nội dung repository

| Slot | Nội dung | Mô tả |
|------|----------|-------|
| [`Slot4`](./Slot4/product-service) | Product Service | REST API CRUD sản phẩm với Spring Boot + MongoDB |
| [`Slot9`](./Slot9) | Microservices Part 1 → 5 | Product / Order / Inventory + API Gateway (Keycloak JWT) + Swagger (Springdoc OpenAPI) |

## 🚀 Slot4 — Product Service

Microservice quản lý sản phẩm, xây dựng bằng **Spring Boot** và **MongoDB**.

### Công nghệ sử dụng

- Java + Spring Boot (Spring Web, Spring Data MongoDB)
- Lombok
- MongoDB (Docker Compose)
- Testcontainers (integration test)

### API Endpoints

| Method | Endpoint | Mô tả | Status |
|--------|----------|-------|--------|
| `POST` | `/api/products` | Tạo sản phẩm mới | `201 Created` |
| `GET` | `/api/products` | Lấy danh sách sản phẩm | `200 OK` |
| `PUT` | `/api/products/{id}` | Cập nhật sản phẩm | `200 OK` |
| `DELETE` | `/api/products/{id}` | Xoá sản phẩm | `204 No Content` |

> Khi không tìm thấy sản phẩm → trả về `404 Not Found` (xử lý bởi `GlobalExceptionHandler`).

### Cách chạy

```bash
cd Slot4/product-service

# Khởi động MongoDB
docker compose up -d

# Chạy ứng dụng
./mvnw spring-boot:run
```

Ứng dụng chạy tại `http://localhost:8080`.

### Chạy test

```bash
cd Slot4/product-service
./mvnw test
```

## 🚀 Slot9 — API Documentation với Swagger

Hệ thống Part 1–4 (Product, Order, Inventory, API Gateway + Keycloak) bổ sung **Springdoc OpenAPI / Swagger UI** (Part 5).

```bash
cd Slot9/order-service && docker compose up -d     # MySQL cho order + inventory
cd Slot9/product-service && docker compose up -d   # MongoDB
cd Slot9/api-gateway && docker compose up -d       # Keycloak :8181
# Chạy từng service: ./mvnw spring-boot:run (inventory 8082, product 8080, order 8081, gateway 9000)
```

| URL | Kết quả |
|-----|---------|
| `http://localhost:9000/swagger-ui.html` | Swagger UI tổng hợp (dropdown 3 service), không cần JWT |
| `http://localhost:8080/swagger-ui.html` · `:8081` · `:8082` | Swagger UI của từng service |
| `http://localhost:{port}/api-docs` | OpenAPI JSON của từng service |
| `http://localhost:9000/api/products` không có JWT | `401 Unauthorized` |

