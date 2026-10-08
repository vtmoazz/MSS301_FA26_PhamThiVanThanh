# FU Cinema Booking System

Hệ thống đặt vé xem phim theo kiến trúc microservices (Spring Boot 4.1, Java 21, polyglot persistence).

| Service | Port | Database |
|---------|------|----------|
| `api-gateway` | 9000 | — (verify JWT, định tuyến, phân quyền) |
| `customer-service` | 8081 | SQL Server (`cinema_customer`) |
| `movie-service` | 8082 | MongoDB (`cinema_movie`) |
| `booking-service` | 8083 | MySQL (`cinema_booking`) |

## Cách chạy

1. Khởi động 3 database:

   ```bash
   docker compose up -d
   ```

2. Chạy từng service (mỗi service một terminal), thứ tự: customer → movie → booking → gateway:

   ```bash
   cd customer-service && mvn spring-boot:run
   ```

   Flyway tự tạo bảng cho customer/booking; `DataSeeder` tự nạp dữ liệu mẫu cho MongoDB.

3. Gọi API qua gateway: `http://localhost:9000`.

## Tài khoản test

| Role | Email | Password |
|------|-------|----------|
| ADMIN | `admin@fucinema.com` | `@@abc123@@` |
| CUSTOMER | `an@gmail.com` | `123456` |
| CUSTOMER | `binh@gmail.com` | `123456` |
| CUSTOMER (INACTIVE) | `chi@gmail.com` | `123456` |

## Kiểm thử

Import `../postman/FUCinema-Booking-System.postman_collection.json` và environment
`../postman/FUCinema-Local.postman_environment.json` vào Postman, hoặc chạy bằng Newman:

```bash
newman run ../postman/FUCinema-Booking-System.postman_collection.json -e ../postman/FUCinema-Local.postman_environment.json
```
