-- =====================================================================
--  Dữ liệu mẫu (5 bản ghi / bảng) cho dự án Web-Ind-mart
--  Database : javaweb34db_btck   (MySQL 8)
--  Cách dùng: chạy ứng dụng Spring Boot 1 lần để Hibernate tự tạo bảng
--             (ddl-auto=update), sau đó chạy file này.
--  Mật khẩu của tất cả tài khoản mẫu: 123456  (đã mã hoá BCrypt)
--  Ảnh: đường dẫn lấy từ thư mục UploadFiles/ có sẵn trong dự án
-- =====================================================================

USE javaweb34db_btck;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- (Tuỳ chọn) Xoá dữ liệu cũ để chạy lại nhiều lần
-- TRUNCATE TABLE tbl_favourite;
-- TRUNCATE TABLE tbl_sale_order_product;
-- TRUNCATE TABLE tbl_sale_order;
-- TRUNCATE TABLE tbl_product_image;
-- TRUNCATE TABLE tbl_product;
-- TRUNCATE TABLE tbl_category;
-- TRUNCATE TABLE tbl_coupon;
-- TRUNCATE TABLE tbl_order_status;
-- TRUNCATE TABLE tbl_wallet;
-- TRUNCATE TABLE user_role;
-- TRUNCATE TABLE tbl_user;
-- TRUNCATE TABLE tbl_role;

-- ---------------------------------------------------------------------
-- 1. tbl_role
--    (Security dùng hasAuthority("ADMIN"); đăng ký mới dùng "ROLE_USER")
-- ---------------------------------------------------------------------
INSERT INTO tbl_role (id, name, description) VALUES
                                                 (1, 'ADMIN',        'Quản trị viên hệ thống'),
                                                 (2, 'ROLE_USER',    'Khách hàng'),
                                                 (3, 'ROLE_STAFF',   'Nhân viên bán hàng'),
                                                 (4, 'ROLE_MANAGER', 'Quản lý cửa hàng'),
                                                 (5, 'ROLE_SHIPPER', 'Nhân viên giao hàng');

-- ---------------------------------------------------------------------
-- 2. tbl_user  (password = 123456, BCrypt)
-- ---------------------------------------------------------------------
INSERT INTO tbl_user
(id, username, password, email, first_name, last_name, mobile, address, avatar, status, otp, otp_expiry, created_date, updated_date) VALUES
                                                                                                                                         (1, 'admin',      '$2a$10$38h.zffhnVzcxcyKnS1/8.whSRO4WwVV.ulYXMMEMt/etOre8yW4u', 'admin@indmart.vn',   'Hiếu',  'Nguyễn', '0901234567', '12 Nguyễn Huệ, Quận 1, TP.HCM',         '/UploadFiles/Account/Avatar/Anh.jpg',   1, NULL, NULL, '2026-10-01 08:00:00', NULL),
                                                                                                                                         (2, 'nguyenvana', '$2a$10$38h.zffhnVzcxcyKnS1/8.whSRO4WwVV.ulYXMMEMt/etOre8yW4u', 'vana@gmail.com',     'Văn A', 'Nguyễn', '0912345678', '45 Lê Lợi, Quận 1, TP.HCM',             '/UploadFiles/Account/Avatar/anh-1.png', 1, NULL, NULL, '2026-10-02 09:15:00', NULL),
                                                                                                                                         (3, 'tranthib',   '$2a$10$38h.zffhnVzcxcyKnS1/8.whSRO4WwVV.ulYXMMEMt/etOre8yW4u', 'thib@gmail.com',     'Thị B', 'Trần',   '0923456789', '78 Trần Hưng Đạo, Quận 5, TP.HCM',      '/UploadFiles/Account/Avatar/anh-2.png', 1, NULL, NULL, '2026-10-03 10:30:00', NULL),
                                                                                                                                         (4, 'levanc',     '$2a$10$38h.zffhnVzcxcyKnS1/8.whSRO4WwVV.ulYXMMEMt/etOre8yW4u', 'vanc@gmail.com',     'Văn C', 'Lê',     '0934567890', '23 Võ Văn Tần, Quận 3, TP.HCM',         '/UploadFiles/Account/Avatar/anh-3.png', 1, NULL, NULL, '2026-10-04 14:20:00', NULL),
                                                                                                                                         (5, 'phamthid',   '$2a$10$38h.zffhnVzcxcyKnS1/8.whSRO4WwVV.ulYXMMEMt/etOre8yW4u', 'thid@gmail.com',     'Thị D', 'Phạm',   '0945678901', '99 Cách Mạng Tháng 8, Quận 10, TP.HCM', '/UploadFiles/Account/Avatar/anh-4.png', 1, NULL, NULL, '2026-10-05 16:45:00', NULL);

