-- 创建数据库脚本
-- 使用方法：在 MySQL 命令行或客户端工具中执行此脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS essayscoring 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- 显示创建结果
SHOW DATABASES LIKE 'essayscoring';

-- 使用数据库
USE essayscoring;

-- 如果需要创建专用用户（可选）
-- CREATE USER 'essay_user'@'localhost' IDENTIFIED BY 'your_password';
-- GRANT ALL PRIVILEGES ON essayscoring.* TO 'essay_user'@'localhost';
-- FLUSH PRIVILEGES;
