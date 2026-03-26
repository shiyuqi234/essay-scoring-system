# MySQL 数据库连接指南

## 📋 前置要求

1. **已安装 MySQL 8.0 或更高版本**
2. **MySQL 服务正在运行**
3. **知道 MySQL root 密码**

---

## 🚀 步骤 1：创建数据库

### 方式一：使用命令行（推荐）

1. **打开命令提示符（CMD）或 PowerShell**

2. **登录 MySQL**：
   ```bash
   mysql -u root -p
   ```
   输入你的 MySQL root 密码

3. **执行创建数据库命令**：
   ```sql
   CREATE DATABASE IF NOT EXISTS essayscoring 
   CHARACTER SET utf8mb4 
   COLLATE utf8mb4_unicode_ci;
   ```

4. **验证数据库创建**：
   ```sql
   SHOW DATABASES LIKE 'essayscoring';
   ```
   应该能看到 `essayscoring` 数据库

5. **退出 MySQL**：
   ```sql
   EXIT;
   ```

### 方式二：使用 SQL 脚本文件

1. **执行项目根目录下的 SQL 脚本**：
   ```bash
   mysql -u root -p < create_database.sql
   ```
   或者在 MySQL 客户端中打开 `create_database.sql` 文件并执行

### 方式三：使用图形化工具

**使用 MySQL Workbench、Navicat、DBeaver 等工具**：

1. 连接到 MySQL 服务器
2. 执行以下 SQL：
   ```sql
   CREATE DATABASE IF NOT EXISTS essayscoring 
   CHARACTER SET utf8mb4 
   COLLATE utf8mb4_unicode_ci;
   ```

---

## ⚙️ 步骤 2：配置 application.yml

编辑 `src/main/resources/application.yml` 文件，确保数据库配置正确：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/essayscoring?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root        # 修改为你的 MySQL 用户名
    password: 123456789   # 修改为你的 MySQL 密码
```

**重要**：
- 将 `username` 改为你的 MySQL 用户名（默认是 `root`）
- 将 `password` 改为你的 MySQL 密码
- 如果 MySQL 端口不是 3306，修改 URL 中的端口号

---

## 🔍 步骤 3：验证连接

### 1. 检查 MySQL 服务是否运行

**Windows**：
```bash
# 在服务管理器中查看 MySQL 服务是否运行
services.msc
```

或者在命令行中：
```bash
net start | findstr MySQL
```

**Linux/Mac**：
```bash
sudo systemctl status mysql
# 或
sudo service mysql status
```

### 2. 测试数据库连接

在 MySQL 命令行中：
```sql
mysql -u root -p
USE essayscoring;
SHOW TABLES;
```

如果数据库已创建，应该能正常连接。

---

## 🚀 步骤 4：启动应用

1. **确保 MySQL 服务正在运行**

2. **启动应用**：
   ```bash
   mvn spring-boot:run
   ```

3. **查看启动日志**：
   - 如果连接成功，会看到类似信息：
     ```
     HikariPool-1 - Start completed.
     ```
   - 如果连接失败，会看到错误信息，检查：
     - 数据库名称是否正确
     - 用户名和密码是否正确
     - MySQL 服务是否运行
     - 端口号是否正确

4. **表结构自动创建**：
   - 应用启动后，Hibernate 会自动创建所有表
   - 第一次启动时会看到 CREATE TABLE 的 SQL 语句

---

## 🛠️ 常见问题

### Q1: 连接失败 - Access denied

**错误信息**：
```
Access denied for user 'root'@'localhost' (using password: YES)
```

**解决方法**：
1. 检查用户名和密码是否正确
2. 确认 MySQL root 用户有权限访问
3. 尝试重置 MySQL root 密码

### Q2: 连接失败 - Unknown database

**错误信息**：
```
Unknown database 'essayscoring'
```

**解决方法**：
1. 执行 `create_database.sql` 脚本创建数据库
2. 或手动执行 `CREATE DATABASE essayscoring;`

### Q3: 连接失败 - Communications link failure

**错误信息**：
```
Communications link failure
```

**解决方法**：
1. 检查 MySQL 服务是否运行
2. 检查端口号是否正确（默认 3306）
3. 检查防火墙设置
4. 尝试 `telnet localhost 3306` 测试端口连接

### Q4: 时区错误

**错误信息**：
```
The server time zone value 'CST' is unrecognized
```

**解决方法**：
- URL 中已包含 `serverTimezone=Asia/Shanghai`，应该不会有这个问题
- 如果需要，可以在 MySQL 中设置时区：
  ```sql
  SET GLOBAL time_zone = '+8:00';
  ```

### Q5: 字符编码问题

**解决方法**：
- 数据库已设置为 `utf8mb4`，支持中文和 emoji
- 确保 MySQL 配置文件 `my.cnf` 中设置了：
  ```ini
  [mysqld]
  character-set-server=utf8mb4
  collation-server=utf8mb4_unicode_ci
  ```

---

## 📊 查看数据库

### 使用 MySQL 命令行

```bash
mysql -u root -p
USE essayscoring;
SHOW TABLES;
SELECT * FROM users;
```

### 使用图形化工具

**推荐工具**：
- **MySQL Workbench**（官方工具）
- **Navicat for MySQL**（商业软件）
- **DBeaver**（免费开源）
- **phpMyAdmin**（Web 界面）

**连接信息**：
- 主机：`localhost`
- 端口：`3306`
- 数据库：`essayscoring`
- 用户名：`root`（或你配置的用户名）
- 密码：你的 MySQL 密码

---

## 📝 数据库表结构

应用启动后会自动创建以下表：

- `users` - 用户表
- `essays` - 作文表
- `scores` - 评分表
- `feedbacks` - 反馈表
- `scoring_rules` - 评分规则表

---

## ✅ 验证清单

- [ ] MySQL 服务正在运行
- [ ] 已创建 `essayscoring` 数据库
- [ ] `application.yml` 中用户名和密码已正确配置
- [ ] 应用启动成功，没有连接错误
- [ ] 表结构已自动创建

---

## 🔄 切换回 H2 数据库

如果需要切换回 H2 数据库：

1. 修改 `application.yml`：
   - 注释掉 MySQL 配置
   - 取消注释 H2 配置

2. 重启应用

---

祝您使用愉快！🎉
