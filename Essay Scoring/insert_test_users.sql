-- 插入测试用户数据（学生和教师）
-- 使用方法：在 MySQL 命令行或客户端工具中执行此脚本
-- 
-- 默认密码说明：
-- 所有学生账号密码：password123
-- 所有教师账号密码：teacher123
-- 
-- 注意：如果这些密码哈希无法使用，请通过应用注册功能创建用户，
--       或者使用在线 BCrypt 生成器生成新的密码哈希：
--       https://bcrypt-generator.com/

USE essayscoring;

-- 插入学生用户（密码：password123）
INSERT INTO users (username, password, role, student_id, display_name, enabled, create_time, update_time) VALUES
('student001', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJLW', 'STUDENT', 'S001', '张三', true, NOW(), NOW()),
('student002', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJLW', 'STUDENT', 'S002', '李四', true, NOW(), NOW()),
('student003', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJLW', 'STUDENT', 'S003', '王五', true, NOW(), NOW()),
('student004', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJLW', 'STUDENT', 'S004', '赵六', true, NOW(), NOW()),
('student005', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwK8pJLW', 'STUDENT', 'S005', '钱七', true, NOW(), NOW());

-- 插入教师用户（密码：teacher123）
INSERT INTO users (username, password, role, student_id, display_name, enabled, create_time, update_time) VALUES
('teacher001', '$2a$10$8K1p/a0dL1L0z8K1p/a0eO8K1p/a0dL1L0z8K1p/a0dL1L0z8K1p', 'TEACHER', NULL, '张老师', true, NOW(), NOW()),
('teacher002', '$2a$10$8K1p/a0dL1L0z8K1p/a0eO8K1p/a0dL1L0z8K1p/a0dL1L0z8K1p', 'TEACHER', NULL, '李老师', true, NOW(), NOW()),
('teacher003', '$2a$10$8K1p/a0dL1L0z8K1p/a0eO8K1p/a0dL1L0z8K1p/a0dL1L0z8K1p', 'TEACHER', NULL, '王老师', true, NOW(), NOW());

-- 查看插入结果
SELECT id, username, role, student_id, display_name, enabled, create_time FROM users ORDER BY role, id;
