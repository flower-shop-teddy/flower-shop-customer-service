-- Quy tắc Flyway: file migration đã chạy thì KHÔNG BAO GIỜ sửa.
-- Muốn thay đổi bảng thì tạo file mới V2__..., V3__...

-- Số thứ tự cho mã khách hàng KH_CN_0000001.
-- MAXVALUE + NO CYCLE: hết số thì DB báo lỗi, không bao giờ quay vòng cấp trùng mã.
-- Sequence có thể "nhảy số" (transaction rollback vẫn tiêu số): chấp nhận được, mã chỉ cần duy nhất.
CREATE SEQUENCE customer_code_seq START WITH 1 INCREMENT BY 1 MAXVALUE 9999999 NO CYCLE;

CREATE TABLE customer (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_code VARCHAR(20)  NOT NULL,
    full_name     VARCHAR(150) NOT NULL,
    phone         VARCHAR(10)  NOT NULL,
    email         VARCHAR(254),
    date_of_birth DATE,
    status        VARCHAR(20)  NOT NULL,
    version       BIGINT       NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL,
    created_by    VARCHAR(100) NOT NULL,
    updated_at    TIMESTAMPTZ,
    updated_by    VARCHAR(100),

    CONSTRAINT uk_customer_code  UNIQUE (customer_code),
    CONSTRAINT uk_customer_phone UNIQUE (phone),
    CONSTRAINT uk_customer_email UNIQUE (email),          -- Postgres coi các NULL là khác nhau: nhiều khách không có email vẫn được
    CONSTRAINT ck_customer_code_format  CHECK (customer_code ~ '^KH_CN_[0-9]{7}$'),
    CONSTRAINT ck_customer_phone_format CHECK (phone ~ '^0[0-9]{9}$'),
    CONSTRAINT ck_customer_email_lower  CHECK (email = lower(email)), -- UNIQUE chỉ đúng khi mọi email đã chuẩn hóa chữ thường
    CONSTRAINT ck_customer_status       CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE INDEX idx_customer_status_created_at ON customer (status, created_at DESC);

-- Sổ địa chỉ người nhận hoa. Không có cấp quận/huyện: từ 01/07/2025 Việt Nam chỉ còn tỉnh/thành + phường/xã.
CREATE TABLE delivery_address (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id     BIGINT       NOT NULL,
    recipient_name  VARCHAR(150) NOT NULL,
    recipient_phone VARCHAR(10)  NOT NULL,
    address_line    VARCHAR(255) NOT NULL,
    ward            VARCHAR(100) NOT NULL,
    province        VARCHAR(100) NOT NULL,
    note            VARCHAR(500),
    is_default      BOOLEAN      NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 0,
    created_at      TIMESTAMPTZ  NOT NULL,
    created_by      VARCHAR(100) NOT NULL,
    updated_at      TIMESTAMPTZ,
    updated_by      VARCHAR(100),

    CONSTRAINT fk_address_customer FOREIGN KEY (customer_id) REFERENCES customer (id),
    CONSTRAINT ck_address_recipient_phone_format CHECK (recipient_phone ~ '^0[0-9]{9}$')
);

CREATE INDEX idx_address_customer ON delivery_address (customer_id);

-- Mỗi khách tối đa MỘT địa chỉ mặc định (partial unique index: chỉ xét các dòng is_default = true).
-- Chốt chặn cuối ở DB, kể cả khi code có bug hoặc hai request chạy song song.
CREATE UNIQUE INDEX uk_address_one_default_per_customer ON delivery_address (customer_id) WHERE is_default;
