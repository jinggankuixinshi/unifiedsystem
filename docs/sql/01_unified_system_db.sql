DROP DATABASE IF EXISTS unified_system_db;
CREATE DATABASE unified_system_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE unified_system_db;

SET NAMES utf8mb4;

CREATE TABLE sys_department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dept_name VARCHAR(50) NOT NULL COMMENT '部门名称',
    dept_code VARCHAR(50) NOT NULL COMMENT '部门编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    ancestors VARCHAR(500) DEFAULT '' COMMENT '祖级列表',
    sort_order INT DEFAULT 0 COMMENT '排序',
    leader VARCHAR(32) DEFAULT '' COMMENT '负责人',
    phone VARCHAR(32) DEFAULT '' COMMENT '联系电话',
    status TINYINT DEFAULT 1 COMMENT '状态 1启用0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_dept_code (dept_code),
    INDEX idx_parent (parent_id)
) COMMENT '部门表';

INSERT INTO sys_department (id, dept_name, dept_code, parent_id, ancestors, sort_order, leader, status)
VALUES (1, '总公司', 'root', 0, '', 0, '老板', 1),
       (2, '生产部', 'production', 1, '0,1', 1, '生产主管', 1),
       (3, '物流部', 'logistics', 1, '0,1', 2, '物流经理', 1),
       (4, '销售部', 'sales', 1, '0,1', 3, '销售经理', 1),
       (5, '财务部', 'finance', 1, '0,1', 4, '财务经理', 1);

CREATE TABLE sys_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    description VARCHAR(255) DEFAULT '' COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_role_code (role_code)
) COMMENT '角色表';

INSERT INTO sys_role (id, role_name, role_code, description, sort_order) VALUES
(1, '系统管理员', 'admin', '系统最高权限', 0),
(2, '老板/总经理', 'boss', '终审审批', 1),
(3, '生产主管', 'prod_manager', '生产管理', 2),
(4, '生产工人', 'prod_worker', '生产执行', 3),
(5, '仓库管理员', 'warehouse_keeper', '仓库管理（生产仓）', 4),
(6, '物流操作员', 'logistics_operator', '物流操作', 5),
(7, '物流经理', 'logistics_manager', '物流管理', 6),
(8, '销售员', 'salesperson', '销售报单', 7),
(9, '销售经理', 'sales_manager', '销售管理', 8),
(10, '财务专员', 'finance_staff', '财务操作', 9),
(11, '财务经理', 'finance_manager', '财务管理', 10),
(12, '出纳', 'cashier', '付款打款', 11);

CREATE TABLE sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码 BCrypt',
    real_name VARCHAR(50) DEFAULT '' COMMENT '姓名',
    phone VARCHAR(32) DEFAULT '' COMMENT '手机号',
    email VARCHAR(100) DEFAULT '' COMMENT '邮箱',
    dept_id BIGINT COMMENT '部门ID',
    status TINYINT DEFAULT 1 COMMENT '状态 1正常0禁用',
    last_login_time DATETIME COMMENT '最后登录时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_username (username),
    INDEX idx_dept (dept_id)
) COMMENT '用户表';

