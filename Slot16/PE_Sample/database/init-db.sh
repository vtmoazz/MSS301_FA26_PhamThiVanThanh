#!/bin/bash
# Chạy trong container db-init: tạo database, tạo bảng và nạp dữ liệu mẫu nếu chưa có.
# SQLCMD có thể đổi qua biến môi trường (image arm64 dùng bản sqlcmd cũ, không có cờ -C).
set -e
SQLCMD="${SQLCMD:-/opt/mssql-tools18/bin/sqlcmd -C}"
run() { $SQLCMD -S sqlserver -U sa -P "$DB_PASSWORD" -b -l 5 "$@"; }

for i in $(seq 1 30); do
  run -Q "SELECT 1" > /dev/null && break
  echo "SQL Server not ready yet, retry $i"; sleep 3
done

run -i /db/00_create_database.sql
tables=$(run -h -1 -W -Q "SET NOCOUNT ON; SELECT COUNT(*) FROM MSS301_2026_PE_MEDIA.sys.tables WHERE name IN ('users','posts')")
if [ "$(echo "$tables" | tr -d '[:space:]')" = "0" ]; then
  run -i /db/01_create_users.sql
  run -i /db/02_create_posts.sql
  run -i /db/03_seed_users.sql
  echo "Tables created and seed data loaded"
else
  echo "Tables already exist, skipping"
fi
