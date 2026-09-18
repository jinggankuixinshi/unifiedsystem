DROP DATABASE IF EXISTS unified_sales_db;
CREATE DATABASE unified_sales_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE unified_sales_db;

SET NAMES utf8mb4;

-- ============================================================
-- 客户表
-- ============================================================
CREATE TABLE sal_customer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_code VARCHAR(50) NOT NULL COMMENT '客户编码',
    customer_name VARCHAR(100) NOT NULL COMMENT '客户名称',
    contact_person VARCHAR(50) DEFAULT '' COMMENT '联系人',
    phone VARCHAR(32) DEFAULT '' COMMENT '联系电话',
    address VARCHAR(200) DEFAULT '' COMMENT '地址',
    status VARCHAR(20) DEFAULT 'normal' COMMENT '状态 normal/disabled/archived',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_customer_code (customer_code),
    INDEX idx_name (customer_name),
    INDEX idx_status (status)
) COMMENT '客户表';

-- ============================================================
-- 产品价格历史
-- ============================================================
CREATE TABLE sal_product_price (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL COMMENT '产品ID',
    price DECIMAL(12,2) NOT NULL COMMENT '售价',
    effective_date DATE COMMENT '生效日期',
    source VARCHAR(50) DEFAULT '' COMMENT '来源',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_product (product_id),
    INDEX idx_date (effective_date)
) COMMENT '产品价格历史表';

-- ============================================================
-- 销售报单
-- ============================================================
CREATE TABLE sal_sales_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL COMMENT '报单号',
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    salesperson_id BIGINT NOT NULL COMMENT '销售员ID',
    total_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '总金额',
    payment_method VARCHAR(20) DEFAULT '' COMMENT '付款方式',
    delivery_date DATE COMMENT '交货日期',
    warranty_terms VARCHAR(500) DEFAULT '' COMMENT '质保条款',
    price_anomaly_level INT DEFAULT 0 COMMENT '价格异常等级 0正常 1轻度 2中度 3重度',
    low_price_reason VARCHAR(500) DEFAULT '' COMMENT '低价特批理由（中度异常必填）',
    approval_status INT DEFAULT 0 COMMENT '审批状态 0待审 1通过 2驳回',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_customer (customer_id),
    INDEX idx_salesperson (salesperson_id),
    INDEX idx_status (approval_status),
    INDEX idx_anomaly (price_anomaly_level)
) COMMENT '销售报单表';

-- ============================================================
-- 销售报单明细
-- ============================================================
CREATE TABLE sal_sales_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '报单ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    specification VARCHAR(100) DEFAULT '' COMMENT '规格',
    quantity DECIMAL(12,2) NOT NULL COMMENT '数量',
    unit_price DECIMAL(12,2) NOT NULL COMMENT '单价',
    amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '金额',
    price_deviation DECIMAL(8,4) DEFAULT 0.0000 COMMENT '价格偏离度',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_order (order_id),
    INDEX idx_product (product_id),
    INDEX idx_deviation (price_deviation)
) COMMENT '销售报单明细表';

-- ============================================================
-- 报单附件
-- ============================================================
CREATE TABLE sal_sales_order_attachment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '报单ID',
    file_name VARCHAR(200) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    upload_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_order (order_id)
) COMMENT '报单附件表';

-- ============================================================
-- 合同
-- ============================================================
CREATE TABLE sal_contract (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_no VARCHAR(50) NOT NULL COMMENT '合同编号',
    sales_order_id BIGINT NOT NULL COMMENT '销售单ID',
    content_json TEXT COMMENT '合同内容JSON',
    seal_id VARCHAR(100) DEFAULT '' COMMENT '电子章ID',
    esign_contract_id VARCHAR(100) DEFAULT '' COMMENT 'e签宝合同ID',
    pdf_path VARCHAR(500) DEFAULT '' COMMENT 'PDF路径',
    sign_status INT DEFAULT 0 COMMENT '签署状态 0待签署 1部分签署 2已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_contract_no (contract_no),
    INDEX idx_sales_order (sales_order_id),
    INDEX idx_sign_status (sign_status)
) COMMENT '合同表';

-- ============================================================
-- 合同签署日志
-- ============================================================
CREATE TABLE sal_contract_sign_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contract_id BIGINT NOT NULL COMMENT '合同ID',
    signer_name VARCHAR(50) NOT NULL COMMENT '签署人',
    sign_time DATETIME COMMENT '签署时间',
    sign_result VARCHAR(20) COMMENT '签署结果 signed/rejected',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_contract (contract_id)
) COMMENT '合同签署日志表';

-- ============================================================
-- 售后服务记录
-- ============================================================
CREATE TABLE sal_after_sale (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '销售单ID',
    issue_type VARCHAR(50) DEFAULT '' COMMENT '问题类型',
    description TEXT COMMENT '问题描述',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态 pending/processing/resolved',
    handler_id BIGINT COMMENT '处理人ID',
    handle_time DATETIME COMMENT '处理时间',
    handle_result TEXT COMMENT '处理结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_order (order_id),
    INDEX idx_status (status)
) COMMENT '售后服务记录表';

-- ============================================================
-- 价格异常阈值配置
-- ============================================================
CREATE TABLE sal_price_anomaly_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    level VARCHAR(20) NOT NULL COMMENT '等级 mild/moderate/severe',
    threshold DECIMAL(8,4) NOT NULL COMMENT '阈值',
    action VARCHAR(200) NOT NULL COMMENT '触发动作 label/required_reason/boss_approval',
    description VARCHAR(500) DEFAULT '' COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_level (level)
) COMMENT '价格异常阈值配置表';

INSERT INTO sal_price_anomaly_config (level, threshold, action, description) VALUES
('mild', 0.1500, 'label', '低于3个月均价85%且≥70%，系统打标签'),
('moderate', 0.3000, 'required_reason', '低于70%且≥50%，强制填写低价特批理由'),
('severe', 0.5000, 'boss_approval', '低于50%，强制老板终审');

-- ============================================================
-- 权限授予（三账户模型）
-- ============================================================
GRANT ALL PRIVILEGES ON unified_sales_db.* TO 'unified_dev'@'localhost';
GRANT SELECT, INSERT, UPDATE ON unified_sales_db.* TO 'unified_ops'@'localhost';
GRANT SELECT, INSERT, UPDATE ON unified_sales_db.* TO 'unified_app'@'localhost';
