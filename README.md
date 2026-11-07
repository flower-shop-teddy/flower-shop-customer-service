# flower-shop-customer-service

Khách hàng cá nhân: hồ sơ (mã `KH_CN_0000001`, SĐT, email, ngày sinh) và sổ địa chỉ người nhận hoa. PostgreSQL + Flyway.

Cổng local: `8082`

## Luật nghiệp vụ chính

- Mã khách hàng `KH_CN_` + 7 chữ số, sinh từ sequence `customer_code_seq`, không sửa được.
- SĐT (`^0\d{9}$`) và email (lưu chữ thường) là duy nhất. SĐT không đổi qua API sửa hồ sơ, vì đổi SĐT cần OTP và sẽ có luồng riêng.
- Xóa khách hàng = `INACTIVE` (soft delete). Khách `INACTIVE` không thêm/sửa địa chỉ được.
- Mỗi khách có tối đa 10 địa chỉ và **đúng 1 địa chỉ mặc định** (DB chặn thêm bằng partial unique index). Địa chỉ đầu tiên tự thành mặc định. Xóa địa chỉ mặc định thì địa chỉ mới nhất còn lại được lên làm mặc định.
- Địa chỉ của khách khác trả 404, không trả 403.
- API danh sách che SĐT/email. Log không bao giờ ghi SĐT đầy đủ.

## API

Mọi API cần token. Hai nhóm:

**Nhân viên** (permission do identity-service cấp theo role):

| Method | Path | Mô tả | Permission |
|---|---|---|---|
| POST | `/api/v1/customers` | Tạo khách hàng | `customer:create` |
| GET | `/api/v1/customers/{id}` | Chi tiết | `customer:read` |
| GET | `/api/v1/customers?phone=&page=&size=` | Danh sách khách ACTIVE (đã che PII) | `customer:read` |
| PUT | `/api/v1/customers/{id}` | Sửa họ tên, email, ngày sinh (kèm `version`) | `customer:write` |
| DELETE | `/api/v1/customers/{id}` | Ngừng hoạt động | `customer:deactivate` |
| GET / POST | `/api/v1/customers/{id}/addresses` | Xem / thêm địa chỉ | `customer:read` / `customer:write` |
| PUT / DELETE | `/api/v1/customers/{id}/addresses/{addressId}` | Sửa (kèm `version`) / xóa địa chỉ | `customer:write` |
| PUT | `/api/v1/customers/{id}/addresses/{addressId}/default` | Đặt làm mặc định | `customer:write` |

**Khách hàng** (mẫu `/me`: id khách lấy từ claim `customer_id` của token, không bao giờ từ URL):

| Method | Path | Mô tả |
|---|---|---|
| GET / PUT | `/api/v1/customers/me` | Xem / sửa hồ sơ của mình |
| GET / POST | `/api/v1/customers/me/addresses` | Sổ địa chỉ của mình |
| PUT / DELETE | `/api/v1/customers/me/addresses/{addressId}` | Sửa / xóa |
| PUT | `/api/v1/customers/me/addresses/{addressId}/default` | Đặt mặc định |

identity-service gọi `POST /api/v1/customers` bằng token riêng của nó (`user_type=SERVICE`, chỉ có `customer:create`) khi khách tự đăng ký.

Mã lỗi: `CUS-404-001` không tìm thấy khách · `CUS-404-002` không tìm thấy địa chỉ · `CUS-409-001` trùng SĐT · `CUS-409-002` trùng email · `CUS-422-001` khách đã ngừng hoạt động · `CUS-422-002` đã đủ 10 địa chỉ.

## Chạy

Cách khuyên dùng: chạy cả hệ thống từ repo `flower-shop-workspace` (`./clone-all.sh` rồi `./run-local.sh`).

Cần database `customer_db` (file `infra/postgres/init/02-create-customer-db.sql` trong workspace).

Build riêng repo này:

```bash
mvn verify     # cần Docker: test chạy với Postgres thật (Testcontainers)
```

Cần parent `com.flowershop:flower-shop-parent` từ repo `flower-shop-platform`: nằm sẵn ở `../platform` trong workspace,
hoặc đã `mvn install` vào `~/.m2`.

## Release và Docker image

Merge PR `dev → production` thì GitHub Actions publish image `ghcr.io/flower-shop-teddy/flower-shop-customer-service:X.Y.Z` và tạo tag `vX.Y.Z`.
Chi tiết quy trình: README của repo `flower-shop-workspace`, mục "CI/CD và release".