-- 初始密码（账号+123，Spring BCrypt $2a$10$ 格式，首次登录后请修改）：
--   admin/admin123                boss/boss123
--   prodmanager/prodmanager123    prodworker/prodworker123     warehouse/warehouse123
--   logiop/logiop123              logimanager/logimanager123
--   sales/sales123                salesmanager/salesmanager123
--   finance/finance123            finmanager/finmanager123     cashier/cashier123
INSERT INTO sys_user (id, username, password, real_name, dept_id, status) VALUES
(1, 'admin', '$2a$10$l01cW8oc6mtprUNgeV7gZuz3MjYR4yDV47/l6KIxIWVgocCv3k42y', '系统管理员', 1, 1),
(2, 'boss', '$2a$10$t79wTdPdGD3z3lQRC.c58O0DnI7Fkq8auUlqe9Y7q25R8pTGW5NrK', '老板', 1, 1),
(3, 'prodmanager', '$2a$10$65waHHbHcuBH5dQK6eWbueEzlaMiyBrinv4uCenGEUFtJo3Z.V3ne', '生产主管', 2, 1),
(4, 'prodworker', '$2a$10$PUiWn8xYF98Ufp8f/5siEu2.ENkkvCEdOz09BRv3.46W1XiMd2cSO', '生产工人', 2, 1),
(5, 'warehouse', '$2a$10$/5M8H3H.McjAulXudJ4B0.tkPaJKfSeheVBTNKb3VNMK9A2E43gey', '仓管员', 2, 1),
(6, 'logiop', '$2a$10$QhWgB6t8OvR/cRYYYNtjeOo899kHIWu5bBUHajnom0W8hW0fpCvaq', '物流操作员', 3, 1),
(7, 'logimanager', '$2a$10$xLhcv3nwc1Kag13CyXTDWeREYwF3VTMZbtSeO8UTGzy2NoWEShZ4.', '物流经理', 3, 1),
(8, 'sales', '$2a$10$hFar4p.8MH.s63MweAjTI.fpQWugDp.VxvN7n1/P.8NtcCbas5sp.', '销售员', 4, 1),
(9, 'salesmanager', '$2a$10$JwLoEDJAsHLEBEaweBu3SelJ0nPaxrJaqkare9ywKhEmQBOECYEX.', '销售经理', 4, 1),
(10, 'finance', '$2a$10$HfkwSlsJiQnqhaTa1F/R7.5bZZTwSr9xIx3n2U/OI07wy.GZ15YN2', '财务专员', 5, 1),
(11, 'finmanager', '$2a$10$BPHUcvnK72ToBLfQJvgrg.Ms4yQ90VYdx6FclndM/N/PYMaR1G5Eu', '财务经理', 5, 1),
(12, 'cashier', '$2a$10$GEP6wVDdrnbZx47tCkzQFujcjmhwS2i2yltu0ouG9J0/NBtpmuch2', '出纳', 5, 1);

-- ============================================================
-- 用户-角色关联
-- ============================================================
CREATE TABLE sys_user_role (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) COMMENT '用户角色关联表';

INSERT INTO sys_user_role (user_id, role_id) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 6),
(7, 7),
(8, 8),
(9, 9),
(10, 10),
(11, 11),
(12, 12);

-- ============================================================
-- 资源权限表（菜单/按钮/API）
-- ============================================================
CREATE TABLE sys_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '名称',
    code VARCHAR(100) NOT NULL COMMENT '权限编码',
    type INT NOT NULL DEFAULT 1 COMMENT '类型 1菜单 2按钮 3接口',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    path VARCHAR(200) DEFAULT '' COMMENT '路由路径',
    component VARCHAR(200) DEFAULT '' COMMENT '组件路径',
    icon VARCHAR(50) DEFAULT '' COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_parent_id (parent_id),
    UNIQUE KEY uk_code (code)
) COMMENT '资源权限表';

