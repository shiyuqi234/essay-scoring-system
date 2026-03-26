-- 修复用户密码的 SQL 语句
-- 
-- 方法1：删除旧用户，通过应用注册功能重新创建（推荐）
-- 方法2：使用下面的 UPDATE 语句更新密码（需要先获取正确的 BCrypt 哈希）

USE essayscoring;

-- 删除所有测试用户（如果需要重新创建）
-- DELETE FROM users WHERE username LIKE 'student%' OR username LIKE 'teacher%';

-- 更新密码的方法：
-- 1. 启动应用
-- 2. 访问：http://localhost:8080/test/generate-password?password=password123
-- 3. 复制返回的 bcryptHash 值
-- 4. 执行下面的 UPDATE 语句，替换 'YOUR_BCRYPT_HASH_HERE' 为实际的哈希值

-- 更新所有学生密码为 password123
-- UPDATE users SET password = 'YOUR_BCRYPT_HASH_HERE' WHERE role = 'STUDENT';

-- 更新所有教师密码为 teacher123  
-- UPDATE users SET password = 'YOUR_BCRYPT_HASH_HERE' WHERE role = 'TEACHER';

-- 或者逐个更新
-- UPDATE users SET password = 'YOUR_BCRYPT_HASH_HERE' WHERE username = 'student001';
-- UPDATE users SET password = 'YOUR_BCRYPT_HASH_HERE' WHERE username = 'student002';
-- ... 以此类推
