DROP DATABASE IF EXISTS unified_logistics_db;
CREATE DATABASE unified_logistics_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE unified_logistics_db;

SET NAMES utf8mb4;

-- ============================================================
-- 物流仓库存
-- ============================================================
CREATE TABLE log_warehouse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL COMMENT '产品ID',
    batch_no VARCHAR(50) NOT NULL COMMENT '批次号',
    production_date DATE COMMENT '生产日期',
    quantity DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '库存数量',
    location_code VARCHAR(50) DEFAULT '' COMMENT '库位编码',
    inbound_date DATE COMMENT '入库日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_product (product_id),
    INDEX idx_batch (batch_no),
    INDEX idx_location (location_code)
) COMMENT '物流仓库存表';

-- ============================================================
-- 物流仓出入库流水
-- ============================================================
CREATE TABLE log_warehouse_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    log_no VARCHAR(50) NOT NULL COMMENT '流水号',
    warehouse_id BIGINT NOT NULL COMMENT '库存ID',
    change_type INT NOT NULL COMMENT '变动类型 1入库 2出库',
    business_id BIGINT COMMENT '关联业务单据ID',
    business_no VARCHAR(50) COMMENT '关联业务单号',
    quantity DECIMAL(12,2) NOT NULL COMMENT '变动数量',
    batch_no VARCHAR(50) COMMENT '批次号',
    log_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    balance_after DECIMAL(12,2) DEFAULT 0.00 COMMENT '变动后余额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_log_no (log_no),
    INDEX idx_warehouse (warehouse_id),
    INDEX idx_business (business_id, business_no),
    INDEX idx_log_time (log_time)
) COMMENT '物流仓出入库流水表';

-- ============================================================
-- 调拨单
-- ============================================================
CREATE TABLE log_transfer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transfer_no VARCHAR(50) NOT NULL COMMENT '调拨单号',
    from_warehouse_id BIGINT COMMENT '调出仓ID',
    to_warehouse_id BIGINT COMMENT '调入仓ID',
    type VARCHAR(20) DEFAULT 'normal' COMMENT '调拨类型 normal/sample/scrap/repair/gift',
    total_value DECIMAL(12,2) DEFAULT 0.00 COMMENT '总价值',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    approval_status INT DEFAULT 0 COMMENT '审批状态 0待审 1通过 2驳回',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_transfer_no (transfer_no),
    INDEX idx_from_warehouse (from_warehouse_id),
    INDEX idx_to_warehouse (to_warehouse_id),
    INDEX idx_type (type),
    INDEX idx_status (approval_status)
) COMMENT '调拨单表';

-- ============================================================
-- 调拨明细
-- ============================================================
CREATE TABLE log_transfer_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transfer_id BIGINT NOT NULL COMMENT '调拨单ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    quantity DECIMAL(12,2) NOT NULL COMMENT '数量',
    batch_no VARCHAR(50) DEFAULT '' COMMENT '批次号',
    unit_value DECIMAL(12,2) DEFAULT 0.00 COMMENT '单价',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_transfer (transfer_id),
    INDEX idx_product (product_id)
) COMMENT '调拨明细表';

-- ============================================================
-- 调拨签收记录
-- ============================================================
CREATE TABLE log_transfer_sign (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transfer_id BIGINT NOT NULL COMMENT '调拨单ID',
    signer_id BIGINT NOT NULL COMMENT '签收人ID',
    sign_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '签收时间',
    sign_type VARCHAR(20) DEFAULT 'confirm' COMMENT '签收类型 confirm/reject',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_transfer (transfer_id),
    INDEX idx_signer (signer_id)
) COMMENT '调拨签收记录表';

-- ============================================================
-- 拣货单
-- ============================================================
CREATE TABLE log_picking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    picking_no VARCHAR(50) NOT NULL COMMENT '拣货单号',
    shipping_id BIGINT COMMENT '发货单ID',
    warehouse_id BIGINT NOT NULL COMMENT '仓库ID',
    operator_id BIGINT COMMENT '操作员ID',
    status INT DEFAULT 0 COMMENT '状态 0待拣货 1拣货中 2已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_picking_no (picking_no),
    INDEX idx_shipping (shipping_id),
    INDEX idx_warehouse (warehouse_id),
    INDEX idx_status (status)
) COMMENT '拣货单表';

-- ============================================================
-- 拣货明细
-- ============================================================
CREATE TABLE log_picking_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    picking_id BIGINT NOT NULL COMMENT '拣货单ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    batch_no VARCHAR(50) DEFAULT '' COMMENT '批次号',
    quantity DECIMAL(12,2) NOT NULL COMMENT '数量',
    location_code VARCHAR(50) DEFAULT '' COMMENT '库位编码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_picking (picking_id),
    INDEX idx_product (product_id)
) COMMENT '拣货明细表';