INSERT INTO sys_resource (id, name, code, type, parent_id, path, component, icon, sort_order) VALUES
-- 一级菜单
(1, '系统管理', 'system', 1, 0, '/system', '', 'SettingOutlined', 1),
(2, '生产管理', 'production', 1, 0, '/production', '', 'AppstoreOutlined', 2),
(3, '物流管理', 'logistics', 1, 0, '/logistics', '', 'CarOutlined', 3),
(4, '销售管理', 'sales', 1, 0, '/sales', '', 'ShoppingOutlined', 4),
(5, '财务管理', 'finance', 1, 0, '/finance', '', 'DollarOutlined', 5),
(6, '实时看板', 'dashboard', 1, 0, '/dashboard', '', 'DashboardOutlined', 6),
(7, '考勤管理', 'attendance', 1, 0, '/attendance', '', 'ClockCircleOutlined', 7),
-- 系统管理子菜单
(11, '用户管理', 'system:user', 1, 1, '/system/user', 'system/user/index', '', 1),
(12, '部门管理', 'system:dept', 1, 1, '/system/dept', 'system/dept/index', '', 2),
(13, '角色管理', 'system:role', 1, 1, '/system/role', 'system/role/index', '', 3),
(14, '资源管理', 'system:resource', 1, 1, '/system/resource', 'system/resource/index', '', 4),
(15, '字典管理', 'system:dict', 1, 1, '/system/dict', 'system/dict/index', '', 5),
(16, '消息中心', 'system:message', 1, 1, '/system/message', 'system/message/index', '', 6),
(17, '操作日志', 'system:operlog', 1, 1, '/system/operlog', 'system/operlog/index', '', 7),
(18, '审计日志', 'system:auditlog', 1, 1, '/system/auditlog', 'system/auditlog/index', '', 8),
-- 生产管理子菜单
(21, '产品管理', 'production:product', 1, 2, '/production/product', 'production/product/index', '', 1),
(22, '物料管理', 'production:material', 1, 2, '/production/material', 'production/material/index', '', 2),
(23, 'BOM管理', 'production:bom', 1, 2, '/production/bom', 'production/bom/index', '', 3),
(24, '生产计划', 'production:plan', 1, 2, '/production/plan', 'production/plan/index', '', 4),
(25, '排期管理', 'production:schedule', 1, 2, '/production/schedule', 'production/schedule/index', '', 5),
(26, '工单管理', 'production:workorder', 1, 2, '/production/workorder', 'production/workorder/index', '', 6),
(27, '采购申请', 'production:purchase', 1, 2, '/production/purchase', 'production/purchase/index', '', 7),
(28, '生产仓管理', 'production:warehouse', 1, 2, '/production/warehouse', 'production/warehouse/index', '', 8),
(29, '质检管理', 'production:quality', 1, 2, '/production/quality', 'production/quality/index', '', 9),
(30, '入库管理', 'production:inbound', 1, 2, '/production/inbound', 'production/inbound/index', '', 10),
(31, '出库管理', 'production:outbound', 1, 2, '/production/outbound', 'production/outbound/index', '', 11),
-- 物流管理子菜单
(41, '物流仓管理', 'logistics:warehouse', 1, 3, '/logistics/warehouse', 'logistics/warehouse/index', '', 1),
(42, '调拨管理', 'logistics:transfer', 1, 3, '/logistics/transfer', 'logistics/transfer/index', '', 2),
(43, '拣货管理', 'logistics:picking', 1, 3, '/logistics/picking', 'logistics/picking/index', '', 3),
(44, '发货管理', 'logistics:shipping', 1, 3, '/logistics/shipping', 'logistics/shipping/index', '', 4),
(45, '物流质检', 'logistics:quality', 1, 3, '/logistics/quality', 'logistics/quality/index', '', 5),
(46, '入库管理', 'logistics:inbound', 1, 3, '/logistics/inbound', 'logistics/inbound/index', '', 6),
(47, '出库管理', 'logistics:outbound', 1, 3, '/logistics/outbound', 'logistics/outbound/index', '', 7),
-- 销售管理子菜单
(51, '客户管理', 'sales:customer', 1, 4, '/sales/customer', 'sales/customer/index', '', 1),
(52, '销售报单', 'sales:order', 1, 4, '/sales/order', 'sales/order/index', '', 2),
(53, '价格管理', 'sales:price', 1, 4, '/sales/price', 'sales/price/index', '', 3),
(54, '合同管理', 'sales:contract', 1, 4, '/sales/contract', 'sales/contract/index', '', 4),
(55, '售后服务', 'sales:aftersale', 1, 4, '/sales/aftersale', 'sales/aftersale/index', '', 5),
-- 财务管理子菜单
(61, '账户管理', 'finance:account', 1, 5, '/finance/account', 'finance/account/index', '', 1),
(62, '会计科目', 'finance:subject', 1, 5, '/finance/subject', 'finance/subject/index', '', 2),
(63, '记账凭证', 'finance:voucher', 1, 5, '/finance/voucher', 'finance/voucher/index', '', 3),
(64, '应收账款', 'finance:receivable', 1, 5, '/finance/receivable', 'finance/receivable/index', '', 4),
(65, '应付账款', 'finance:payable', 1, 5, '/finance/payable', 'finance/payable/index', '', 5),
(66, '费用报销', 'finance:expense', 1, 5, '/finance/expense', 'finance/expense/index', '', 6),
(67, '预算管理', 'finance:budget', 1, 5, '/finance/budget', 'finance/budget/index', '', 7),
(68, '工资管理', 'finance:salary', 1, 5, '/finance/salary', 'finance/salary/index', '', 8),
(69, '供应商管理', 'finance:supplier', 1, 5, '/finance/supplier', 'finance/supplier/index', '', 9),
(70, '资金日报', 'finance:settlement', 1, 5, '/finance/settlement', 'finance/settlement/index', '', 10),
-- 实时看板子菜单
(81, '销售看板', 'dashboard:sales', 1, 6, '/dashboard/sales', 'dashboard/sales/index', '', 1),
(82, '库存看板', 'dashboard:inventory', 1, 6, '/dashboard/inventory', 'dashboard/inventory/index', '', 2),
(83, '应收应付看板', 'dashboard:receivable', 1, 6, '/dashboard/receivable', 'dashboard/receivable/index', '', 3),
(84, '资金看板', 'dashboard:capital', 1, 6, '/dashboard/capital', 'dashboard/capital/index', '', 4),
(85, '生产看板', 'dashboard:production', 1, 6, '/dashboard/production', 'dashboard/production/index', '', 5),
(86, '审批看板', 'dashboard:approval', 1, 6, '/dashboard/approval', 'dashboard/approval/index', '', 6),
-- 考勤管理子菜单
(91, '考勤记录', 'attendance:record', 1, 7, '/attendance/record', 'attendance/record/index', '', 1),
(92, '请假管理', 'attendance:leave', 1, 7, '/attendance/leave', 'attendance/leave/index', '', 2),
(93, '加班管理', 'attendance:overtime', 1, 7, '/attendance/overtime', 'attendance/overtime/index', '', 3),
(94, '考勤规则', 'attendance:schedule', 1, 7, '/attendance/schedule', 'attendance/schedule/index', '', 4);

