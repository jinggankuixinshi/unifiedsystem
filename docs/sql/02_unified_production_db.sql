DROP DATABASE IF EXISTS unified_production_db;
CREATE DATABASE unified_production_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE unified_production_db;

SET NAMES utf8mb4;

-- ============================================================
-- 产成品表
-- ============================================================
CREATE TABLE prod_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL COMMENT '产品编码',
    product_name VARCHAR(100) NOT NULL COMMENT '产品名称',
    specification VARCHAR(100) DEFAULT '' COMMENT '规格',
    unit VARCHAR(20) DEFAULT '' COMMENT '单位',
    base_price DECIMAL(12,2) DEFAULT 0.00 COMMENT '基准售价',
    status TINYINT DEFAULT 1 COMMENT '状态 1启用0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_product_code (product_code),
    INDEX idx_name (product_name)
) COMMENT '产成品表';

-- ============================================================
-- 原材料/物料表
-- ============================================================
CREATE TABLE prod_material (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    material_code VARCHAR(50) NOT NULL COMMENT '物料编码',
    material_name VARCHAR(100) NOT NULL COMMENT '物料名称',
    specification VARCHAR(100) DEFAULT '' COMMENT '规格',
    unit VARCHAR(20) DEFAULT '' COMMENT '单位',
    safety_stock DECIMAL(12,2) DEFAULT 0.00 COMMENT '安全库存',
    status TINYINT DEFAULT 1 COMMENT '状态 1启用0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_material_code (material_code),
    INDEX idx_name (material_name)
) COMMENT '原材料表';

-- ============================================================
-- BOM表
-- ============================================================
CREATE TABLE prod_bom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL COMMENT '产品ID',
    version VARCHAR(20) NOT NULL COMMENT '版本号',
    effective_date DATE COMMENT '生效日期',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_product (product_id),
    INDEX idx_version (product_id, version)
) COMMENT '物料清单表';

-- ============================================================
-- BOM明细
-- ============================================================
CREATE TABLE prod_bom_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bom_id BIGINT NOT NULL COMMENT 'BOM ID',
    material_id BIGINT NOT NULL COMMENT '物料ID',
    quantity DECIMAL(12,2) NOT NULL COMMENT '用量',
    unit VARCHAR(20) DEFAULT '' COMMENT '单位',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_bom (bom_id),
    INDEX idx_material (material_id)
) COMMENT 'BOM明细表';

-- ============================================================
-- 生产计划
-- ============================================================
CREATE TABLE prod_production_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_no VARCHAR(50) NOT NULL COMMENT '计划编号',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    plan_quantity DECIMAL(12,2) NOT NULL COMMENT '计划数量',
    plan_date DATE COMMENT '计划日期',
    status INT DEFAULT 0 COMMENT '状态 0待排期 1已排期 2生产中 3已完成 4已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_plan_no (plan_no),
    INDEX idx_product (product_id),
    INDEX idx_status (status)
) COMMENT '生产计划表';

-- ============================================================
-- 排期表
-- ============================================================
CREATE TABLE prod_production_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL COMMENT '计划ID',
    production_line VARCHAR(50) DEFAULT '' COMMENT '产线',
    team VARCHAR(50) DEFAULT '' COMMENT '班组/人员',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    status INT DEFAULT 0 COMMENT '状态 0待开工 1生产中 2已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_plan (plan_id),
    INDEX idx_status (status),
    INDEX idx_time_range (start_time, end_time)
) COMMENT '排期表';

-- ============================================================
-- 工单
-- ============================================================
CREATE TABLE prod_work_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(50) NOT NULL COMMENT '工单号',
    schedule_id BIGINT NOT NULL COMMENT '排期ID',
    start_time DATETIME COMMENT '开工时间',
    end_time DATETIME COMMENT '完工时间',
    planned_output DECIMAL(12,2) DEFAULT 0.00 COMMENT '计划产出',
    actual_output DECIMAL(12,2) DEFAULT 0.00 COMMENT '实际产出',
    status INT DEFAULT 0 COMMENT '状态 0待开工 1生产中 2已完成 3异常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_schedule (schedule_id),
    INDEX idx_status (status)
) COMMENT '工单表';

-- ============================================================
-- 采购申请单
-- ============================================================
CREATE TABLE prod_purchase_request (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_no VARCHAR(50) NOT NULL COMMENT '申请编号',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    dept_id BIGINT COMMENT '部门ID',
    total_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '总金额',
    reason VARCHAR(500) DEFAULT '' COMMENT '申请原因',
    approval_status INT DEFAULT 0 COMMENT '审批状态 0待审 1通过 2驳回',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_request_no (request_no),
    INDEX idx_applicant (applicant_id),
    INDEX idx_status (approval_status)
) COMMENT '采购申请单表';

