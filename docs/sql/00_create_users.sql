SET NAMES utf8mb4;
-- 连接字符集统一为 utf8mb4，防止 Windows/macOS 客户端默认字符集（GBK/latin1）导致中文乱码

-- =============================================================================
-- UnifiedSystem 数据库账户初始化脚本 v2.0
-- 三账户模型：开发(unified_dev) / 运维(unified_ops) / 应用(unified_app)
--
-- 执行方式：
--   方式一（推荐）：通过环境变量注入密码
--     SET DB_DEV_PWD=your_dev_pwd
--     SET DB_OPS_PWD=your_ops_pwd
--     SET DB_APP_PWD=your_app_pwd
--     mysql --default-character-set=utf8mb4 -u root -p -e "SET @dev='%DB_DEV_PWD%'; SET @ops='%DB_OPS_PWD%'; SET @app='%DB_APP_PWD%'; source 00_create_users.sql"
--
--   方式二：手动替换下方三个密码后直接执行
--     mysql --default-character-set=utf8mb4 -u root -p < 00_create_users.sql
-- =============================================================================

-- 若未通过命令行传入，使用下方的默认占位符（部署时务必替换！）
SET @dev_pwd = IFNULL(@dev, '<请替换为开发账户密码>');
SET @ops_pwd = IFNULL(@ops, '<请替换为运维账户密码>');
SET @app_pwd = IFNULL(@app, '<请替换为应用账户密码>');

-- =============================================================================
-- 1. 删除旧账户
-- =============================================================================
DROP USER IF EXISTS 'unified_dev'@'localhost';
DROP USER IF EXISTS 'unified_ops'@'localhost';
DROP USER IF EXISTS 'unified_app'@'localhost';

-- =============================================================================
-- 2. 创建账户（通过 PREPARE 动态 SQL 注入密码）
-- =============================================================================

-- 开发账户：DDL + DML 全部权限，最大连接数 32
SET @stmt = CONCAT("CREATE USER 'unified_dev'@'localhost' IDENTIFIED BY 'unified123' WITH MAX_USER_CONNECTIONS 32");
PREPARE stmt_dev FROM @stmt; EXECUTE stmt_dev; DEALLOCATE PREPARE stmt_dev;

-- 运维账户：仅 DML（SELECT/INSERT/UPDATE），最大连接数 8
SET @stmt = CONCAT("CREATE USER 'unified_ops'@'localhost' IDENTIFIED BY 'unified123' WITH MAX_USER_CONNECTIONS 8");
PREPARE stmt_ops FROM @stmt; EXECUTE stmt_ops; DEALLOCATE PREPARE stmt_ops;

-- 应用账户：仅 DML（SELECT/INSERT/UPDATE），最大连接数 16
SET @stmt = CONCAT("CREATE USER 'unified_app'@'localhost' IDENTIFIED BY 'unified123' WITH MAX_USER_CONNECTIONS 16");
PREPARE stmt_app FROM @stmt; EXECUTE stmt_app; DEALLOCATE PREPARE stmt_app;

-- =============================================================================
-- 3. 五库授权
-- =============================================================================

-- 开发账户：ALL PRIVILEGES（建表/改表/删表/建索引）
GRANT ALL PRIVILEGES ON `unified_system_db`.*     TO 'unified_dev'@'localhost';
GRANT ALL PRIVILEGES ON `unified_production_db`.* TO 'unified_dev'@'localhost';
GRANT ALL PRIVILEGES ON `unified_logistics_db`.*  TO 'unified_dev'@'localhost';
GRANT ALL PRIVILEGES ON `unified_sales_db`.*      TO 'unified_dev'@'localhost';
GRANT ALL PRIVILEGES ON `unified_finance_db`.*    TO 'unified_dev'@'localhost';

-- 运维账户：SELECT/INSERT/UPDATE（无 DELETE —— 系统走 @TableLogic 逻辑删除）
GRANT SELECT, INSERT, UPDATE ON `unified_system_db`.*     TO 'unified_ops'@'localhost';
GRANT SELECT, INSERT, UPDATE ON `unified_production_db`.* TO 'unified_ops'@'localhost';
GRANT SELECT, INSERT, UPDATE ON `unified_logistics_db`.*  TO 'unified_ops'@'localhost';
GRANT SELECT, INSERT, UPDATE ON `unified_sales_db`.*      TO 'unified_ops'@'localhost';
GRANT SELECT, INSERT, UPDATE ON `unified_finance_db`.*    TO 'unified_ops'@'localhost';

-- 应用账户：SELECT/INSERT/UPDATE（最小权限，禁止 DELETE + DDL）
GRANT SELECT, INSERT, UPDATE ON `unified_system_db`.*     TO 'unified_app'@'localhost';
GRANT SELECT, INSERT, UPDATE ON `unified_production_db`.* TO 'unified_app'@'localhost';
GRANT SELECT, INSERT, UPDATE ON `unified_logistics_db`.*  TO 'unified_app'@'localhost';
GRANT SELECT, INSERT, UPDATE ON `unified_sales_db`.*      TO 'unified_app'@'localhost';
GRANT SELECT, INSERT, UPDATE ON `unified_finance_db`.*    TO 'unified_app'@'localhost';

-- =============================================================================
-- 4. 刷新
-- =============================================================================
FLUSH PRIVILEGES;

-- 验证
SELECT User AS '账户', Host AS '主机', max_user_connections AS '最大连接数'
FROM mysql.user WHERE User LIKE 'unified_%';
