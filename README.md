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
| [`Slot18`](./Slot18/Assignment2/WatchManagement) | Assignment 2 — Watch Management | 4 microservice + SQL Server, Saga Orchestration với bù trừ LIFO, JWT qua API Gateway, Docker Compose |

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

## 🚀 Slot18 — Assignment 2: Watch Management (Saga Pattern)

Hệ thống quản lý đồng hồ gồm 4 microservice, mỗi service một database riêng trên SQL Server (Database per Service). Thao tác thêm, sửa, xoá đồng hồ chạm tới hai database (`DBWatch` và `DBBrand`) nên được điều phối bằng **Saga dạng Orchestration**: `MSWatch` ghi trạng thái từng bước, khi một bước lỗi thì chạy các bước bù trừ theo thứ tự ngược lại (LIFO).

| Service | Cổng | Database | Vai trò |
|---------|------|----------|---------|
| `APIGateway_StudentCode` | 8080 | — | Spring Cloud Gateway, kiểm tra JWT, phân quyền theo role |
| `MSAccount_StudentCode` | 8081 | `DBAccount` | Đăng nhập, cấp access token và refresh token |
| `MSBrand_StudentCode` | 8082 | `DBBrand` | Thương hiệu, liên kết đồng hồ với thương hiệu (participant của Saga) |
| `MSWatch_StudentCode` | 8083 | `DBWatch` | CRUD đồng hồ, Saga orchestrator, lịch sử Saga |

### Các Saga

| Saga | Bước 1 (bù trừ) | Bước 2 (bù trừ) |
|------|-----------------|-----------------|
| Tạo đồng hồ | `CREATE_WATCH` (`DELETE_WATCH`) | `ASSIGN_BRAND` (`UNASSIGN_BRAND`) |
| Sửa đồng hồ | `UPDATE_WATCH` (`RESTORE_WATCH`) | `ASSIGN_BRAND` (`ASSIGN_BRAND_BACK`) |
| Xoá đồng hồ | `UNASSIGN_BRAND` (`ASSIGN_BRAND_BACK`) | `DELETE_WATCH` (bước pivot, không bù trừ) |

Thiết kế chi tiết: [`docs/SAGA_DESIGN.md`](./Slot18/Assignment2/WatchManagement/docs/SAGA_DESIGN.md).

### Cách chạy

Đặt biến môi trường trong shell (hoặc tạo `.env` từ `.env.example`, file này không được commit):

```bash
cd Slot18/Assignment2/WatchManagement
export DB_PASSWORD='YourStrong@Password123' JWT_SECRET='<chuỗi bí mật dài ít nhất 32 ký tự>' \
       JWT_EXPIRATION=900000 REFRESH_EXPIRATION=604800000 SAGA_FAULT_INJECTION=true
docker compose up -d --build
```

Trên máy Apple Silicon (arm64), image SQL Server 2022 chỉ có bản amd64 và bị lỗi khi giả lập. Dùng thêm file override chạy Azure SQL Edge (cùng engine SQL Server):

```bash
docker compose -f docker-compose.yml -f docker-compose.arm64.yml up -d --build
```

| URL | Nội dung |
|-----|----------|
| `http://localhost:8080/api/auth/login` | Đăng nhập (`adminsys` / `@1` là quản trị viên) |
| `http://localhost:8080/api/watches` | Danh sách đồng hồ (công khai) |
| `http://localhost:8080/api/sagas` | Lịch sử Saga (chỉ quản trị viên) |
| `http://localhost:8083/swagger-ui.html` | Swagger UI của MSWatch |

### Kiểm thử Saga

Import [`WatchManagement_Saga.postman_collection.json`](./Slot18/Assignment2/WatchManagement/WatchManagement_Saga.postman_collection.json) vào Postman và chạy lần lượt các folder. Header `X-Saga-Fail-After-Step: <n>` giả lập lỗi sau bước thứ n để kiểm tra bù trừ. Chạy bằng newman:

```bash
docker run --rm -v "$PWD":/etc/newman postman/newman:6-alpine run WatchManagement_Saga.postman_collection.json \
  --env-var baseUrl=http://host.docker.internal:8080
```

Báo cáo: [`Assignment 2_Report.docx`](./Slot18/Assignment2/Assignment%202_Report.docx).