-- ---------------------------------------------------------------------
-- 3. user_role  (bảng trung gian User - Role)
-- ---------------------------------------------------------------------
INSERT INTO user_role (user_id, role_id) VALUES
                                             (1, 1),   -- admin       -> ADMIN
                                             (2, 2),   -- nguyenvana  -> ROLE_USER
                                             (3, 2),   -- tranthib    -> ROLE_USER
                                             (4, 2),   -- levanc      -> ROLE_USER
                                             (5, 2);   -- phamthid    -> ROLE_USER

-- ---------------------------------------------------------------------
-- 4. tbl_wallet  (mỗi user 1 ví)
-- ---------------------------------------------------------------------
INSERT INTO tbl_wallet (id, balance, user_id) VALUES
                                                  (1, 5000000, 1),
                                                  (2, 1500000, 2),
                                                  (3,  800000, 3),
                                                  (4, 2300000, 4),
                                                  (5,  450000, 5);

-- ---------------------------------------------------------------------
-- 5. tbl_category
-- ---------------------------------------------------------------------
INSERT INTO tbl_category (id, name, image, active, parent_category_id) VALUES
                                                                           (1, 'Rau củ',             '/UploadFiles/Category/Avatar/vegetable.png', 1, NULL),
                                                                           (2, 'Trái cây',           '/UploadFiles/Category/Avatar/fruit.png',     1, NULL),
                                                                           (3, 'Đồ uống',            '/UploadFiles/Category/Avatar/drink.png',     1, NULL),
                                                                           (4, 'Bánh kẹo - Snack',   '/UploadFiles/Category/Avatar/snack.png',     1, NULL),
                                                                           (5, 'Đồ dùng hàng ngày',  '/UploadFiles/Category/Avatar/daily.png',     1, NULL);

-- ---------------------------------------------------------------------
-- 6. tbl_product
-- ---------------------------------------------------------------------
INSERT INTO tbl_product (id, name, image, price, active, description, created_date, updated_date, category_id) VALUES
                                                                                                                   (1, 'Cà tím tươi (500g)',        '/UploadFiles/Product/Avatar/Brinjal.png', 25000,  1, 'Cà tím tươi, trồng theo tiêu chuẩn VietGAP, thích hợp xào, nướng hoặc kho.',        '2026-10-06 08:00:00', NULL, 1),
                                                                                                                   (2, 'Xoài cát Hòa Lộc (1kg)',    '/UploadFiles/Product/Avatar/mango.png',   65000,  1, 'Xoài cát Hòa Lộc chín cây, thịt dày, ngọt thơm, không hạt lép.',                '2026-10-06 08:10:00', NULL, 2),
                                                                                                                   (3, 'Nước ép trái cây tươi 350ml','/UploadFiles/Product/Avatar/drink.png',  35000,  1, 'Nước ép nguyên chất 100%, không đường, không chất bảo quản.',                   '2026-10-06 08:20:00', NULL, 3),
                                                                                                                   (4, 'Snack khoai tây giòn 100g', '/UploadFiles/Product/Avatar/snack.png',   15000,  1, 'Snack khoai tây vị tự nhiên, giòn rụm, đóng gói tiện lợi.',                     '2026-10-06 08:30:00', NULL, 4),
                                                                                                                   (5, 'Bộ đồ dùng gia đình tiện ích','/UploadFiles/Product/Avatar/daily.png', 120000, 1, 'Combo đồ dùng gia đình thiết yếu: khăn giấy, nước rửa chén, túi đựng rác.',     '2026-10-06 08:40:00', NULL, 5);