-- ============================================================
-- 采购申请明细
-- ============================================================
CREATE TABLE prod_purchase_request_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT NOT NULL COMMENT '申请单ID',
    material_id BIGINT NOT NULL COMMENT '物料ID',
    quantity DECIMAL(12,2) NOT NULL COMMENT '数量',
    unit_price DECIMAL(12,2) DEFAULT 0.00 COMMENT '单价',
    amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_request (request_id),
    INDEX idx_material (material_id)
) COMMENT '采购申请明细表';

-- ============================================================
-- 生产仓库存
-- ============================================================
CREATE TABLE prod_warehouse (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    material_product_id BIGINT NOT NULL COMMENT '物料/产品ID',
    item_type INT NOT NULL COMMENT '类型 1物料 2产品',
    batch_no VARCHAR(50) NOT NULL COMMENT '批次号',
    production_date DATE COMMENT '生产日期',
    quantity DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT '库存数量',
    location_code VARCHAR(50) DEFAULT '' COMMENT '库位编码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_item (material_product_id, item_type),
    INDEX idx_batch (batch_no),
    INDEX idx_location (location_code)
) COMMENT '生产仓库存表';

-- ============================================================
-- 生产仓出入库流水
-- ============================================================
CREATE TABLE prod_warehouse_log (
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
) COMMENT '生产仓出入库流水表';

-- ============================================================
-- 质检记录
-- ============================================================
CREATE TABLE prod_quality_check (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    warehouse_id BIGINT COMMENT '库存ID',
    check_type VARCHAR(20) NOT NULL COMMENT '质检类型 raw_material_in/production_in',
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
) COMMENT '质检记录表';

-- ============================================================
-- 入库单
-- ============================================================
CREATE TABLE prod_inbound (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inbound_no VARCHAR(50) NOT NULL COMMENT '入库单号',
    inbound_type VARCHAR(20) NOT NULL COMMENT '入库类型 purchase/production',
    supplier_id BIGINT COMMENT '供应商ID',
    quality_check_id BIGINT COMMENT '质检单ID',
    status INT DEFAULT 0 COMMENT '状态 0待入库 1已入库 2已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_inbound_no (inbound_no),
    INDEX idx_type (inbound_type),
    INDEX idx_status (status)
) COMMENT '入库单表';

-- ============================================================
-- 入库单明细
-- ============================================================
CREATE TABLE prod_inbound_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    inbound_id BIGINT NOT NULL COMMENT '入库单ID',
    material_product_id BIGINT NOT NULL COMMENT '物料/产品ID',
    item_type INT NOT NULL COMMENT '类型 1物料 2产品',
    quantity DECIMAL(12,2) NOT NULL COMMENT '数量',
    batch_no VARCHAR(50) DEFAULT '' COMMENT '批次号',
    location_code VARCHAR(50) DEFAULT '' COMMENT '库位编码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_inbound (inbound_id),
    INDEX idx_item (material_product_id, item_type)
) COMMENT '入库单明细表';

-- ============================================================
-- 出库单
-- ============================================================
CREATE TABLE prod_outbound (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    outbound_no VARCHAR(50) NOT NULL COMMENT '出库单号',
    outbound_type VARCHAR(20) NOT NULL COMMENT '出库类型 production_pick/transfer_out',
    business_id BIGINT COMMENT '关联业务单据ID',
    business_no VARCHAR(50) COMMENT '关联业务单号',
    status INT DEFAULT 0 COMMENT '状态 0待出库 1已出库 2已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_outbound_no (outbound_no),
    INDEX idx_type (outbound_type),
    INDEX idx_status (status)
) COMMENT '出库单表';

-- ============================================================
-- 出库单明细
-- ============================================================
CREATE TABLE prod_outbound_item (
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
) COMMENT '出库单明细表';

-- ============================================================
-- 权限授予（三账户模型）
-- ============================================================
GRANT ALL PRIVILEGES ON unified_production_db.* TO 'unified_dev'@'localhost';
GRANT SELECT, INSERT, UPDATE ON unified_production_db.* TO 'unified_ops'@'localhost';
GRANT SELECT, INSERT, UPDATE ON unified_production_db.* TO 'unified_app'@'localhost';