-- ============================================================
-- 角色-资源关联（admin角色拥有全部资源）
-- ============================================================
CREATE TABLE sys_role_resource (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_role_id (role_id),
    INDEX idx_resource_id (resource_id)
) COMMENT '角色资源关联表';

INSERT INTO sys_role_resource (role_id, resource_id)
SELECT 1, id FROM sys_resource;

-- ============================================================
-- 资源-部门可见性（部门级数据权限）
-- ============================================================
CREATE TABLE sys_resource_dept (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL COMMENT '资源ID',
    dept_id BIGINT NOT NULL COMMENT '部门ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_resource (resource_id),
    INDEX idx_dept (dept_id)
) COMMENT '资源部门可见性表';

-- ============================================================
-- 站内消息
-- ============================================================
CREATE TABLE sys_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT COMMENT '内容',
    sender_id BIGINT COMMENT '发送人ID',
    receiver_id BIGINT NOT NULL COMMENT '接收人ID',
    is_read TINYINT DEFAULT 0 COMMENT '已读 1是0否',
    read_time DATETIME COMMENT '阅读时间',
    business_type VARCHAR(50) COMMENT '业务类型',
    business_id BIGINT COMMENT '业务ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_receiver (receiver_id),
    INDEX idx_is_read (is_read),
    INDEX idx_business (business_type, business_id)
) COMMENT '站内消息表';

-- ============================================================
-- 字典类型
-- ============================================================
CREATE TABLE sys_dict_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型',
    status TINYINT DEFAULT 1 COMMENT '状态',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_dict_type (dict_type)
) COMMENT '字典类型表';

INSERT INTO sys_dict_type (id, dict_name, dict_type, remark) VALUES
(1, '系统开关', 'sys_normal_disable', '系统通用开关状态'),
(2, '是否', 'sys_yes_no', '通用是/否字典'),
(3, '报销类型', 'expense_type', '费用报销类型'),
(4, '调拨类型', 'transfer_type', '调拨业务类型');

-- ============================================================
-- 字典数据
-- ============================================================
CREATE TABLE sys_dict_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型',
    dict_label VARCHAR(100) NOT NULL COMMENT '标签',
    dict_value VARCHAR(100) NOT NULL COMMENT '值',
    sort_order INT DEFAULT 0 COMMENT '排序',
    css_class VARCHAR(50) DEFAULT '' COMMENT '样式类名',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_dict_type (dict_type)
) COMMENT '字典数据表';

INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort_order, css_class) VALUES
('sys_normal_disable', '正常', '1', 1, 'success'),
('sys_normal_disable', '停用', '0', 2, 'danger'),
('sys_yes_no', '是', 'Y', 1, 'success'),
('sys_yes_no', '否', 'N', 2, 'danger'),
('expense_type', '差旅费', 'travel', 1, ''),
('expense_type', '办公费', 'office', 2, ''),
('expense_type', '招待费', 'entertainment', 3, ''),
('expense_type', '其他', 'other', 4, ''),
('transfer_type', '常规调拨', 'normal', 1, ''),
('transfer_type', '样品调拨', 'sample', 2, ''),
('transfer_type', '报废调拨', 'scrap', 3, ''),
('transfer_type', '返修调拨', 'repair', 4, ''),
('transfer_type', '赠送调拨', 'gift', 5, '');