-- ---------------------------------------------------------------------
-- 7. tbl_product_image  (ảnh chi tiết sản phẩm)
-- ---------------------------------------------------------------------
INSERT INTO tbl_product_image (id, image_url, product_id) VALUES
                                                              (1, '/UploadFiles/Product/Image/Brinjal.png', 1),
                                                              (2, '/UploadFiles/Product/Image/mango.png',   2),
                                                              (3, '/UploadFiles/Product/Image/nuocEp.jpg',  3),
                                                              (4, '/UploadFiles/Product/Image/snack.png',   4),
                                                              (5, '/UploadFiles/Product/Image/daily.png',   5);

-- ---------------------------------------------------------------------
-- 8. tbl_coupon  (discount_type: PERCENT hoặc FIXED)
-- ---------------------------------------------------------------------
INSERT INTO tbl_coupon
(id, code, name, description, discount_type, discount_value, min_order_value, max_discount, start_date, end_date, usage_limit, used_count, active) VALUES
                                                                                                                                                       (1, 'WELCOME10',   'Chào bạn mới',          'Giảm 10% cho đơn từ 100.000đ, tối đa 50.000đ',   'PERCENT', 10,    100000, 50000,  '2026-01-01', '2026-12-31', 100, 1, 1),
                                                                                                                                                       (2, 'FREESHIP30K', 'Hỗ trợ phí vận chuyển', 'Giảm 30.000đ cho đơn từ 200.000đ',               'FIXED',   30000, 200000, NULL,   '2026-01-01', '2026-12-31', 200, 1, 1),
                                                                                                                                                       (3, 'SALE20',      'Siêu sale 20%',         'Giảm 20% cho đơn từ 300.000đ, tối đa 100.000đ',  'PERCENT', 20,    300000, 100000, '2026-10-01', '2026-12-31', 50,  1, 1),
                                                                                                                                                       (4, 'FRUIT15',     'Mừng mùa trái cây',     'Giảm 15% cho đơn từ 150.000đ, tối đa 40.000đ',   'PERCENT', 15,    150000, 40000,  '2026-10-01', '2026-11-30', 80,  0, 1),
                                                                                                                                                       (5, 'NEWUSER50K',  'Khách hàng mới 50K',    'Giảm 50.000đ cho đơn từ 250.000đ',               'FIXED',   50000, 250000, NULL,   '2026-01-01', '2026-12-31', 100, 1, 1);

-- ---------------------------------------------------------------------
-- 9. tbl_order_status  (tên PENDING/COMPLETED/CANCELLED được Dashboard dùng)
-- ---------------------------------------------------------------------
INSERT INTO tbl_order_status (id, name, description) VALUES
                                                         (1, 'PENDING',   'Chờ xử lý'),
                                                         (2, 'CONFIRMED', 'Đã xác nhận'),
                                                         (3, 'SHIPPING',  'Đang giao hàng'),
                                                         (4, 'COMPLETED', 'Hoàn thành'),
                                                         (5, 'CANCELLED', 'Đã huỷ');

