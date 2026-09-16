DROP DATABASE IF EXISTS unified_finance_db;
CREATE DATABASE unified_finance_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE unified_finance_db;

SET NAMES utf8mb4;

-- ============================================================
-- 账户表
-- ============================================================
CREATE TABLE fin_account (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_name VARCHAR(100) NOT NULL COMMENT '账户名称',
    account_no VARCHAR(50) NOT NULL COMMENT '账号',
    bank_name VARCHAR(100) DEFAULT '' COMMENT '开户银行',
    account_type VARCHAR(20) NOT NULL COMMENT '账户类型 bank/cash/other',
    balance DECIMAL(12,2) DEFAULT 0.00 COMMENT '余额',
    status TINYINT DEFAULT 1 COMMENT '状态 1启用0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_account_no (account_no),
    INDEX idx_type (account_type)
) COMMENT '账户表';

-- ============================================================
-- 会计科目表
-- ============================================================
CREATE TABLE fin_account_subject (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_code VARCHAR(50) NOT NULL COMMENT '科目编码',
    subject_name VARCHAR(100) NOT NULL COMMENT '科目名称',
    type VARCHAR(20) NOT NULL COMMENT '类型 asset/liability/equity/revenue/expense',
    parent_id BIGINT DEFAULT 0 COMMENT '父级科目ID',
    level INT DEFAULT 1 COMMENT '级次',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_subject_code (subject_code),
    INDEX idx_type (type),
    INDEX idx_parent (parent_id)
) COMMENT '会计科目表';

INSERT INTO fin_account_subject (id, subject_code, subject_name, type, parent_id, level) VALUES
(1, '1000', '资产类', 'asset', 0, 1),
(2, '1001', '库存现金', 'asset', 1, 2),
(3, '1002', '银行存款', 'asset', 1, 2),
(4, '1122', '应收账款', 'asset', 1, 2),
(5, '1403', '原材料', 'asset', 1, 2),
(6, '1405', '库存商品', 'asset', 1, 2),
(7, '2000', '负债类', 'liability', 0, 1),
(8, '2001', '短期借款', 'liability', 7, 2),
(9, '2202', '应付账款', 'liability', 7, 2),
(10, '2211', '应付职工薪酬', 'liability', 7, 2),
(11, '2221', '应交税费', 'liability', 7, 2),
(12, '3000', '权益类', 'equity', 0, 1),
(13, '3001', '实收资本', 'equity', 12, 2),
(14, '3101', '未分配利润', 'equity', 12, 2),
(15, '4000', '收入类', 'revenue', 0, 1),
(16, '4001', '主营业务收入', 'revenue', 15, 2),
(17, '5000', '费用类', 'expense', 0, 1),
(18, '5001', '主营业务成本', 'expense', 17, 2),
(19, '5002', '管理费用', 'expense', 17, 2),
(20, '5003', '销售费用', 'expense', 17, 2),
(21, '5004', '财务费用', 'expense', 17, 2);

-- ============================================================
-- 记账凭证
-- ============================================================
CREATE TABLE fin_voucher (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    voucher_no VARCHAR(50) NOT NULL COMMENT '凭证号',
    voucher_date DATE NOT NULL COMMENT '凭证日期',
    summary VARCHAR(500) DEFAULT '' COMMENT '摘要',
    creator_id BIGINT COMMENT '制单人ID',
    reviewer_id BIGINT COMMENT '审核人ID',
    status INT DEFAULT 0 COMMENT '状态 0草稿 1已审核 2已过账',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_voucher_no (voucher_no),
    INDEX idx_date (voucher_date),
    INDEX idx_status (status)
) COMMENT '记账凭证表';

-- ============================================================
-- 凭证分录
-- ============================================================
CREATE TABLE fin_voucher_entry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    voucher_id BIGINT NOT NULL COMMENT '凭证ID',
    subject_id BIGINT NOT NULL COMMENT '科目ID',
    debit_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '借方金额',
    credit_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '贷方金额',
    summary VARCHAR(500) DEFAULT '' COMMENT '摘要',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_voucher (voucher_id),
    INDEX idx_subject (subject_id)
) COMMENT '凭证分录表';