-- ============================================================
-- 发货单
-- ============================================================
CREATE TABLE log_shipping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipping_no VARCHAR(50) NOT NULL COMMENT '发货单号',
    sales_order_id BIGINT NOT NULL COMMENT '销售单ID',
    shipping_method VARCHAR(30) DEFAULT '' COMMENT '发货方式',
    logistics_info VARCHAR(500) DEFAULT '' COMMENT '物流信息',
    print_status INT DEFAULT 0 COMMENT '打印状态 0未打印 1已打印',
    status INT DEFAULT 0 COMMENT '状态 0待发货 1已发货 2已签收',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_shipping_no (shipping_no),
    INDEX idx_sales_order (sales_order_id),
    INDEX idx_status (status)
) COMMENT '发货单表';

-- ============================================================
-- 发货明细
-- ============================================================
CREATE TABLE log_shipping_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipping_id BIGINT NOT NULL COMMENT '发货单ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    quantity DECIMAL(12,2) NOT NULL COMMENT '数量',
    batch_no VARCHAR(50) DEFAULT '' COMMENT '批次号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_shipping (shipping_id),
    INDEX idx_product (product_id)
) COMMENT '发货明细表';

-- ============================================================
-- 物流质检记录
-- ============================================================
CREATE TABLE log_quality_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT COMMENT '库存ID',
    check_type VARCHAR(20) NOT NULL COMMENT '质检类型 production_in/transfer_in',
    aql_standard VARCHAR(50) DEFAULT '' COMMENT 'AQL标准',
    sample_quantity DECIMAL(12,2) DEFAULT 0.00 COMMENT '抽样数量',
    qualified_quantity DECIMAL(12,2) DEFAULT 0.00 COMMENT '合格数量',
    unqualified_quantity DECIMAL(12,2) DEFAULT 0.00 COMMENT '不合格数量',
    result VARCHAR(20) COMMENT '质检结果 qualified/unqualified',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    checker_id BIGINT COMMENT '质检人ID',
    check_time DATETIME COMMENT '质检时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_warehouse (warehouse_id),
    INDEX idx_check_type (check_type),
    INDEX idx_result (result)
) COMMENT '物流质检记录表';

-- ============================================================
-- 物流仓入库单
-- ============================================================
CREATE TABLE log_inbound (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inbound_no VARCHAR(50) NOT NULL COMMENT '入库单号',
    inbound_type VARCHAR(20) NOT NULL COMMENT '入库类型 production/transfer',
    transfer_id BIGINT COMMENT '关联调拨单ID',
    quality_check_id BIGINT COMMENT '质检单ID',
    status INT DEFAULT 0 COMMENT '状态 0待入库 1已入库 2已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_inbound_no (inbound_no),
    INDEX idx_transfer (transfer_id),
    INDEX idx_status (status)
) COMMENT '物流仓入库单表';

-- ============================================================
-- 物流仓入库单明细
-- ============================================================
CREATE TABLE log_inbound_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inbound_id BIGINT NOT NULL COMMENT '入库单ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    quantity DECIMAL(12,2) NOT NULL COMMENT '数量',
    batch_no VARCHAR(50) DEFAULT '' COMMENT '批次号',
    location_code VARCHAR(50) DEFAULT '' COMMENT '库位编码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_inbound (inbound_id),
    INDEX idx_product (product_id)
) COMMENT '物流仓入库单明细表';

-- ============================================================
-- 物流仓出库单
-- ============================================================
CREATE TABLE log_outbound (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    outbound_no VARCHAR(50) NOT NULL COMMENT '出库单号',
    outbound_type VARCHAR(20) NOT NULL COMMENT '出库类型 shipping',
    shipping_id BIGINT COMMENT '关联发货单ID',
    status INT DEFAULT 0 COMMENT '状态 0待出库 1已出库 2已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_outbound_no (outbound_no),
    INDEX idx_shipping (shipping_id),
    INDEX idx_status (status)
) COMMENT '物流仓出库单表';

-- ============================================================
-- 物流仓出库单明细
-- ============================================================
CREATE TABLE log_outbound_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    outbound_id BIGINT NOT NULL COMMENT '出库单ID',
    warehouse_id BIGINT NOT NULL COMMENT '库存ID',
    quantity DECIMAL(12,2) NOT NULL COMMENT '数量',
    batch_no VARCHAR(50) DEFAULT '' COMMENT '批次号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_outbound (outbound_id),
    INDEX idx_warehouse (warehouse_id)
) COMMENT '物流仓出库单明细表';

-- ============================================================
-- 权限授予（三账户模型）
-- ============================================================
GRANT ALL PRIVILEGES ON unified_logistics_db.* TO 'unified_dev'@'localhost';
GRANT SELECT, INSERT, UPDATE ON unified_logistics_db.* TO 'unified_ops'@'localhost';
GRANT SELECT, INSERT, UPDATE ON unified_logistics_db.* TO 'unified_app'@'localhost';
