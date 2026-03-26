-- 更新用户密码的 SQL 语句
-- 使用方法：在 MySQL 命令行或客户端工具中执行此脚本
-- 
-- 注意：这些密码哈希是通过 BCryptPasswordEncoder 生成的
-- 如果这些密码仍然无法使用，请访问 http://localhost:8080/test/generate-password?password=password123
-- 获取新的密码哈希值

USE essayscoring;

-- 更新学生密码为 password123
-- 注意：需要先运行应用，访问 http://localhost:8080/test/generate-password?password=password123
-- 获取正确的 BCrypt 哈希值，然后替换下面的哈希值

-- 临时方案：先删除旧用户，然后通过应用注册功能重新创建
-- 或者使用下面的 UPDATE 语句更新密码（需要先获取正确的 BCrypt 哈希）

-- 示例：更新 student001 的密码
-- UPDATE users SET password = '这里填入从 /test/generate-password 获取的哈希值' WHERE username = 'student001';

-- 更简单的方法：通过应用注册功能创建新用户
-- 访问 http://localhost:8080/student.html 或 http://localhost:8080/teacher.html 注册新用户