-- ============================================================
-- 操作审计日志
-- ============================================================
CREATE TABLE sys_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    module VARCHAR(50) COMMENT '模块',
    operation VARCHAR(50) COMMENT '操作类型',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    duration BIGINT DEFAULT 0 COMMENT '耗时(ms)',
    ip VARCHAR(50) DEFAULT '' COMMENT 'IP地址',
    result VARCHAR(50) DEFAULT '' COMMENT '操作结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_module (module)
) COMMENT '审计日志表';

-- ============================================================
-- 操作日志
-- ============================================================
CREATE TABLE sys_operate_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    module VARCHAR(50) COMMENT '模块',
    operation VARCHAR(50) COMMENT '操作类型',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    duration BIGINT DEFAULT 0 COMMENT '耗时(ms)',
    ip VARCHAR(50) DEFAULT '' COMMENT 'IP地址',
    result VARCHAR(50) DEFAULT '' COMMENT '操作结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT '操作日志表';

-- ============================================================
-- 业务序列号
-- ============================================================
CREATE TABLE sys_sequence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    prefix VARCHAR(10) NOT NULL COMMENT '单号前缀',
    current_seq INT NOT NULL DEFAULT 1 COMMENT '当前序列号',
    update_date DATE COMMENT '更新日期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_prefix_date (prefix, update_date)
) COMMENT '业务序列号表';

-- ============================================================
-- 审批工作流模板
-- ============================================================
CREATE TABLE wf_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
    description VARCHAR(500) DEFAULT '' COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态',
    version INT DEFAULT 1 COMMENT '版本号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    UNIQUE KEY uk_business_type (business_type),
    INDEX idx_status (status)
) COMMENT '审批流程模板表';

INSERT INTO wf_template (id, template_name, business_type, description, version) VALUES
(1, '采购审批流程', 'purchase_request', '生产采购申请审批流程，按金额分级', 1),
(2, '销售报单审批流程', 'sales_order', '销售报单审批流程，含价格异常检测分级', 1),
(3, '调拨审批流程', 'transfer', '物流调拨审批流程，按调拨类型和价值分级', 1),
(4, '费用报销审批流程', 'expense', '费用报销审批流程，按金额分级', 1),
(5, '请假审批流程', 'leave', '员工请假审批流程', 1),
(6, '加班审批流程', 'overtime', '员工加班审批流程', 1);

-- ============================================================
-- 审批节点模板
-- ============================================================
CREATE TABLE wf_node_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL COMMENT '模板ID',
    node_order INT NOT NULL COMMENT '审批顺序',
    approver_type VARCHAR(32) NOT NULL COMMENT '审批人类型 DEPT_MANAGER/ROLE/SPECIFIC_USER',
    approver_id BIGINT COMMENT '审批人/角色ID',
    condition_type VARCHAR(32) DEFAULT 'NONE' COMMENT '条件类型 NONE/AMOUNT_RANGE/PERCENTAGE',
    condition_config TEXT COMMENT '条件配置JSON',
    node_name VARCHAR(100) NOT NULL COMMENT '节点名称',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_template (template_id),
    INDEX idx_node_order (template_id, node_order)
) COMMENT '审批节点模板表';

