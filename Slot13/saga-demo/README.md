# Slot13 — Saga Pattern: Order Saga (Orchestration)

Sinh viên: **Phạm Thị Vân Thanh — SE192641**

Demo nghiệp vụ "Đặt hàng" chạy trên 3 service, mỗi service một loại database, điều phối bằng Saga Orchestrator.
Stack: Java 21, Spring Boot 4.1.1, Spring MVC, RestClient, Spring Data JPA / MongoDB, Lombok, Docker Compose, Postman.

| Service | Port | Database | Local transaction | Compensation |
|---|---|---|---|---|
| `order-service` | 8081 | MySQL 8.4 (`orderdb`, host 3307) | tạo order `PENDING`, `confirmOrder` | `cancelOrder` |
| `inventory-service` | 8082 | MongoDB 8.0 replica set (`inventorydb`, host 27017) | `reserve` | `release` |
| `payment-service` | 8083 | SQL Server 2022 (`paymentdb`, host 14330) | `pay` | `refund` |

Trạng thái Saga: `PENDING → EXECUTING → COMPLETED`, hoặc `FAILED → COMPENSATING → COMPENSATED`.
Compensation chạy theo thứ tự ngược (stack LIFO), mỗi bước thử tối đa 3 lần bằng `RetryTemplate` của Spring Framework 7.

## Cách chạy

```bash
cd Slot13/saga-demo
docker compose up -d --build
# Máy Apple Silicon (arm64): SQL Server 2022 chỉ có bản amd64, dùng thêm file override
docker compose -f docker-compose.yml -f docker-compose.arm64.yml up -d --build
```

Reset dữ liệu: `docker compose down -v`.

## API

| Method | URL | Mô tả |
|---|---|---|
| POST | `/api/orders` | Đặt hàng, chạy Saga. Body `{customerId, productId, quantity, amount}` |
| GET | `/api/orders` | Danh sách order |
| GET | `/api/orders/{id}/saga-logs` | Các bước Saga của order |
| POST | `/api/inventory/reserve`, `/api/inventory/release/{orderId}` | Giữ / trả hàng (idempotent) |
| GET | `/api/inventory/products`, `/api/inventory/reservations` | Tồn kho, reservation |
| POST | `/api/payments/pay`, `/api/payments/refund/{orderId}` | Thanh toán / hoàn tiền (idempotent) |
| GET | `/api/payments/wallets`, `/api/payments` | Ví, payment |

## Kiểm thử bằng Postman

Import `Saga_Postman_Collection.json`, chạy Collection Runner cho `0 → KB1 → KB2 → KB3 → KB4 → KB5 → KB7`.
KB6 (payment-service tắt) và KB8 (compensation cũng lỗi) chạy thủ công trong thư mục "Thủ công".

Chạy bằng dòng lệnh:

```bash
npx newman run Saga_Postman_Collection.json --folder "0 - Kiểm tra service" --folder "KB1 - Happy path" \
  --folder "KB2 - Thanh toán thất bại (402)" --folder "KB3 - Hết hàng (409)" \
  --folder "KB4 - Sản phẩm không tồn tại (404 inventory)" --folder "KB5 - Ví không tồn tại (404 payment)" \
  --folder "KB7 - Idempotent (gọi lặp lại)"
```

Báo cáo kết quả test 2 kịch bản (KB2, KB6) nằm ở [`Slot14`](../../Slot14).
