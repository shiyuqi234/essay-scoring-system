-- 删除测试用户数据
-- 使用方法：在 MySQL 命令行或客户端工具中执行此脚本
-- 删除后可以通过应用注册功能重新创建用户

USE essayscoring;

-- 删除所有测试学生用户
DELETE FROM users WHERE username LIKE 'student%';

-- 删除所有测试教师用户
DELETE FROM users WHERE username LIKE 'teacher%';

-- 查看删除结果（应该没有这些用户了）
SELECT id, username, role, student_id, display_name FROM users;