-- 采购审批节点 (template_id=1, 按金额分级)
-- 小额 <5000: 财务专员
INSERT INTO wf_node_template (template_id, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(1, 1, 'ROLE', 10, 'AMOUNT_RANGE', '{"max":5000}', '财务专员审批');
-- 中额 5000-50000: 财务专员 → 财务经理
INSERT INTO wf_node_template (template_id, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(1, 1, 'ROLE', 10, 'AMOUNT_RANGE', '{"min":5000,"max":50000}', '财务专员审批'),
(1, 2, 'ROLE', 11, 'NONE', '{}', '财务经理审批');
-- 大额 50000-200000: 财务专员 → 财务经理
INSERT INTO wf_node_template (template_id, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(1, 1, 'ROLE', 10, 'AMOUNT_RANGE', '{"min":50000,"max":200000}', '财务专员审批'),
(1, 2, 'ROLE', 11, 'NONE', '{}', '财务经理审批');
-- 特大额 >200000: 财务专员 → 财务经理(前置) → 老板(终审)
INSERT INTO wf_node_template (template_id, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(1, 1, 'ROLE', 10, 'AMOUNT_RANGE', '{"min":200000}', '财务专员审批'),
(1, 2, 'ROLE', 11, 'NONE', '{}', '财务经理前置审批'),
(1, 3, 'ROLE', 2, 'NONE', '{}', '老板终审');

-- 销售报单审批节点 (template_id=2, 按价格偏离度分级)
-- 正常价格 ≥85%: 财务专员
INSERT INTO wf_node_template (template_id, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(2, 1, 'ROLE', 10, 'PERCENTAGE', '{"min":0.85}', '财务专员审批');
-- 轻度异常 <85%: 系统打标签, 财务专员
INSERT INTO wf_node_template (template_id, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(2, 1, 'ROLE', 10, 'PERCENTAGE', '{"min":0.70,"max":0.85}', '财务专员审批(系统打标签)');
-- 中度异常 <70%: 强制低价特批理由, 财务专员
INSERT INTO wf_node_template (template_id, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(2, 1, 'ROLE', 10, 'PERCENTAGE', '{"min":0.50,"max":0.70}', '财务专员审批(强制低价理由)');
-- 重度异常 <50%: 财务专员 → 老板终审
INSERT INTO wf_node_template (template_id, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(2, 1, 'ROLE', 10, 'PERCENTAGE', '{"max":0.50}', '财务专员审批'),
(2, 2, 'ROLE', 2, 'NONE', '{}', '老板终审');

-- 调拨审批节点 (template_id=3)
-- 常规调拨小额: 调入仓主管 → 调出仓主管
INSERT INTO wf_node_template (template_id, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(3, 1, 'ROLE', 7, 'AMOUNT_RANGE', '{"max":50000}', '调入仓主管审核'),
(3, 2, 'ROLE', 7, 'NONE', '{}', '调出仓主管审批');
-- 常规调拨大额: 调入仓主管 → 调出仓主管 → 财务复核
INSERT INTO wf_node_template (template_id, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(3, 1, 'ROLE', 7, 'AMOUNT_RANGE', '{"min":50000}', '调入仓主管审核'),
(3, 2, 'ROLE', 7, 'NONE', '{}', '调出仓主管审批'),
(3, 3, 'ROLE', 11, 'NONE', '{}', '财务复核');
-- 特殊调拨(样品/报废/返修/赠送): 仓主管 → 财务 → 老板
INSERT INTO wf_node_template (template_id, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(3, 1, 'ROLE', 7, 'NONE', '{}', '仓主管审批'),
(3, 2, 'ROLE', 11, 'NONE', '{}', '财务复核'),
(3, 3, 'ROLE', 2, 'NONE', '{}', '老板终审');

-- 费用报销审批节点 (template_id=4, 按金额分级)
-- 小额: 部门经理 → 财务专员 → 出纳打款
INSERT INTO wf_node_template (template_id, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(4, 1, 'DEPT_MANAGER', NULL, 'AMOUNT_RANGE', '{"max":5000}', '部门经理审批'),
(4, 2, 'ROLE', 10, 'NONE', '{}', '财务专员审批'),
(4, 3, 'ROLE', 12, 'NONE', '{}', '出纳打款');
-- 中额 5000-20000: 部门经理 → 财务专员 → 财务经理 → 出纳打款
INSERT INTO wf_node_template (template_id, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(4, 1, 'DEPT_MANAGER', NULL, 'AMOUNT_RANGE', '{"min":5000,"max":20000}', '部门经理审批'),
(4, 2, 'ROLE', 10, 'NONE', '{}', '财务专员审批'),
(4, 3, 'ROLE', 11, 'NONE', '{}', '财务经理审批'),
(4, 4, 'ROLE', 12, 'NONE', '{}', '出纳打款');
-- 大额 >20000: 部门经理 → 财务专员 → 财务经理 → 老板 → 出纳打款
INSERT INTO wf_node_template (template_id, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(4, 1, 'DEPT_MANAGER', NULL, 'AMOUNT_RANGE', '{"min":20000}', '部门经理审批'),
(4, 2, 'ROLE', 10, 'NONE', '{}', '财务专员审批'),
(4, 3, 'ROLE', 11, 'NONE', '{}', '财务经理审批'),
(4, 4, 'ROLE', 2, 'NONE', '{}', '老板审批'),
(4, 5, 'ROLE', 12, 'NONE', '{}', '出纳打款');

-- 请假审批节点 (template_id=5): 部门经理审批
INSERT INTO wf_node_template (template_id, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(5, 1, 'DEPT_MANAGER', NULL, 'NONE', '{}', '部门经理审批');

-- 加班审批节点 (template_id=6): 部门经理审批
INSERT INTO wf_node_template (template_id, node_order, approver_type, approver_id, condition_type, condition_config, node_name) VALUES
(6, 1, 'DEPT_MANAGER', NULL, 'NONE', '{}', '部门经理审批');

-- ============================================================
-- 审批流程实例
-- ============================================================
CREATE TABLE wf_instance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id BIGINT NOT NULL COMMENT '模板ID',
    business_type VARCHAR(50) NOT NULL COMMENT '业务类型',
    business_id BIGINT NOT NULL COMMENT '业务单据ID',
    current_node_order INT DEFAULT 1 COMMENT '当前审批节点顺序',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态 pending/approved/rejected/recalled',
    approval_time DATETIME COMMENT '审批完成时间',
    result VARCHAR(20) COMMENT '审批结果',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_template (template_id),
    INDEX idx_business (business_type, business_id),
    INDEX idx_status (status)
) COMMENT '审批流程实例表';

-- ============================================================
-- 审批记录
-- ============================================================
CREATE TABLE wf_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    instance_id BIGINT NOT NULL COMMENT '流程实例ID',
    node_order INT NOT NULL COMMENT '审批节点顺序',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    approver_action VARCHAR(20) NOT NULL COMMENT '操作 APPROVE/REJECT/PUSH_UP/DELEGATE',
    comment VARCHAR(500) DEFAULT '' COMMENT '审批意见',
    action_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_instance (instance_id),
    INDEX idx_approver (approver_id)
) COMMENT '审批记录表';

-- ============================================================
-- 考勤记录
-- ============================================================
CREATE TABLE att_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    login_time DATETIME COMMENT '登录时间(上班打卡)',
    logout_time DATETIME COMMENT '登出时间(下班打卡)',
    status VARCHAR(20) DEFAULT 'normal' COMMENT '考勤状态 normal/late/early/absent',
    remark VARCHAR(500) DEFAULT '' COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time)
) COMMENT '考勤记录表';

-- ============================================================
-- 请假记录
-- ============================================================
CREATE TABLE att_leave (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    leave_type VARCHAR(20) NOT NULL COMMENT '请假类型',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    duration DOUBLE DEFAULT 0 COMMENT '时长(天)',
    reason VARCHAR(500) DEFAULT '' COMMENT '请假原因',
    approval_status INT DEFAULT 0 COMMENT '审批状态 0待审 1通过 2驳回',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_status (approval_status)
) COMMENT '请假记录表';

-- ============================================================
-- 加班记录
-- ============================================================
CREATE TABLE att_overtime (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    duration DOUBLE DEFAULT 0 COMMENT '时长(小时)',
    reason VARCHAR(500) DEFAULT '' COMMENT '加班原因',
    approval_status INT DEFAULT 0 COMMENT '审批状态 0待审 1通过 2驳回',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_status (approval_status)
) COMMENT '加班记录表';

-- ============================================================
-- 考勤基准规则
-- ============================================================
CREATE TABLE att_schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_start_time TIME NOT NULL COMMENT '标准上班时间',
    work_end_time TIME NOT NULL COMMENT '标准下班时间',
    flex_enabled TINYINT DEFAULT 0 COMMENT '弹性工作制 1是 0否',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted TINYINT DEFAULT 0
) COMMENT '考勤基准规则表';

INSERT INTO att_schedule (work_start_time, work_end_time, flex_enabled) VALUES
('09:00:00', '18:00:00', 0);

-- ============================================================
-- 权限授予（三账户模型）
-- 注：请在执行本脚本前先执行 00_create_users.sql 创建账户
-- ============================================================
GRANT ALL PRIVILEGES ON unified_system_db.* TO 'unified_dev'@'localhost';
GRANT SELECT, INSERT, UPDATE ON unified_system_db.* TO 'unified_ops'@'localhost';
GRANT SELECT, INSERT, UPDATE ON unified_system_db.* TO 'unified_app'@'localhost';