-- ============================================================
-- 应收账款
-- ============================================================
CREATE TABLE fin_receivable (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id BIGINT NOT NULL COMMENT '客户ID',
    sales_order_id BIGINT NOT NULL COMMENT '销售单ID',
    amount DECIMAL(12,2) NOT NULL COMMENT '应收金额',
    received_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '已收金额',
    balance DECIMAL(12,2) DEFAULT 0.00 COMMENT '余额',
    due_date DATE COMMENT '到期日期',
    status INT DEFAULT 0 COMMENT '状态 0未收款 1部分收款 2已结清 3逾期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_customer (customer_id),
    INDEX idx_sales_order (sales_order_id),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date)
) COMMENT '应收账款表';

-- ============================================================
-- 收款记录
-- ============================================================
CREATE TABLE fin_receivable_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receivable_id BIGINT NOT NULL COMMENT '应收款ID',
    amount DECIMAL(12,2) NOT NULL COMMENT '收款金额',
    payment_date DATE COMMENT '收款日期',
    payment_method VARCHAR(20) DEFAULT '' COMMENT '收款方式',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_receivable (receivable_id),
    INDEX idx_date (payment_date)
) COMMENT '收款记录表';

-- ============================================================
-- 应付账款
-- ============================================================
CREATE TABLE fin_payable (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    purchase_order_id BIGINT NOT NULL COMMENT '采购单ID',
    amount DECIMAL(12,2) NOT NULL COMMENT '应付金额',
    paid_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '已付金额',
    balance DECIMAL(12,2) DEFAULT 0.00 COMMENT '余额',
    due_date DATE COMMENT '到期日期',
    status INT DEFAULT 0 COMMENT '状态 0未付款 1部分付款 2已结清 3逾期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_supplier (supplier_id),
    INDEX idx_purchase_order (purchase_order_id),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date)
) COMMENT '应付账款表';

-- ============================================================
-- 付款记录
-- ============================================================
CREATE TABLE fin_payable_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    payable_id BIGINT NOT NULL COMMENT '应付款ID',
    amount DECIMAL(12,2) NOT NULL COMMENT '付款金额',
    payment_date DATE COMMENT '付款日期',
    payment_method VARCHAR(20) DEFAULT '' COMMENT '付款方式',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_payable (payable_id),
    INDEX idx_date (payment_date)
) COMMENT '付款记录表';

-- ============================================================
-- 费用报销单
-- ============================================================
CREATE TABLE fin_expense (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    expense_no VARCHAR(50) NOT NULL COMMENT '报销单号',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    dept_id BIGINT COMMENT '部门ID',
    expense_type VARCHAR(30) NOT NULL COMMENT '报销类型 travel/office/entertainment/other',
    total_amount DECIMAL(12,2) NOT NULL COMMENT '总金额',
    approval_status INT DEFAULT 0 COMMENT '审批状态 0待审 1通过 2驳回',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_expense_no (expense_no),
    INDEX idx_applicant (applicant_id),
    INDEX idx_status (approval_status),
    INDEX idx_type (expense_type)
) COMMENT '费用报销单表';

-- ============================================================
-- 报销明细
-- ============================================================
CREATE TABLE fin_expense_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    expense_id BIGINT NOT NULL COMMENT '报销单ID',
    item_name VARCHAR(100) NOT NULL COMMENT '费用项目',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_expense (expense_id)
) COMMENT '报销明细表';

-- ============================================================
-- 预算表
-- ============================================================
CREATE TABLE fin_budget (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    subject_id BIGINT NOT NULL COMMENT '科目ID',
    budget_year INT NOT NULL COMMENT '预算年度',
    budget_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '预算金额',
    executed_amount DECIMAL(12,2) DEFAULT 0.00 COMMENT '已执行金额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_dept_subject_year (dept_id, subject_id, budget_year),
    INDEX idx_year (budget_year)
) COMMENT '预算表';

-- ============================================================
-- 预算执行记录
-- ============================================================
CREATE TABLE fin_budget_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    budget_id BIGINT NOT NULL COMMENT '预算ID',
    amount DECIMAL(12,2) NOT NULL COMMENT '金额',
    business_id BIGINT COMMENT '业务单据ID',
    business_type VARCHAR(30) COMMENT '业务类型',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_budget (budget_id),
    INDEX idx_business (business_id, business_type)
) COMMENT '预算执行记录表';

