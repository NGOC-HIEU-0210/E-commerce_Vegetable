-- Chỉ chạy lần đầu khi volume MySQL còn trống.
CREATE DATABASE IF NOT EXISTS keycloak CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
GRANT ALL PRIVILEGES ON keycloak.* TO 'indmart'@'%';
FLUSH PRIVILEGES;