-- ---------------------------------------------------------------------
-- 10. tbl_sale_order
--     total = tổng tiền hàng - giảm giá
-- ---------------------------------------------------------------------
INSERT INTO tbl_sale_order
(id, order_code, customer_name, email, phone, address, note, order_date, total, status_id, user_id, coupon_id, coupon_code) VALUES
                                                                                                                                (1, 'ORD-20261010-0001', 'Nguyễn Văn A', 'vana@gmail.com', '0912345678', '45 Lê Lợi, Quận 1, TP.HCM',             'Giao giờ hành chính',    '2026-10-10 09:30:00',  90000, 4, 2, 1, 'WELCOME10'),    -- 100.000 - 10% = 90.000
                                                                                                                                (2, 'ORD-20261012-0002', 'Trần Thị B',   'thib@gmail.com', '0923456789', '78 Trần Hưng Đạo, Quận 5, TP.HCM',      'Gọi trước khi giao',     '2026-10-12 14:10:00', 260000, 3, 3, 3, 'SALE20'),       -- 325.000 - 20% = 260.000
                                                                                                                                (3, 'ORD-20261014-0003', 'Lê Văn C',     'vanc@gmail.com', '0934567890', '23 Võ Văn Tần, Quận 3, TP.HCM',         NULL,                     '2026-10-14 18:45:00', 350000, 1, 4, NULL, NULL),        -- 350.000
                                                                                                                                (4, 'ORD-20261015-0004', 'Phạm Thị D',   'thid@gmail.com', '0945678901', '99 Cách Mạng Tháng 8, Quận 10, TP.HCM', 'Để hàng ở bảo vệ',       '2026-10-15 11:05:00', 250000, 2, 5, 5, 'NEWUSER50K'),   -- 300.000 - 50.000 = 250.000
                                                                                                                                (5, 'ORD-20261016-0005', 'Nguyễn Văn A', 'vana@gmail.com', '0912345678', '45 Lê Lợi, Quận 1, TP.HCM',             'Khách đổi ý, huỷ đơn',   '2026-10-16 20:00:00', 210000, 5, 2, 2, 'FREESHIP30K');  -- 240.000 - 30.000 = 210.000

-- ---------------------------------------------------------------------
-- 11. tbl_sale_order_product  (chi tiết đơn hàng, price = đơn giá tại thời điểm mua)
-- ---------------------------------------------------------------------
INSERT INTO tbl_sale_order_product (id, quantity, price, order_id, product_id) VALUES
                                                                                   (1,  4,  25000, 1, 1),   -- 4 x Cà tím            = 100.000
                                                                                   (2,  5,  65000, 2, 2),   -- 5 x Xoài cát          = 325.000
                                                                                   (3, 10,  35000, 3, 3),   -- 10 x Nước ép          = 350.000
                                                                                   (4, 20,  15000, 4, 4),   -- 20 x Snack khoai tây  = 300.000
                                                                                   (5,  2, 120000, 5, 5);   -- 2 x Đồ dùng gia đình  = 240.000

-- ---------------------------------------------------------------------
-- 12. tbl_favourite  (unique: user_id + product_id)
-- ---------------------------------------------------------------------
INSERT INTO tbl_favourite (id, user_id, product_id, created_date) VALUES
                                                                      (1, 2, 2, '2026-10-08 10:00:00'),
                                                                      (2, 2, 3, '2026-10-09 11:20:00'),
                                                                      (3, 3, 1, '2026-10-09 15:30:00'),
                                                                      (4, 4, 4, '2026-10-11 09:45:00'),
                                                                      (5, 5, 5, '2026-10-13 19:10:00');

SET FOREIGN_KEY_CHECKS = 1;

-- Kiểm tra nhanh số bản ghi
SELECT 'tbl_role' AS bang, COUNT(*) AS so_dong FROM tbl_role
UNION ALL SELECT 'tbl_user', COUNT(*) FROM tbl_user
UNION ALL SELECT 'user_role', COUNT(*) FROM user_role
UNION ALL SELECT 'tbl_wallet', COUNT(*) FROM tbl_wallet
UNION ALL SELECT 'tbl_category', COUNT(*) FROM tbl_category
UNION ALL SELECT 'tbl_product', COUNT(*) FROM tbl_product
UNION ALL SELECT 'tbl_product_image', COUNT(*) FROM tbl_product_image
UNION ALL SELECT 'tbl_coupon', COUNT(*) FROM tbl_coupon
UNION ALL SELECT 'tbl_order_status', COUNT(*) FROM tbl_order_status
UNION ALL SELECT 'tbl_sale_order', COUNT(*) FROM tbl_sale_order
UNION ALL SELECT 'tbl_sale_order_product', COUNT(*) FROM tbl_sale_order_product
UNION ALL SELECT 'tbl_favourite', COUNT(*) FROM tbl_favourite;