-- ============================================================
-- 工资单
-- ============================================================
CREATE TABLE fin_salary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '员工ID',
    salary_month VARCHAR(7) NOT NULL COMMENT '工资月份 YYYY-MM',
    base_salary DECIMAL(10,2) DEFAULT 0.00 COMMENT '基本工资',
    performance_bonus DECIMAL(10,2) DEFAULT 0.00 COMMENT '绩效奖金',
    overtime_pay DECIMAL(10,2) DEFAULT 0.00 COMMENT '加班费',
    deduction DECIMAL(10,2) DEFAULT 0.00 COMMENT '扣款',
    social_insurance DECIMAL(10,2) DEFAULT 0.00 COMMENT '社保扣款',
    housing_fund DECIMAL(10,2) DEFAULT 0.00 COMMENT '公积金扣款',
    net_salary DECIMAL(10,2) DEFAULT 0.00 COMMENT '实发工资',
    status INT DEFAULT 0 COMMENT '状态 0草稿 1已审核 2已发放',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_user_month (user_id, salary_month),
    INDEX idx_status (status)
) COMMENT '工资单表';

-- ============================================================
-- 工资项目明细
-- ============================================================
CREATE TABLE fin_salary_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    salary_id BIGINT NOT NULL COMMENT '工资单ID',
    item_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    item_type VARCHAR(20) NOT NULL COMMENT '项目类型 income/deduction',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_salary (salary_id)
) COMMENT '工资项目明细表';

-- ============================================================
-- 工资计算模板
-- ============================================================
CREATE TABLE fin_salary_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL COMMENT '项目名称',
    item_type VARCHAR(20) NOT NULL COMMENT '项目类型 income/deduction',
    formula VARCHAR(500) DEFAULT '' COMMENT '计算公式',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_type (item_type)
) COMMENT '工资计算模板表';

-- ============================================================
-- 社保公积金配置
-- ============================================================
CREATE TABLE fin_social_insurance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    insurance_type VARCHAR(30) NOT NULL COMMENT '保险类型 pension/medical/unemployment/injury/maternity/housing_fund',
    company_rate DECIMAL(6,4) DEFAULT 0.0000 COMMENT '公司缴纳比例',
    personal_rate DECIMAL(6,4) DEFAULT 0.0000 COMMENT '个人缴纳比例',
    base_lower DECIMAL(10,2) DEFAULT 0.00 COMMENT '缴费基数下限',
    base_upper DECIMAL(10,2) DEFAULT 0.00 COMMENT '缴费基数上限',
    effective_date DATE COMMENT '生效日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_type (insurance_type),
    INDEX idx_date (effective_date)
) COMMENT '社保公积金配置表';

-- ============================================================
-- 资金日报
-- ============================================================
CREATE TABLE fin_daily_settlement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    settlement_date DATE NOT NULL COMMENT '结算日期',
    account_id BIGINT NOT NULL COMMENT '账户ID',
    opening_balance DECIMAL(12,2) DEFAULT 0.00 COMMENT '期初余额',
    total_income DECIMAL(12,2) DEFAULT 0.00 COMMENT '当日收入',
    total_expense DECIMAL(12,2) DEFAULT 0.00 COMMENT '当日支出',
    closing_balance DECIMAL(12,2) DEFAULT 0.00 COMMENT '期末余额',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_account_date (account_id, settlement_date),
    INDEX idx_date (settlement_date)
) COMMENT '资金日报表';

-- ============================================================
-- 供应商表
-- ============================================================
CREATE TABLE fin_supplier (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supplier_code VARCHAR(50) NOT NULL COMMENT '供应商编码',
    supplier_name VARCHAR(100) NOT NULL COMMENT '供应商名称',
    contact_person VARCHAR(50) DEFAULT '' COMMENT '联系人',
    phone VARCHAR(32) DEFAULT '' COMMENT '联系电话',
    bank_account VARCHAR(50) DEFAULT '' COMMENT '银行账号',
    bank_name VARCHAR(100) DEFAULT '' COMMENT '开户银行',
    status TINYINT DEFAULT 1 COMMENT '状态 1启用0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_supplier_code (supplier_code),
    INDEX idx_name (supplier_name)
) COMMENT '供应商表';

-- ============================================================
-- 权限授予（三账户模型）
-- ============================================================
GRANT ALL PRIVILEGES ON unified_finance_db.* TO 'unified_dev'@'localhost';
GRANT SELECT, INSERT, UPDATE ON unified_finance_db.* TO 'unified_ops'@'localhost';
GRANT SELECT, INSERT, UPDATE ON unified_finance_db.* TO 'unified_app'@'localhost';
